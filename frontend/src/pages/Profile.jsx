import { useEffect, useState } from 'react';
import { api } from '../api/client.js';
import { useAuth } from '../api/auth.jsx';

export default function Profile() {
  const { user, refresh } = useAuth();
  const [profile, setProfile] = useState(user);
  const [error, setError] = useState('');

  useEffect(() => {
    api.me().then(setProfile).catch((e) => setError(e.message));
  }, []);

  if (error) return <div className="alert error">{error}</div>;
  if (!profile) return <div className="muted">Loading…</div>;

  const fmt = (iso) => (iso ? new Date(iso).toLocaleString() : '—');

  return (
    <div className="card">
      <h2>Profile</h2>
      <div className="profile-grid">
        <div className="stat">
          <div className="label">Username</div>
          <div className="value" style={{ fontSize: '1rem' }}>{profile.username}</div>
        </div>
        <div className="stat">
          <div className="label">Email</div>
          <div className="value" style={{ fontSize: '1rem' }}>{profile.email || '—'}</div>
        </div>
        <div className="stat">
          <div className="label">Tokens used</div>
          <div className="value">{profile.tokenUsage}</div>
        </div>
        <div className="stat">
          <div className="label">Requests</div>
          <div className="value">{profile.requestCount}</div>
        </div>
        <div className="stat">
          <div className="label">Joined</div>
          <div className="value" style={{ fontSize: '0.95rem' }}>{fmt(profile.createdAt)}</div>
        </div>
        <div className="stat">
          <div className="label">Last login</div>
          <div className="value" style={{ fontSize: '0.95rem' }}>{fmt(profile.lastLoginAt)}</div>
        </div>
      </div>
      <div style={{ marginTop: '1rem' }}>
        <button className="btn secondary" onClick={refresh}>Refresh</button>
      </div>
    </div>
  );
}
