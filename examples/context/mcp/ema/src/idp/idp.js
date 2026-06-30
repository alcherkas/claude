// Enterprise IdP
// =================
// Two responsibilities in this demo:
//   1. /authorize + /token  -> standard OIDC login, issues an ID Token.
//   2. /token (token-exchange) -> exchanges the ID Token for an ID-JAG,
//      AFTER evaluating org policy (group membership).
//
// This is the "centralized policy decision point". A user who is not authorized
// for the MCP server is denied here and the client never gets a token.

import express from "express";
import { SignJWT, jwtVerify, createLocalJWKSet } from "jose";
import { IDP, MAS, CLIENT, USERS, POLICY } from "../shared/config.js";
import { makeSigner } from "../shared/keys.js";

const app = express();
app.use(express.urlencoded({ extended: true }));
app.use(express.json());

const signer = await makeSigner(IDP.issuer);
const localJwks = createLocalJWKSet(signer.jwks);

// --- Discovery -------------------------------------------------------------
app.get("/.well-known/openid-configuration", (_req, res) => {
  res.json({
    issuer: IDP.issuer,
    authorization_endpoint: `${IDP.issuer}/authorize`,
    token_endpoint: `${IDP.issuer}/token`,
    jwks_uri: `${IDP.issuer}/jwks`,
    grant_types_supported: [
      "authorization_code",
      "urn:ietf:params:oauth:grant-type:token-exchange",
    ],
    // Advertise the ID-JAG grant profile per the extension spec.
    authorization_grant_profiles_supported: [
      "urn:ietf:params:oauth:grant-profile:id-jag",
    ],
  });
});

app.get("/jwks", (_req, res) => res.json(signer.jwks));

// --- 1. Login: issue an authorization code -------------------------------
// (Real IdPs render a login page; we accept ?user= to keep the demo headless.)
app.get("/authorize", (req, res) => {
  const { user = "alice", redirect_uri, state } = req.query;
  if (!USERS[user]) return res.status(400).json({ error: "unknown_user" });
  const code = Buffer.from(JSON.stringify({ user })).toString("base64url");
  const url = new URL(redirect_uri);
  url.searchParams.set("code", code);
  if (state) url.searchParams.set("state", state);
  res.redirect(url.toString());
});

async function issueIdToken(user) {
  const u = USERS[user];
  return new SignJWT({ email: u.email, groups: u.groups })
    .setProtectedHeader({ alg: "RS256", kid: signer.kid, typ: "JWT" })
    .setIssuer(IDP.issuer)
    .setSubject(u.sub)
    .setAudience(CLIENT.client_id)
    .setIssuedAt()
    .setExpirationTime("1h")
    .sign(signer.privateKey);
}

// --- 2. Token endpoint: handles BOTH code exchange and ID-JAG exchange ----
app.post("/token", async (req, res) => {
  const { grant_type } = req.body;

  // 2a. authorization_code -> ID Token (SSO login completes)
  if (grant_type === "authorization_code") {
    const { code, client_id } = req.body;
    if (client_id !== CLIENT.client_id)
      return res.status(401).json({ error: "invalid_client" });
    const { user } = JSON.parse(Buffer.from(code, "base64url").toString());
    const id_token = await issueIdToken(user);
    return res.json({ token_type: "N_A", id_token, expires_in: 3600 });
  }

  // 2b. token-exchange -> ID-JAG (the heart of the extension)
  if (grant_type === "urn:ietf:params:oauth:grant-type:token-exchange") {
    const {
      requested_token_type,
      subject_token,
      subject_token_type,
      audience,
      resource,
      scope,
      client_id,
    } = req.body;

    if (requested_token_type !== "urn:ietf:params:oauth:token-type:id-jag")
      return res.status(400).json({ error: "unsupported_token_type" });
    if (subject_token_type !== "urn:ietf:params:oauth:token-type:id_token")
      return res.status(400).json({ error: "invalid_request" });

    // Validate the subject (ID) token we previously issued.
    let claims;
    try {
      ({ payload: claims } = await jwtVerify(subject_token, localJwks, {
        issuer: IDP.issuer,
        audience: CLIENT.client_id,
      }));
    } catch {
      return res.status(400).json({ error: "invalid_grant" });
    }

    // *** POLICY DECISION POINT ***
    // Per-resource policy: each MCP server requires a specific org group.
    // This is what makes access "enterprise-managed" — the same user is
    // allowed on one server and denied on another, decided centrally here.
    const requiredGroup = POLICY[resource];
    if (!requiredGroup) {
      return res.status(400).json({
        error: "invalid_target",
        error_description: `Unknown resource ${resource}`,
      });
    }
    if (!claims.groups?.includes(requiredGroup)) {
      return res.status(403).json({
        error: "access_denied",
        error_description: `User ${claims.sub} lacks group '${requiredGroup}' required for ${resource}`,
      });
    }

    // Mint the ID-JAG. Note typ header and aud == Resource AS issuer.
    const idjag = await new SignJWT({
      email: claims.email,
      resource, // Resource Identifier of the MCP server (if provided)
      client_id,
      scope: scope || "mcp:tools",
    })
      .setProtectedHeader({ alg: "RS256", kid: signer.kid, typ: "oauth-id-jag+jwt" })
      .setIssuer(IDP.issuer)
      .setSubject(claims.sub)
      .setAudience(audience) // the MCP Authorization Server's issuer id
      .setJti(crypto.randomUUID())
      .setIssuedAt()
      .setExpirationTime("5m")
      .sign(signer.privateKey);

    return res.json({
      issued_token_type: "urn:ietf:params:oauth:token-type:id-jag",
      access_token: idjag, // token-exchange returns the result in access_token
      token_type: "N_A",
      expires_in: 300,
    });
  }

  return res.status(400).json({ error: "unsupported_grant_type" });
});

app.listen(IDP.port, () =>
  console.log(`[IdP]  Enterprise IdP listening on ${IDP.issuer}`)
);
