import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import federation from '@originjs/vite-plugin-federation';

// QuickBite shell — Module Federation HOST.
// Federation name: "shell" (per PLATFORM_SPEC §1.2). Dev port: 3000.
// Lists all six feature MFE remotes by their dev remoteEntry URL and shares
// react / react-dom / react-router-dom as singletons.
export default defineConfig({
  plugins: [
    react(),
    federation({
      name: 'shell',
      remotes: {
        discovery: 'http://localhost:3001/assets/remoteEntry.js',
        checkout: 'http://localhost:3002/assets/remoteEntry.js',
        orderTracking: 'http://localhost:3003/assets/remoteEntry.js',
        account: 'http://localhost:3004/assets/remoteEntry.js',
        restaurantAdmin: 'http://localhost:3005/assets/remoteEntry.js',
        driverPortal: 'http://localhost:3006/assets/remoteEntry.js',
      },
      shared: ['react', 'react-dom', 'react-router-dom'],
    }),
  ],
  server: {
    port: 3000,
    strictPort: true,
  },
  preview: {
    port: 3000,
    strictPort: true,
  },
  build: {
    target: 'esnext',
    minify: true,
    cssCodeSplit: false,
  },
});
