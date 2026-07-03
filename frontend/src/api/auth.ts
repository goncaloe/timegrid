import { apiFetch } from './client';

export interface AuthProfile {
  userId: string;
  username: string;
  fullName: string;
  organizationId: string;
  organizationName: string;
  roles: string[];
  permissions: string[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse extends AuthProfile {
  accessToken: string;
  refreshToken: string;
  expiresAt: string;
}

export type MeResponse = AuthProfile;

export function login(request: LoginRequest) {
  return apiFetch<AuthResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify(request),
    auth: false,
  });
}

export function me() {
  return apiFetch<MeResponse>('/auth/me');
}
