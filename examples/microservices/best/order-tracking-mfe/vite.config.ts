import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import federation from '@originjs/vite-plugin-federation';

// QuickBite order-tracking-mfe — Module Federation REMOTE.
// Federation name: "orderTracking" (per PLATFORM_SPEC §1.2). Dev port: 3003.
// Exposes './App' to the shell host and shares react / react-dom /
// react-router-dom as singletons so the federation graph stays consistent.
export default defineConfig({
  plugins: [
    react(),
    federation({
      name: 'orderTracking',
      filename: 'remoteEntry.js',
      exposes: {
        './App': './src/App.tsx',
      },
      shared: ['react', 'react-dom', 'react-router-dom'],
    }),
  ],
  server: {
    port: 3003,
    strictPort: true,
  },
  preview: {
    port: 3003,
    strictPort: true,
  },
  build: {
    target: 'esnext',
    minify: true,
    cssCodeSplit: false,
  },
});
