import { useEffect, useState, type ReactNode } from 'react';
import {
  getStoredRefreshToken,
  setAccessToken,
  setSessionExpiredHandler,
  setStoredRefreshToken,
} from '../api/client';
import {
  login as loginRequest,
  logout as logoutRequest,
  me,
  refresh as refreshRequest,
  type AuthProfile,
  type LoginRequest,
} from '../api/auth';
import { AuthContext } from './AuthContext';

function toProfile(response: AuthProfile): AuthProfile {
  const { userId, username, fullName, organizationId, organizationName, roles, permissions } = response;
  return { userId, username, fullName, organizationId, organizationName, roles, permissions };
}

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<AuthProfile | null>(null);
  const [isLoading, setIsLoading] = useState(() => getStoredRefreshToken() !== null);

  useEffect(() => {
    setSessionExpiredHandler(() => setUser(null));
    return () => setSessionExpiredHandler(null);
  }, []);

  useEffect(() => {
    const storedRefreshToken = getStoredRefreshToken();
    if (!storedRefreshToken) {
      return;
    }
    refreshRequest(storedRefreshToken)
      .then((response) => {
        setAccessToken(response.accessToken);
        setStoredRefreshToken(response.refreshToken);
        return me();
      })
      .then((profile) => setUser(toProfile(profile)))
      .catch(() => {
        setAccessToken(null);
        setStoredRefreshToken(null);
      })
      .finally(() => setIsLoading(false));
  }, []);

  async function login(credentials: LoginRequest) {
    const response = await loginRequest(credentials);
    setAccessToken(response.accessToken);
    setStoredRefreshToken(response.refreshToken);
    setUser(toProfile(response));
  }

  async function logout() {
    const storedRefreshToken = getStoredRefreshToken();
    if (storedRefreshToken) {
      await logoutRequest(storedRefreshToken).catch(() => undefined);
    }
    setAccessToken(null);
    setStoredRefreshToken(null);
    setUser(null);
  }

  return <AuthContext.Provider value={{ user, isLoading, login, logout }}>{children}</AuthContext.Provider>;
}
