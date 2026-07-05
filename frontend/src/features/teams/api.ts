import { apiFetch } from '../../api/client';

export interface Team {
  id: string;
  name: string;
  description: string | null;
  organizationId: string;
}

export function fetchTeams() {
  return apiFetch<Team[]>('/teams');
}

export function deleteTeam(id: string) {
  return apiFetch<void>(`/teams/${id}`, { method: 'DELETE' });
}

export function restoreTeam(id: string) {
  return apiFetch<Team>(`/teams/${id}/restore`, { method: 'POST' });
}