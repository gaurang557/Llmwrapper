import { useEffect, useMemo, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client.js';
import { useAuth } from '../api/auth.jsx';

const STORAGE_KEY = 'chat.thread';

export default function Chat() {
  const { user } = useAuth();
  const [providers, setProviders] = useState([]);
  const [provider, setProvider] = useState('');
  const [model, setModel] = useState('');
  const [systemPrompt, setSystemPrompt] = useState('');
  const [messages, setMessages] = useState(() => {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  });
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const messagesRef = useRef(null);

  useEffect(() => {
    api.providers()
      .then((list) => {
        setProviders(list);
        const firstUsable = list.find((p) => p.configured) || list[0];
        if (firstUsable) {
          setProvider(firstUsable.name);
          setModel(firstUsable.defaultModel || '');
        }
      })
      .catch((e) => setError(e.message));
  }, []);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(messages));
  }, [messages]);

  useEffect(() => {
    if (messagesRef.current) messagesRef.current.scrollTop = messagesRef.current.scrollHeight;
  }, [messages, loading]);

  const selectedProvider = useMemo(
    () => providers.find((p) => p.name === provider),
    [providers, provider],
  );

  const handleProviderChange = (e) => {
    const name = e.target.value;
    setProvider(name);
    const p = providers.find((x) => x.name === name);
    setModel(p?.defaultModel || '');
  };

  const send = async (e) => {
    e?.preventDefault();
    const text = input.trim();
    if (!text || loading) return;
    setError('');

    const newMessages = [...messages, { role: 'user', content: text }];
    setMessages(newMessages);
    setInput('');
    setLoading(true);

    const apiMessages = [];
    if (systemPrompt.trim()) apiMessages.push({ role: 'system', content: systemPrompt.trim() });
    for (const m of newMessages) apiMessages.push({ role: m.role, content: m.content });

    try {
      const res = await api.chat({
        provider,
        model: model || undefined,
        messages: apiMessages,
      });
      setMessages([
        ...newMessages,
        {
          role: 'assistant',
          content: res.content,
          meta: `${res.provider} · ${res.model} · ${res.totalTokens} tok`,
        },
      ]);
    } catch (err) {
      setError(err.message || 'request failed');
    } finally {
      setLoading(false);
    }
  };

  const clearThread = () => {
    setMessages([]);
    setError('');
  };

  return (
    <div className="chat-layout">
      <aside className="sidebar">
        <div className="card">
          <h3>Provider</h3>
          <div className="field">
            <select value={provider} onChange={handleProviderChange}>
              {providers.map((p) => (
                <option key={p.name} value={p.name} disabled={!p.configured}>
                  {p.name}{p.configured ? '' : ' (not configured)'}
                </option>
              ))}
            </select>
          </div>
          <div className="field">
            <label>Model</label>
            <input
              value={model}
              onChange={(e) => setModel(e.target.value)}
              placeholder={selectedProvider?.defaultModel || 'default'}
            />
          </div>
          {selectedProvider && (
            <div className="small muted">
              {user
                ? `signed in · ${selectedProvider.totalUsed}/${selectedProvider.totalLimit || '∞'} total`
                : `anonymous · ${selectedProvider.anonymousLimit} req/IP allowed`}
            </div>
          )}
        </div>

        <div className="card">
          <h3>System prompt</h3>
          <textarea
            value={systemPrompt}
            onChange={(e) => setSystemPrompt(e.target.value)}
            placeholder="Optional system instruction"
          />
        </div>

        {!user && (
          <div className="card">
            <div className="small">
              <Link to="/login">Sign in</Link> to lift anonymous limits.
            </div>
          </div>
        )}

        <button className="btn secondary" onClick={clearThread}>Clear conversation</button>
      </aside>

      <section className="chat-main">
        <div className="messages" ref={messagesRef}>
          {messages.length === 0 && (
            <div className="muted small" style={{ textAlign: 'center', margin: 'auto' }}>
              Start the conversation by typing below.
            </div>
          )}
          {messages.map((m, i) => (
            <div key={i} className={`message ${m.role}`}>
              <div className="role">{m.role}{m.meta ? ` · ${m.meta}` : ''}</div>
              {m.content}
            </div>
          ))}
          {loading && (
            <div className="message assistant">
              <div className="role">assistant</div>
              <span className="spinner" /> thinking…
            </div>
          )}
        </div>

        {error && <div className="alert error">{error}</div>}

        <form className="composer" onSubmit={send}>
          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Type your message…"
            onKeyDown={(e) => {
              if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) send(e);
            }}
          />
          <div className="actions">
            <span className="hint">Cmd/Ctrl+Enter to send</span>
            <button className="btn" disabled={loading || !input.trim()}>
              {loading ? <span className="spinner" /> : 'Send'}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}
