// Lightweight read-only view of the session the shell host persists in
// localStorage (keys mirror the host's AuthProvider). The driver portal remote
// does not own auth — it only needs the current user's id (the courier) to
// resolve the driver record that backs every endpoint in this MFE.

const USER_STORAGE_KEY = 'quickbite.user';

interface PersistedUser {
  id: string;
  email: string;
  fullName: string;
  role: string;
}

export function getCurrentUser(): PersistedUser | null {
  const raw = localStorage.getItem(USER_STORAGE_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as PersistedUser;
  } catch {
    return null;
  }
}

export function getCurrentUserId(): string | null {
  return getCurrentUser()?.id ?? null;
}
