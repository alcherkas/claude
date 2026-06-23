import { useQuery } from '@tanstack/react-query';
import { getMe } from '../api/client';

// Customer identity card — identity-service GET /api/users/me.
export function ProfileCard() {
  const {
    data: user,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ['me'],
    queryFn: getMe,
  });

  if (isLoading) {
    return <div className="acct-card acct-muted">Loading profile…</div>;
  }

  if (isError || !user) {
    return (
      <div className="acct-card acct-error">
        Could not load your profile
        {error instanceof Error ? `: ${error.message}` : ''}.
      </div>
    );
  }

  const initials = user.fullName
    .split(' ')
    .map((part) => part.charAt(0))
    .join('')
    .slice(0, 2)
    .toUpperCase();

  return (
    <section className="acct-card acct-profile">
      <div className="acct-profile__avatar" aria-hidden="true">
        {initials || '?'}
      </div>
      <div className="acct-profile__body">
        <h2 className="acct-profile__name">{user.fullName}</h2>
        <p className="acct-profile__email">{user.email}</p>
        <dl className="acct-profile__meta">
          <div>
            <dt>Role</dt>
            <dd>
              <span className="acct-badge">{user.role}</span>
            </dd>
          </div>
          <div>
            <dt>Member since</dt>
            <dd>{new Date(user.createdAt).toLocaleDateString()}</dd>
          </div>
          <div>
            <dt>Account ID</dt>
            <dd className="acct-mono">{user.id}</dd>
          </div>
        </dl>
      </div>
    </section>
  );
}
