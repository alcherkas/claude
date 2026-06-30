// Per-process ephemeral signing keys + JWKS publication helpers.
// Each service generates its own RSA keypair on boot and exposes a JWKS so
// other services can verify its signatures over HTTP — exactly how the IdP and
// MAS verify each other's tokens in the spec.

import { generateKeyPair, exportJWK, calculateJwkThumbprint } from "jose";

export async function makeSigner(issuer) {
  const { publicKey, privateKey } = await generateKeyPair("RS256");
  const publicJwk = await exportJWK(publicKey);
  const kid = await calculateJwkThumbprint(publicJwk);
  publicJwk.kid = kid;
  publicJwk.alg = "RS256";
  publicJwk.use = "sig";
  return {
    issuer,
    privateKey,
    kid,
    jwks: { keys: [publicJwk] },
  };
}
