// MCP Authorization Server (MAS)
// ==============================
// Accepts an ID-JAG via the jwt-bearer grant, validates it against the IdP's
// JWKS, and issues a short-lived MCP access token bound to the resource.

import express from "express";
import { SignJWT, jwtVerify, createRemoteJWKSet } from "jose";
import { IDP, MAS, POLICY } from "../shared/config.js";
import { makeSigner } from "../shared/keys.js";

const app = express();
app.use(express.urlencoded({ extended: true }));
app.use(express.json());

const signer = await makeSigner(MAS.issuer);
// Verify ID-JAGs against the IdP's published keys.
const idpJwks = createRemoteJWKSet(new URL(`${IDP.issuer}/jwks`));

app.get("/.well-known/oauth-authorization-server", (_req, res) => {
  res.json({
    issuer: MAS.issuer,
    token_endpoint: `${MAS.issuer}/token`,
    jwks_uri: `${MAS.issuer}/jwks`,
    grant_types_supported: ["urn:ietf:params:oauth:grant-type:jwt-bearer"],
  });
});

app.get("/jwks", (_req, res) => res.json(signer.jwks));

app.post("/token", async (req, res) => {
  const { grant_type, assertion, client_id } = req.body;

  if (grant_type !== "urn:ietf:params:oauth:grant-type:jwt-bearer")
    return res.status(400).json({ error: "unsupported_grant_type" });

  // Validate the ID-JAG: signature (IdP JWKS), audience (us), issuer (IdP),
  // expiry, and the id-jag typ header.
  let idjag;
  try {
    ({ payload: idjag } = await jwtVerify(assertion, idpJwks, {
      issuer: IDP.issuer,
      audience: MAS.issuer,
      typ: "oauth-id-jag+jwt",
    }));
  } catch (e) {
    return res
      .status(400)
      .json({ error: "invalid_grant", error_description: e.message });
  }

  // Ensure the ID-JAG targets one of the MCP resources we govern.
  if (!idjag.resource || !POLICY[idjag.resource])
    return res.status(400).json({ error: "invalid_target" });

  // Map IdP claims (sub/email/scope) to an MCP access token.
  const access_token = await new SignJWT({
    email: idjag.email,
    scope: idjag.scope,
    client_id: client_id || idjag.client_id,
  })
    .setProtectedHeader({ alg: "RS256", kid: signer.kid, typ: "at+jwt" })
    .setIssuer(MAS.issuer)
    .setSubject(idjag.sub)
    .setAudience(idjag.resource) // bound to the specific MCP Resource Server
    .setIssuedAt()
    .setExpirationTime("15m")
    .sign(signer.privateKey);

  res.json({ token_type: "Bearer", access_token, scope: idjag.scope, expires_in: 900 });
});

app.listen(MAS.port, () =>
  console.log(`[MAS]  MCP Authorization Server listening on ${MAS.issuer}`)
);
