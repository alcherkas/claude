/// <reference types="vite/client" />

interface ImportMetaEnv {
  // Gateway base URL for all /api/* calls (PLATFORM_SPEC §2.3). Defaults to
  // http://localhost:8080 when unset.
  readonly VITE_GATEWAY_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
