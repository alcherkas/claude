import { useState, type FormEvent } from 'react';
import { useAuth } from './AuthProvider';
import type { UserRole } from '../types';

interface LoginDialogProps {
  open: boolean;
  onClose: () => void;
}

type Mode = 'login' | 'register';

const ROLES: UserRole[] = ['CUSTOMER', 'RESTAURANT_OWNER', 'COURIER'];

export function LoginDialog({ open, onClose }: LoginDialogProps) {
  const { login, register } = useAuth();
  const [mode, setMode] = useState<Mode>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [role, setRole] = useState<UserRole>('CUSTOMER');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (!open) return null;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      if (mode === 'login') {
        await login({ email, password });
      } else {
        await register({ email, password, fullName, role });
      }
      onClose();
    } catch (err) {
      setError(
        mode === 'login'
          ? 'Invalid email or password.'
          : 'Could not create the account.',
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="qb-modal-backdrop" role="dialog" aria-modal="true">
      <div className="qb-modal">
        <header className="qb-modal__header">
          <h2>{mode === 'login' ? 'Log in' : 'Create account'}</h2>
          <button
            type="button"
            className="qb-modal__close"
            aria-label="Close"
            onClick={onClose}
          >
            ×
          </button>
        </header>

        <form className="qb-form" onSubmit={handleSubmit}>
          {mode === 'register' && (
            <label className="qb-field">
              <span>Full name</span>
              <input
                type="text"
                value={fullName}
                required
                onChange={(e) => setFullName(e.target.value)}
              />
            </label>
          )}

          <label className="qb-field">
            <span>Email</span>
            <input
              type="email"
              value={email}
              required
              autoComplete="email"
              onChange={(e) => setEmail(e.target.value)}
            />
          </label>

          <label className="qb-field">
            <span>Password</span>
            <input
              type="password"
              value={password}
              required
              autoComplete={
                mode === 'login' ? 'current-password' : 'new-password'
              }
              onChange={(e) => setPassword(e.target.value)}
            />
          </label>

          {mode === 'register' && (
            <label className="qb-field">
              <span>Account type</span>
              <select
                value={role}
                onChange={(e) => setRole(e.target.value as UserRole)}
              >
                {ROLES.map((r) => (
                  <option key={r} value={r}>
                    {r.replace('_', ' ')}
                  </option>
                ))}
              </select>
            </label>
          )}

          {error && <p className="qb-form__error">{error}</p>}

          <button type="submit" className="qb-btn" disabled={submitting}>
            {submitting
              ? 'Please wait…'
              : mode === 'login'
                ? 'Log in'
                : 'Sign up'}
          </button>
        </form>

        <footer className="qb-modal__footer">
          {mode === 'login' ? (
            <button
              type="button"
              className="qb-link"
              onClick={() => {
                setMode('register');
                setError(null);
              }}
            >
              Need an account? Sign up
            </button>
          ) : (
            <button
              type="button"
              className="qb-link"
              onClick={() => {
                setMode('login');
                setError(null);
              }}
            >
              Already have an account? Log in
            </button>
          )}
        </footer>
      </div>
    </div>
  );
}
