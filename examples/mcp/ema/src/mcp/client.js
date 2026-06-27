// MCP Client
// ==========
// Drives the full Enterprise-Managed Authorization flow for a chosen user
// against a chosen MCP server:
//   1. SSO login with the enterprise IdP  -> ID Token
//   2. Token-exchange the ID Token        -> ID-JAG  (per-resource policy here)
//   3. jwt-bearer the ID-JAG at the MAS   -> MCP access token (bound to resource)
//   4. Call that MCP Resource Server with the access token
//
// Usage:
//   node src/client.js <user> [resource]            run flow + call the tool
//   node src/client.js <user> <resource> --token-only   print ONLY the token
//
//   node src/client.js alice files       # allowed
//   node src/client.js alice payroll     # DENIED by org policy
//   node src/client.js carol payroll     # allowed (payroll-admins)

import { IDP, MAS, RESOURCES, CLIENT } from "../shared/config.js";

const REDIRECT = "http://localhost:9999/callback";

async function form(url, body) {
  const res = await fetch(url, {
    method: "POST",
    headers: { "content-type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams(body),
  });
  return { status: res.status, json: await res.json().catch(() => ({})) };
}

// Steps 1-3: returns an MCP access token for `user` at `resource`, or throws.
export async function getAccessToken(user, resource, log = () => {}) {
  // 1. SSO login -> authorization code -> ID Token
  log("1. SSO", `Logging in '${user}' at the enterprise IdP`);
  const authUrl = new URL(`${IDP.issuer}/authorize`);
  authUrl.searchParams.set("user", user);
  authUrl.searchParams.set("redirect_uri", REDIRECT);
  const authRes = await fetch(authUrl, { redirect: "manual" });
  const code = new URL(authRes.headers.get("location")).searchParams.get("code");

  const tok = await form(`${IDP.issuer}/token`, {
    grant_type: "authorization_code",
    code,
    client_id: CLIENT.client_id,
    client_secret: CLIENT.client_secret,
    redirect_uri: REDIRECT,
  });
  const idToken = tok.json.id_token;
  log("1. SSO", `ID Token acquired (${idToken.slice(0, 24)}...)`);

  // 2. Exchange ID Token -> ID-JAG (per-resource org policy evaluated by IdP)
  log("2. ID-JAG", `Requesting ID-JAG for ${resource}`);
  const jag = await form(`${IDP.issuer}/token`, {
    grant_type: "urn:ietf:params:oauth:grant-type:token-exchange",
    requested_token_type: "urn:ietf:params:oauth:token-type:id-jag",
    subject_token: idToken,
    subject_token_type: "urn:ietf:params:oauth:token-type:id_token",
    audience: MAS.issuer, // Resource Authorization Server issuer
    resource, // Resource Identifier of the target MCP server
    scope: "mcp:tools",
    client_id: CLIENT.client_id,
    client_secret: CLIENT.client_secret,
  });
  if (jag.status !== 200) {
    const err = new Error(jag.json.error_description || jag.json.error);
    err.denied = jag.json;
    throw err;
  }
  const idjag = jag.json.access_token;
  log("2. ID-JAG", `ID-JAG issued (${idjag.slice(0, 24)}...)`);

  // 3. Exchange ID-JAG -> MCP access token at the MAS (no user redirect!)
  log("3. Access Token", "Presenting ID-JAG to the MCP Authorization Server");
  const at = await form(`${MAS.issuer}/token`, {
    grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
    assertion: idjag,
    client_id: CLIENT.client_id,
  });
  if (at.status !== 200) throw new Error(JSON.stringify(at.json));
  log("3. Access Token", `Access token issued (${at.json.access_token.slice(0, 24)}...)`);
  return at.json.access_token;
}

function consoleLog(step, msg) {
  console.log(`\x1b[36m[${step}]\x1b[0m ${msg}`);
}

async function main() {
  const args = process.argv.slice(2);
  const tokenOnly = args.includes("--token-only");
  const positional = args.filter((a) => !a.startsWith("-"));
  const user = positional[0] || "alice";
  const resourceKey = positional[1] || "files";
  const R = RESOURCES[resourceKey];
  if (!R) {
    console.error(`Unknown resource '${resourceKey}'. Choose: ${Object.keys(RESOURCES).join(", ")}`);
    process.exit(1);
  }

  let accessToken;
  try {
    accessToken = await getAccessToken(user, R.resource, tokenOnly ? () => {} : consoleLog);
  } catch (e) {
    if (tokenOnly) console.error(e.message);
    else console.error(`\x1b[31mDENIED:\x1b[0m`, e.denied || e.message);
    process.exit(1);
  }

  if (tokenOnly) {
    process.stdout.write(accessToken + "\n");
    return;
  }

  // 4. Call the target MCP Resource Server (tools/call over JSON-RPC).
  consoleLog("4. MCP Call", `Calling '${R.tool.name}' on ${R.serverName}`);
  const res = await fetch(R.resource, {
    method: "POST",
    headers: {
      "content-type": "application/json",
      authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify({
      jsonrpc: "2.0",
      id: 1,
      method: "tools/call",
      params: { name: R.tool.name, arguments: {} },
    }),
  });
  const out = await res.json();
  console.log(`    \x1b[32m${out.result.content[0].text}\x1b[0m`);
}

if (import.meta.url === `file://${process.argv[1]}`) main();
