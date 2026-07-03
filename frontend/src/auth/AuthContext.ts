import { createContext } from 'react';
import type { AuthProfile, LoginRequest } from '../api/auth';

export interface AuthContextValue {
  user: AuthProfile | null;
  login: (credentials: LoginRequest) => Promise<void>;
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);
