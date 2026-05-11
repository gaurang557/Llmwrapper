import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../api/auth.jsx';

export default function Layout() {
  const { user, logout } = useAuth();

  return (
    <div className="app">
      <nav className="navbar">
        <span className="brand">LLM Wrapper</span>
        <div className="nav-links">
          <NavLink to="/" end className={({ isActive }) => (isActive ? 'active' : '')}>
            Chat
          </NavLink>
          <NavLink to="/providers" className={({ isActive }) => (isActive ? 'active' : '')}>
            Providers
          </NavLink>
          {user && (
            <NavLink to="/profile" className={({ isActive }) => (isActive ? 'active' : '')}>
              Profile
            </NavLink>
          )}
        </div>
        <div className="user-info">
          {user ? (
            <>
              <span>{user.username}</span>
              <button className="btn secondary" onClick={logout}>Sign out</button>
            </>
          ) : (
            <>
              <NavLink to="/login">Sign in</NavLink>
              <NavLink to="/signup" className="btn" style={{ color: 'white' }}>Sign up</NavLink>
            </>
          )}
        </div>
      </nav>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
