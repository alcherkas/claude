# MCP Enterprise-Managed Authorization — runnable example

A minimal, dependency-light implementation of the MCP
[Enterprise-Managed Authorization](https://modelcontextprotocol.io/extensions/auth/enterprise-managed-authorization)
extension (`io.modelcontextprotocol/enterprise-managed-authorization`).

Instead of each user authorizing each MCP server, the organization's IdP is the
authoritative policy decision point. The client never visits the MCP
authorization server's `/authorize` page — it obtains an **ID-JAG** (Identity
Assertion JWT Authorization Grant) from the enterprise IdP and exchanges it for
an access token.

To actually *prove* "enterprise-managed", this example runs **two** MCP servers
governed by **one** IdP + Authorization Server, with **per-server policy**:

| Server | Required org group | alice | carol | bob |
|--------|--------------------|:----:|:----:|:---:|
| `files`   (`:4002`) | `files-users`    | ✅ | ✅ | ❌ |
| `payroll` (`:4003`) | `payroll-admins` | ❌ | ✅ | ❌ |

Same login, different server, different outcome — decided centrally at the IdP.
That's the whole value proposition.

## Architecture

```
            src/idp/                 src/mcp/
        ┌───────────────┐   ┌──────────────────────────────┐
        │ Enterprise    │   │ MAS (auth server)            │
        │ IdP  :4000    │   │      :4001                   │
        │ - SSO login   │   │ - validates ID-JAG           │
        │ - ID-JAG mint │   │ - mints access token         │
        │ - POLICY      │   │ MRS files   :4002 (tools)    │
        └───────────────┘   │ MRS payroll :4003 (tools)    │
                            │ client (the MCP client)      │
                            └──────────────────────────────┘
```

## The flow

```
 MCP Client          Enterprise IdP            MCP Auth Server (MAS)   MCP Resource (MRS)
     |  1. SSO login  ───────►|                          |                    |
     |◄── ID Token ───────────|                          |                    |
     |  2. token-exchange ───►| (evaluate PER-SERVER     |                    |
     |◄── ID-JAG ─────────────|  ORG POLICY)             |                    |
     |  3. jwt-bearer (ID-JAG) ─────────────────────────►| (validate ID-JAG)  |
     |◄── MCP access token ──────────────────────────────|                    |
     |  4. tools/call + Bearer access token ──────────────────────────────────►|
     |◄── result ──────────────────────────────────────────────────────────────|
```

| Step | Endpoint | Grant / token type |
|------|----------|--------------------|
| 1 | IdP `/token` | `authorization_code` → `id_token` |
| 2 | IdP `/token` | `urn:ietf:params:oauth:grant-type:token-exchange`, `requested_token_type=urn:ietf:params:oauth:token-type:id-jag` |
| 3 | MAS `/token` | `urn:ietf:params:oauth:grant-type:jwt-bearer`, `assertion=<ID-JAG>` |
| 4 | MRS `/mcp` | `Authorization: Bearer <access token>` |

The ID-JAG is a JWT with header `typ: oauth-id-jag+jwt`, `iss` = IdP, `aud` =
MAS issuer, and a `resource` claim = the target MCP server's resource id. The
issued access token's `aud` is bound to that specific resource.

## Run it

```bash
npm install
npm run demo
```

`demo` starts all services, then runs four `(user, server)` cases proving
centralized per-server policy: `alice→files` ✅, `alice→payroll` ❌ (denied at
the IdP, step 2, before any token reaches the server), `carol→payroll` ✅,
`bob→files` ❌.

Or run the pieces separately:

```bash
npm run servers                 # IdP :4000, MAS :4001, files :4002, payroll :4003
npm run client alice files      # allowed
npm run client alice payroll    # denied by org policy
npm run client carol payroll    # allowed
```

## Connecting to the Claude Code CLI

> **Caveat:** the Claude Code MCP client does **not** implement the
> enterprise-managed-authorization (ID-JAG / RFC 8693 token-exchange) flow — it
> supports static Bearer tokens and standard OAuth 2.0 only. So Claude Code
> can't perform steps 1–3 itself. The way to test against it is to run the
> enterprise flow with the client to **mint a real access token out-of-band**,
> then hand that token to Claude Code. This exercises the MCP **resource servers**
> (token validation + tools) through Claude Code.

1. Start the services (leave running):

   ```bash
   npm run servers
   ```

2. Mint a per-server access token and register each MCP server. Note the token
   is bound to one resource, so each server needs its own:

   ```bash
   FILES_TOKEN=$(node src/mcp/client.js alice files --token-only)
   PAYROLL_TOKEN=$(node src/mcp/client.js carol payroll --token-only)
   [ -n "$FILES_TOKEN" ] && [ -n "$PAYROLL_TOKEN" ] || { echo "mint failed — are the servers running?"; }

   claude mcp add --transport http ema-files   http://localhost:4002/mcp --header "Authorization: Bearer $FILES_TOKEN"
   claude mcp add --transport http ema-payroll http://localhost:4003/mcp --header "Authorization: Bearer $PAYROLL_TOKEN"
   ```

3. Verify and use:

   ```bash
   claude mcp list      # both should show:  ✔ Connected
   ```

   Inside `claude`, run `/mcp` to inspect them, then ask Claude to call
   `list_files` (on ema-files) or `view_salaries` (on ema-payroll). Each returns
   the enterprise identity baked into its token.

To see the **denial** path, try minting a token Claude Code shouldn't have —
`node src/mcp/client.js alice payroll --token-only` fails at the IdP policy
check, so there's no token to register at all. Access tokens are short-lived
(15 min); re-run step 2 (and `claude mcp remove ema-files` / re-add) to refresh.

## Files

| File | Role |
|------|------|
| `src/idp/idp.js` | Enterprise IdP — SSO login, token-exchange → ID-JAG, **per-server policy decision** |
| `src/mcp/mas.js` | MCP Authorization Server — validates ID-JAG, mints resource-bound access token |
| `src/mcp/mrs.js` | MCP Resource Server (Streamable-HTTP JSON-RPC) — validates token, serves tools. One process per resource |
| `src/mcp/client.js` | MCP Client — drives the 4-step flow for a given `(user, server)` |
| `src/shared/config.js` | Issuers, ports, the two resources, demo users, and the org policy (group→resource) |
| `src/shared/keys.js` | Per-service RSA keypairs + JWKS publication |
| `src/servers.js` | Boots IdP + MAS + both resource servers in one process |
| `src/demo.js` | Runs the four policy cases end to end |

## What's real vs. simplified

- **Real**: the grant types, token-exchange / jwt-bearer parameters, ID-JAG
  `typ` header and claims, resource-bound access tokens, JWKS-based signature
  verification across services, protected-resource metadata advertising the
  extension, and per-server policy enforcement at the IdP.
- **Simplified for brevity**: no PKCE / login UI (login is headless via
  `?user=`), no client-secret enforcement on token-exchange, in-memory keys
  regenerated per boot, one tool per server, and no SSE/streaming on the MCP
  endpoint (plain JSON responses only). Harden these before any real use.
