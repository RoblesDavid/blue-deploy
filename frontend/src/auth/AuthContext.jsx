import { createContext, useContext, useState, useEffect } from 'react';
import authApi from '../api/authApi';
import usersApi from '../api/usersApi';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('bluedeploy_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const handleUnauthorized = () => {
      setUser(null);
    };
    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
  }, []);

  async function login(username, password) {
    setLoading(true);
    try {
      const response = await authApi.login({ username, password });
      
      const safeUser = {
        id: response.userId,
        username: response.username,
        name: response.name,
        email: response.email,
        role: response.role,
        initials: response.initials,
        department: response.department,
      };

      if (response.token) {
        localStorage.setItem('bluedeploy_token', response.token);
      }
      localStorage.setItem('bluedeploy_user', JSON.stringify(safeUser));
      setUser(safeUser);

      return { success: true, user: safeUser };
    } catch (err) {
      return { 
        success: false, 
        error: err.message || 'Usuario o contraseña incorrectos' 
      };
    } finally {
      setLoading(false);
    }
  }

  function logout() {
    setUser(null);
    localStorage.removeItem('bluedeploy_token');
    localStorage.removeItem('bluedeploy_user');
  }

  function updateUser(updatedData) {
    setUser((prev) => {
      const next = { ...prev, ...updatedData };
      localStorage.setItem('bluedeploy_user', JSON.stringify(next));
      return next;
    });
  }

  async function refreshProfile() {
    try {
      const profile = await usersApi.getMe();
      updateUser(profile);
      return profile;
    } catch (err) {
      console.error('Error refreshing profile:', err);
    }
  }

  return (
    <AuthContext.Provider value={{ user, login, logout, updateUser, refreshProfile, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  return useContext(AuthContext);
}
