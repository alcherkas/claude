/// <reference types="vite/client" />

interface ImportMetaEnv {
  // Gateway base URL; defaults to http://localhost:8080 (PLATFORM_SPEC §2.3).
  readonly VITE_GATEWAY_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
