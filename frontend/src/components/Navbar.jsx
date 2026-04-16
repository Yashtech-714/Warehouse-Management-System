import { useAuth } from '../context/AuthContext';

const ROLE_COLORS = {
  MANAGER:  '#6366f1',
  STAFF:    '#0ea5e9',
  CUSTOMER: '#10b981',
  SUPPLIER: '#f59e0b',
};

function Navbar({ title }) {
  const { user, logout } = useAuth();

  const accent = ROLE_COLORS[user?.role] ?? '#6366f1';
  const initials = user?.name
    ? user.name.split(' ').map((w) => w[0]).join('').slice(0, 2).toUpperCase()
    : '?';

  return (
    <header className="navbar">
      <h1 className="navbar-title">{title}</h1>
      <div className="navbar-user">
        <div className="user-avatar" style={{ background: accent }}>
          {initials}
        </div>
        <div className="user-info">
          <span className="user-name">{user?.name ?? 'Guest'}</span>
          <span className="user-role" style={{ color: accent }}>{user?.role}</span>
        </div>
        <button
          id="logout-btn"
          className="btn btn-logout"
          onClick={logout}
          title="Sign out"
        >
          ⏻ Logout
        </button>
      </div>
    </header>
  );
}

export default Navbar;
