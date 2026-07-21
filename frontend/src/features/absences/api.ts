import { apiFetch } from '../../api/client';
import type { StatusChange } from '../../api/workflow';

export type AbsenceType = 'VACATION' | 'SICK' | 'PERSONAL' | 'TRAINING' | 'OTHER';
export type AbsenceStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export interface AbsenceRequest {
  id: string;
  userId: string;
  userFullName: string;
  startDate: string;
  endDate: string;
  workingDays: number;
  type: AbsenceType;
  notes: string | null;
  status: AbsenceStatus;
  rejectionReason: string | null;
  approverId: string | null;
  approvedAt: string | null;
  createdAt: string;
  allowedActions: string[];
}

export interface AbsenceRequestInput {
  startDate: string;
  endDate: string;
  type: AbsenceType;
  notes: string;
}

export function fetchAbsences(year?: number) {
  const query = year ? `?year=${year}` : '';
  return apiFetch<AbsenceRequest[]>(`/absences${query}`);
}

export function fetchAbsenceHistory(id: string) {
  return apiFetch<StatusChange[]>(`/absences/${id}/history`);
}

export function createAbsence(input: AbsenceRequestInput) {
  return apiFetch<AbsenceRequest>('/absences', {
    method: 'POST',
    body: JSON.stringify(input),
  });
}

export function updateAbsence(id: string, input: AbsenceRequestInput) {
  return apiFetch<AbsenceRequest>(`/absences/${id}`, {
    method: 'PUT',
    body: JSON.stringify(input),
  });
}

export function deleteAbsence(id: string) {
  return apiFetch<void>(`/absences/${id}`, { method: 'DELETE' });
}

export function submitAbsence(id: string) {
  return apiFetch<AbsenceRequest>(`/absences/${id}/submit`, { method: 'POST' });
}

export function cancelAbsence(id: string) {
  return apiFetch<AbsenceRequest>(`/absences/${id}/cancel`, { method: 'POST' });
}