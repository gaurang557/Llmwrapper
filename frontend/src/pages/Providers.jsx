import { useEffect, useState } from 'react';
import { api } from '../api/client.js';

export default function Providers() {
  const [providers, setProviders] = useState([]);
  const [health, setHealth] = useState({});
  const [checking, setChecking] = useState({});
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const list = await api.providers();
      setProviders(list);
    } catch (e) {
      setError(e.message);
    }
  };

  useEffect(() => { load(); }, []);

  const ping = async (name) => {
    setChecking((c) => ({ ...c, [name]: true }));
    try {
      const status = await api.healthFor(name);
      setHealth((h) => ({ ...h, [name]: status }));
    } catch (e) {
      setHealth((h) => ({ ...h, [name]: { provider: name, reachable: false, detail: e.message } }));
    } finally {
      setChecking((c) => ({ ...c, [name]: false }));
    }
  };

  const pingAll = async () => {
    await Promise.all(providers.map((p) => ping(p.name)));
  };

  return (
    <div className="card">
      <div className="row" style={{ marginBottom: '1rem' }}>
        <h2 style={{ margin: 0, flex: 1 }}>Providers</h2>
        <button className="btn secondary" onClick={load}>Refresh</button>
        <button className="btn" onClick={pingAll} disabled={providers.length === 0}>Sanity check all</button>
      </div>
      {error && <div className="alert error">{error}</div>}
      <div className="providers-list">
        {providers.map((p) => {
          const h = health[p.name];
          let dotClass = 'warn';
          let dotLabel = 'unchecked';
          if (h) {
            dotClass = h.reachable ? 'ok' : 'err';
            dotLabel = h.reachable ? `reachable · ${h.latencyMs}ms` : `unreachable: ${h.detail}`;
          } else if (!p.configured) {
            dotClass = 'err';
            dotLabel = 'no api key configured';
          }
          return (
            <div key={p.name} className="provider-row">
              <div>
                <div>
                  <span className={`dot ${dotClass}`} />
                  <strong>{p.name}</strong>{' '}
                  <span className="muted small">· {p.defaultModel}</span>
                </div>
                <div className="meta">
                  {p.configured ? 'configured' : 'not configured'} ·
                  {' '}anon limit {p.anonymousLimit} ·
                  {' '}total {p.totalUsed}/{p.totalLimit || '∞'} ·
                  {' '}{dotLabel}
                </div>
              </div>
              <button
                className="btn secondary"
                onClick={() => ping(p.name)}
                disabled={!p.configured || checking[p.name]}
              >
                {checking[p.name] ? <span className="spinner" /> : 'Check'}
              </button>
            </div>
          );
        })}
        {providers.length === 0 && !error && <div className="muted small">No providers configured.</div>}
      </div>
    </div>
  );
}
