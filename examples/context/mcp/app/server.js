#!/usr/bin/env node
// Minimal MCP App server (stdio).
// Exposes ONE tool that references a ui:// resource. A host that supports
// MCP Apps (e.g. Claude Desktop) fetches the resource and renders the HTML
// inside a sandboxed iframe in the conversation.

import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
  ListToolsRequestSchema,
  CallToolRequestSchema,
  ListResourcesRequestSchema,
  ReadResourceRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";

const __dirname = dirname(fileURLToPath(import.meta.url));
const UI_URI = "ui://mcp-apps-explainer/index.html";
const UI_MIME = "text/html;profile=mcp-app"; // required by the MCP Apps spec
const html = readFileSync(join(__dirname, "ui", "index.html"), "utf8");

// UI is fully self-contained (no external loads), so no CSP allowances are needed.
const UI_META = { ui: {} };

const server = new Server(
  { name: "mcp-apps-explainer", version: "0.1.0" },
  { capabilities: { tools: {}, resources: {} } }
);

// The tool declares its UI via _meta.ui.resourceUri — this is what makes it an "MCP App".
server.setRequestHandler(ListToolsRequestSchema, async () => ({
  tools: [
    {
      name: "explain_mcp_apps",
      description: "Show an interactive page explaining what MCP Apps are.",
      inputSchema: { type: "object", properties: {}, additionalProperties: false },
      _meta: { ui: { resourceUri: UI_URI } },
    },
  ],
}));

server.setRequestHandler(CallToolRequestSchema, async (req) => {
  if (req.params.name !== "explain_mcp_apps") {
    throw new Error(`Unknown tool: ${req.params.name}`);
  }
  return {
    content: [{ type: "text", text: "Rendering the MCP Apps explainer." }],
    _meta: { ui: { resourceUri: UI_URI } },
  };
});

// Serve the HTML as a ui:// resource the host can fetch and render.
server.setRequestHandler(ListResourcesRequestSchema, async () => ({
  resources: [{ uri: UI_URI, name: "MCP Apps Explainer", mimeType: UI_MIME, _meta: UI_META }],
}));

server.setRequestHandler(ReadResourceRequestSchema, async (req) => {
  if (req.params.uri !== UI_URI) throw new Error(`Unknown resource: ${req.params.uri}`);
  return { contents: [{ uri: UI_URI, mimeType: UI_MIME, text: html, _meta: UI_META }] };
});

await server.connect(new StdioServerTransport());
console.error("mcp-apps-explainer running on stdio");
