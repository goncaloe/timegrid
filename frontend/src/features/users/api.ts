import { apiFetch } from '../../api/client';

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'LOCKED';

export interface User {
  id: string;
  username: string;
  email: string;
  fullName: string;
  status: UserStatus;
  organizationId: string;
  teamId: string | null;
  roles: string[];
  dailyWorkMinutes: number;
  createdAt: string;
}

export interface UserPage {
  content: User[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CreateUserInput {
  username: string;
  email: string;
  fullName: string;
  password: string;
  teamId: string | null;
}

export function fetchUsers(page = 0, size = 20) {
  return apiFetch<UserPage>(`/users?page=${page}&size=${size}`);
}

export function createUser(input: CreateUserInput) {
  return apiFetch<User>('/users', {
    method: 'POST',
    body: JSON.stringify(input),
  });
}

export function updateUserRoles(id: string, roles: string[]) {
  return apiFetch<User>(`/users/${id}/roles`, {
    method: 'PUT',
    body: JSON.stringify({ roles }),
  });
}

export function updateUserDailyWorkMinutes(id: string, dailyWorkMinutes: number) {
  return apiFetch<User>(`/users/${id}/daily-work-minutes`, {
    method: 'PUT',
    body: JSON.stringify({ dailyWorkMinutes }),
  });
}

export function updateUserTeam(id: string, teamId: string | null) {
  return apiFetch<User>(`/users/${id}/team`, {
    method: 'PUT',
    body: JSON.stringify({ teamId }),
  });
}

export function deleteUser(id: string) {
  return apiFetch<void>(`/users/${id}`, { method: 'DELETE' });
}

export function restoreUser(id: string) {
  return apiFetch<User>(`/users/${id}/restore`, { method: 'POST' });
}