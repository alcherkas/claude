# MCP Apps — minimal example

A tiny [MCP App](https://modelcontextprotocol.io/extensions/apps/overview): an MCP
server whose tool renders an **interactive HTML page inside the chat** instead of
plain text. The page explains the benefits of MCP Apps and diagrams the data flow
between the app and Claude.

## Files

| File             | What it is                                                              |
| ---------------- | ----------------------------------------------------------------------- |
| `server.js`      | stdio MCP server. Exposes the `explain_mcp_apps` tool + a `ui://` resource. |
| `ui/index.html`  | The interactive App UI (benefits, SVG flow diagram, live bridge demo). |
| `package.json`   | Deps + `npm start`.                                                      |

The magic is one line: the tool declares `_meta.ui.resourceUri`, pointing at the
`ui://` HTML resource. A host that supports MCP Apps fetches that resource and
renders it in a sandboxed iframe.

## Run it

```bash
npm install
npm start          # serves on stdio
```

Add it to an MCP-Apps-capable host:

```bash
claude mcp add explainer -- node /absolute/path/to/server.js
```

Then ask the host to run **explain_mcp_apps**.

> **Note:** MCP Apps render only in GUI hosts (Claude Desktop, claude.ai, VS Code
> Copilot, …). The Claude Code **CLI** can call the tool but has no surface to
> render the iframe. Open `ui/index.html` directly in a browser to preview the UI
> (the page renders, but the bridge calls only resolve when run inside a real host).

The UI uses the official [`@modelcontextprotocol/ext-apps`](https://github.com/modelcontextprotocol/ext-apps)
`App` class (loaded from a CDN, so there's no build step): `app.connect()` does the
handshake, `ontoolinput` / `ontoolresult` receive pushed data, and
`callServerTool` / `sendMessage` talk back to the host.

## How it talks to Claude

```
User → Claude:        "explain mcp apps"
Claude → Server:      tools/call explain_mcp_apps
Server → Claude:      text result + ui:// resource
Claude → iframe:      renders HTML, pushes tool data
iframe → Claude:      ui/sendMessage / tools/call  (over postMessage)
Claude → iframe:      fresh data pushed back
```
