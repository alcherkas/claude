// Shared configuration.
//
// One Enterprise IdP + one MCP Authorization Server (MAS) centrally govern
// MULTIPLE MCP resource servers. That's the whole point of the extension: the
// IdP decides, per-server, who is allowed — so the same user login yields
// different access on different servers.

export const IDP = {
  port: 4000,
  issuer: "http://localhost:4000", // Enterprise IdP issuer identifier
};

export const MAS = {
  port: 4001,
  issuer: "http://localhost:4001", // MCP Authorization Server issuer identifier
};

// Two independent MCP resource servers, both guarded by the same MAS/IdP.
// Each declares the org group required to access it. Pick one by key, e.g.
//   node src/mrs.js files
//   node src/mrs.js payroll
export const RESOURCES = {
  files: {
    port: 4002,
    resource: "http://localhost:4002/mcp",
    requiredGroup: "files-users", // org policy: who may use this server
    serverName: "ema-files",
    tool: {
      name: "list_files",
      description: "Lists the user's enterprise file workspace (demo).",
      text: (u) => `Files for ${u.email}: report.docx, budget.xlsx`,
    },
  },
  payroll: {
    port: 4003,
    resource: "http://localhost:4003/mcp",
    requiredGroup: "payroll-admins", // a more restricted group
    serverName: "ema-payroll",
    tool: {
      name: "view_salaries",
      description: "Views the payroll salary table (demo, highly sensitive).",
      text: (u) => `Payroll accessed by ${u.email}: alice=$120k, bob=$95k`,
    },
  },
};

// A "client" registered with the enterprise IdP and known to the MAS.
export const CLIENT = {
  client_id: "mcp-client-app",
  client_secret: "demo-client-secret",
};

// Demo enterprise users and their group memberships. The IdP enforces policy
// against these groups per-resource:
//   - alice: files only  (not payroll)
//   - carol: both files and payroll
//   - bob:   neither (an intern)
export const USERS = {
  alice: { sub: "user-alice", email: "alice@corp.example", groups: ["files-users"] },
  carol: { sub: "user-carol", email: "carol@corp.example", groups: ["files-users", "payroll-admins"] },
  bob: { sub: "user-bob", email: "bob@corp.example", groups: ["interns"] },
};

// resource identifier -> required group, used by the IdP policy engine.
export const POLICY = Object.fromEntries(
  Object.values(RESOURCES).map((r) => [r.resource, r.requiredGroup])
);
