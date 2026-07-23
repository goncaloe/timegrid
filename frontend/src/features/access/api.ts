import { apiFetch } from '../../api/client';

export interface Role {
  id: string;
  name: string;
}

export interface Permission {
  id: string;
  name: string;
  description: string;
}

export function fetchRoles() {
  return apiFetch<Role[]>('/roles');
}

export function fetchPermissions() {
  return apiFetch<Permission[]>('/permissions');
}