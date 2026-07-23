import { apiFetch } from '../../api/client';

export type OrganizationStatus = 'ACTIVE' | 'INACTIVE';

export interface Organization {
  id: string;
  name: string;
  timezone: string;
  status: OrganizationStatus;
  createdAt: string;
}

export interface UpdateOrganizationInput {
  name: string;
  timezone: string;
}

export function fetchOrganization() {
  return apiFetch<Organization[]>('/organizations');
}

export function updateOrganization(id: string, input: UpdateOrganizationInput) {
  return apiFetch<Organization>(`/organizations/${id}`, {
    method: 'PUT',
    body: JSON.stringify({ ...input, status: 'ACTIVE' }),
  });
}