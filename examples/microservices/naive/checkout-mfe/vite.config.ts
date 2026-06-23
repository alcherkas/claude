import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import federation from '@originjs/vite-plugin-federation';

// QuickBite checkout-mfe — Module Federation REMOTE.
// Federation name: "checkout" (per PLATFORM_SPEC §1.2). Dev port: 3002.
// Exposes './App' to the shell host and shares react / react-dom /
// react-router-dom as singletons so the federation graph stays consistent.
export default defineConfig({
  plugins: [
    react(),
    federation({
      name: 'checkout',
      filename: 'remoteEntry.js',
      exposes: {
        './App': './src/App.tsx',
      },
      shared: ['react', 'react-dom', 'react-router-dom'],
    }),
  ],
  server: {
    port: 3002,
    strictPort: true,
  },
  preview: {
    port: 3002,
    strictPort: true,
  },
  build: {
    target: 'esnext',
    minify: true,
    cssCodeSplit: false,
  },
});
