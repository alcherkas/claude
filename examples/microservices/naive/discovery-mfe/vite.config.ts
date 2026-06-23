import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import federation from '@originjs/vite-plugin-federation';

// QuickBite discovery — Module Federation REMOTE.
// Federation name: "discovery" (per PLATFORM_SPEC §1.2). Dev port: 3001.
// Exposes './App' as the mountable feature root; the shell host consumes it
// lazily. Shares react / react-dom / react-router-dom as singletons so the
// host and remote run on one copy of each.
export default defineConfig({
  plugins: [
    react(),
    federation({
      name: 'discovery',
      filename: 'remoteEntry.js',
      exposes: {
        './App': './src/App.tsx',
      },
      shared: ['react', 'react-dom', 'react-router-dom'],
    }),
  ],
  server: {
    port: 3001,
    strictPort: true,
    cors: true,
  },
  preview: {
    port: 3001,
    strictPort: true,
  },
  build: {
    target: 'esnext',
    minify: true,
    cssCodeSplit: false,
  },
});
