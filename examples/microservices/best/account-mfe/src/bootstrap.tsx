import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import App from './App';
import './styles.css';

// Standalone QueryClient — only used when the account MFE runs on its own dev
// server. Inside the shell host the remote reuses the host's QueryClient via
// the shared singleton graph and the host's <BrowserRouter>.
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000,
      refetchOnWindowFocus: false,
    },
  },
});

export function mount(container: HTMLElement): void {
  createRoot(container).render(
    <StrictMode>
      <QueryClientProvider client={queryClient}>
        {/* In standalone mode we provide our own router (basename mirrors the
            host route the remote is mounted under). */}
        <BrowserRouter basename="/account">
          <App />
        </BrowserRouter>
      </QueryClientProvider>
    </StrictMode>,
  );
}
