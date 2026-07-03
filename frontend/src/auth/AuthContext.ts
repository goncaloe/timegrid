import { createContext } from 'react';
import type { AuthProfile, LoginRequest } from '../api/auth';

export interface AuthContextValue {
  user: AuthProfile | null;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);
