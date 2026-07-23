import { apiFetch } from '../../api/client';

export interface Team {
  id: string;
  name: string;
  description: string | null;
  organizationId: string;
}

export interface TeamInput {
  name: string;
  description: string;
}

export function fetchTeams() {
  return apiFetch<Team[]>('/teams');
}

export function createTeam(input: TeamInput) {
  return apiFetch<Team>('/teams', {
    method: 'POST',
    body: JSON.stringify(input),
  });
}

export function updateTeam(id: string, input: TeamInput) {
  return apiFetch<Team>(`/teams/${id}`, {
    method: 'PUT',
    body: JSON.stringify(input),
  });
}

export function deleteTeam(id: string) {
  return apiFetch<void>(`/teams/${id}`, { method: 'DELETE' });
}

export function restoreTeam(id: string) {
  return apiFetch<Team>(`/teams/${id}/restore`, { method: 'POST' });
}