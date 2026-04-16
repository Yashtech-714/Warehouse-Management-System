import { useState } from 'react';
import { useAuth } from '../context/AuthContext';

const DEMO_CREDENTIALS = {
  MANAGER:  { email: 'manager@wms.com',  password: 'manager123' },
  STAFF:    { email: 'staff@wms.com',    password: 'staff123' },
  CUSTOMER: { email: 'customer@wms.com', password: 'customer123' },
  SUPPLIER: { email: 'supplier@wms.com', password: 'supplier123' },
};

const ROLES = [
  { key: 'MANAGER',  label: '🏭 Manager',  desc: 'Full warehouse access' },
  { key: 'STAFF',    label: '📦 Staff',    desc: 'Process & manage orders' },
  { key: 'CUSTOMER', label: '🛒 Customer', desc: 'Place and track orders' },
  { key: 'SUPPLIER', label: '🚚 Supplier', desc: 'Manage supply & stock' },
];

function Login() {
  const { login, loading, error } = useAuth();
  const [selectedRole, setSelectedRole] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [formError, setFormError] = useState('');

  const handleRoleSelect = (role) => {
    setSelectedRole(role);
    const creds = DEMO_CREDENTIALS[role];
    setEmail(creds.email);
    setPassword(creds.password);
    setFormError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    if (!email || !password) {
      setFormError('Email and password are required.');
      return;
    }
    try {
      await login(email, password);
    } catch (err) {
      setFormError(err.message);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-header">
          <div className="login-logo">WMS</div>
          <h1 className="login-title">Warehouse Management System</h1>
          <p className="login-subtitle">Sign in to your account</p>
        </div>

        {/* Role selector */}
        <div className="role-grid">
          {ROLES.map((r) => (
            <button
              key={r.key}
              type="button"
              className={`role-card ${selectedRole === r.key ? 'role-card--active' : ''}`}
              onClick={() => handleRoleSelect(r.key)}
            >
              <span className="role-label">{r.label}</span>
              <span className="role-desc">{r.desc}</span>
            </button>
          ))}
        </div>

        {/* Login form */}
        <form className="login-form" onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              placeholder="email@wms.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="username"
            />
          </div>
          <div className="field">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
          </div>

          {(formError || error) && (
            <div className="error-notice">{formError || error}</div>
          )}

          <button
            id="login-btn"
            type="submit"
            className="btn login-btn"
            disabled={loading}
          >
            {loading ? 'Signing in…' : 'Sign In'}
          </button>
        </form>

        <p className="login-hint">
          💡 Click a role above to auto-fill demo credentials
        </p>
      </div>
    </div>
  );
}

export default Login;
