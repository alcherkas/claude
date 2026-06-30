// One-shot demo proving CENTRALIZED, PER-SERVER policy:
// starts all services, then runs the client for several (user, server) pairs.
// Same login, different servers, different outcomes — decided at the IdP.
import { spawn } from "node:child_process";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const here = dirname(fileURLToPath(import.meta.url));
const node = process.execPath;

const servers = spawn(node, [join(here, "servers.js")], { stdio: "inherit" });

function runClient(user, resource) {
  return new Promise((resolve) => {
    const c = spawn(node, [join(here, "mcp", "client.js"), user, resource], {
      stdio: "inherit",
    });
    c.on("exit", resolve);
  });
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

// (user, server, expectation)
const CASES = [
  ["alice", "files", "ALLOWED  (alice is in files-users)"],
  ["alice", "payroll", "DENIED   (alice lacks payroll-admins)"],
  ["carol", "payroll", "ALLOWED  (carol is a payroll-admin)"],
  ["bob", "files", "DENIED   (bob is an intern)"],
];

try {
  await sleep(800); // let servers bind their ports
  for (const [user, resource, expect] of CASES) {
    console.log(`\n================ ${user} -> ${resource}  [${expect}] ================`);
    await runClient(user, resource);
  }
} finally {
  servers.kill();
}
