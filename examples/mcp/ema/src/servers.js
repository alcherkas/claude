// Boots every service in one process:
//   - Enterprise IdP            (idp/)
//   - MCP Authorization Server  (mcp/)
//   - both MCP Resource Servers (mcp/)  -> files + payroll
//
// Handy for `npm run servers`, then `npm run client` in another terminal.
import "./idp/idp.js";
import "./mcp/mas.js";
import { startMrs } from "./mcp/mrs.js";
import { RESOURCES } from "./shared/config.js";

for (const key of Object.keys(RESOURCES)) startMrs(key);
