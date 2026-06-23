import { useState, type ReactNode } from 'react';
import { NavBar } from './NavBar';
import { LoginDialog } from './LoginDialog';

interface LayoutProps {
  children: ReactNode;
}

export function Layout({ children }: LayoutProps) {
  const [loginOpen, setLoginOpen] = useState(false);

  return (
    <div className="qb-shell">
      <NavBar onLoginClick={() => setLoginOpen(true)} />
      <main className="qb-main">{children}</main>
      <footer className="qb-footer">
        <span>QuickBite — food delivery, federated.</span>
      </footer>
      <LoginDialog open={loginOpen} onClose={() => setLoginOpen(false)} />
    </div>
  );
}
