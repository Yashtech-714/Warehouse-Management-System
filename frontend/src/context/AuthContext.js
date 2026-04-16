import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { login as apiLogin } from '../api/authApi';
import { getApiErrorMessage } from '../services/apiError';

const AuthContext = createContext({
  user: null,
  loading: false,
  error: '',
  login: async () => {},
  logout: () => {},
});

const USER_KEY = 'wms_user';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const stored = localStorage.getItem(USER_KEY);
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (user) {
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    } else {
      localStorage.removeItem(USER_KEY);
    }
  }, [user]);

  const login = async (email, password) => {
    setLoading(true);
    setError('');
    try {
      const data = await apiLogin({ email, password });
      setUser(data.user);
      return data.user;
    } catch (err) {
      const msg = getApiErrorMessage(err);
      setError(msg);
      throw new Error(msg);
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    setError('');
  };

  const value = useMemo(
    () => ({ user, loading, error, login, logout }),
    [user, loading, error]
  );

  // Use React.createElement to avoid JSX in a .js file
  return React.createElement(AuthContext.Provider, { value }, children);
}

export function useAuth() {
  return useContext(AuthContext);
}
