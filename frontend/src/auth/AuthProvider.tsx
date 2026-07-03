import { useState, type ReactNode } from 'react';
import { setAccessToken } from '../api/client';
import { login as loginRequest, type AuthProfile, type LoginRequest } from '../api/auth';
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

  async function login(credentials: LoginRequest) {
    const response = await loginRequest(credentials);
    setAccessToken(response.accessToken);
    setUser(toProfile(response));
  }

  return <AuthContext.Provider value={{ user, login }}>{children}</AuthContext.Provider>;
}
