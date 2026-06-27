// MCP Resource Server (MRS)
// =========================
// A minimal Streamable-HTTP MCP server (JSON-RPC 2.0: initialize / tools/list /
// tools/call) so the Claude Code CLI can connect. One process serves ONE of the
// configured resources.
//
//   node src/mcp/mrs.js files       # http://localhost:4002/mcp
//   node src/mcp/mrs.js payroll     # http://localhost:4003/mcp
//
// servers.js imports startMrs() to run both instances in one process.
//
// Every request must carry a Bearer access token issued by the MAS and bound
// (aud) to THIS resource; the token is validated against the MAS JWKS.

import express from "express";
import { jwtVerify, createRemoteJWKSet } from "jose";
import { MAS, RESOURCES } from "../shared/config.js";

const masJwks = createRemoteJWKSet(new URL(`${MAS.issuer}/jwks`));

export function startMrs(key) {
  const R = RESOURCES[key];
  if (!R) {
    console.error(`Unknown resource '${key}'. Choose: ${Object.keys(RESOURCES).join(", ")}`);
    process.exit(1);
  }

  const app = express();
  app.use(express.json());

  // Protected-resource metadata (RFC 9728): which AS guards this resource, and
  // that the enterprise-managed-authorization extension is required.
  app.get("/.well-known/oauth-protected-resource", (_req, res) => {
    res.json({
      resource: R.resource,
      authorization_servers: [MAS.issuer],
      extensions: {
        "io.modelcontextprotocol/enterprise-managed-authorization": { required: true },
      },
    });
  });

  async function authenticate(req) {
    const auth = req.headers.authorization || "";
    const token = auth.startsWith("Bearer ") ? auth.slice(7) : null;
    if (!token) return null;
    try {
      const { payload } = await jwtVerify(token, masJwks, {
        issuer: MAS.issuer,
        audience: R.resource, // token must be bound to THIS server
      });
      return payload;
    } catch {
      return null;
    }
  }

  const TOOLS = [
    {
      name: R.tool.name,
      description: R.tool.description,
      inputSchema: { type: "object", properties: {}, additionalProperties: false },
    },
  ];

  app.post("/mcp", async (req, res) => {
    const user = await authenticate(req);
    if (!user) {
      res.set(
        "WWW-Authenticate",
        `Bearer resource_metadata="${R.resource}/.well-known/oauth-protected-resource"`
      );
      return res.status(401).json({
        jsonrpc: "2.0",
        id: req.body?.id ?? null,
        error: { code: -32001, message: "unauthorized" },
      });
    }

    const { id, method, params } = req.body || {};
    const reply = (result) => res.json({ jsonrpc: "2.0", id, result });

    switch (method) {
      case "initialize":
        return reply({
          protocolVersion: params?.protocolVersion || "2025-06-18",
          capabilities: { tools: {} },
          serverInfo: { name: R.serverName, version: "1.0.0" },
        });

      case "notifications/initialized":
        return res.status(202).end();

      case "tools/list":
        return reply({ tools: TOOLS });

      case "tools/call": {
        if (params?.name !== R.tool.name)
          return res.json({
            jsonrpc: "2.0",
            id,
            error: { code: -32602, message: `unknown tool: ${params?.name}` },
          });
        return reply({ content: [{ type: "text", text: R.tool.text(user) }] });
      }

      default:
        return res.json({
          jsonrpc: "2.0",
          id,
          error: { code: -32601, message: `method not found: ${method}` },
        });
    }
  });

  app.listen(R.port, () =>
    console.log(`[MRS:${key}] ${R.serverName} listening on ${R.resource}`)
  );
}

// CLI entry: `node src/mcp/mrs.js <key>`
if (import.meta.url === `file://${process.argv[1]}`) startMrs(process.argv[2]);
