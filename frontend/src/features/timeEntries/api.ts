import { apiFetch } from '../../api/client';
import type { StatusChange } from '../../api/workflow';

export type TimeEntryType = 'OFFICE' | 'REMOTE' | 'ON_SITE' | 'OVERTIME';
export type TimeEntryStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED';

export interface TimeEntry {
  id: string;
  userId: string;
  userFullName: string;
  workDate: string;
  startTime: string;
  endTime: string;
  breakMinutes: number;
  durationMinutes: number;
  type: TimeEntryType;
  notes: string | null;
  status: TimeEntryStatus;
  rejectionReason: string | null;
  approverId: string | null;
  approvedAt: string | null;
  createdAt: string;
  allowedActions: string[];
}

export interface TimeEntryInput {
  workDate: string;
  startTime: string;
  endTime: string;
  breakMinutes: number;
  type: TimeEntryType;
  notes: string;
}

export function fetchTimeEntries(from: string, to: string) {
  return apiFetch<TimeEntry[]>(`/time-entries?from=${from}&to=${to}`);
}

export function fetchTimeEntryHistory(id: string) {
  return apiFetch<StatusChange[]>(`/time-entries/${id}/history`);
}

export function createTimeEntry(input: TimeEntryInput) {
  return apiFetch<TimeEntry>('/time-entries', {
    method: 'POST',
    body: JSON.stringify(input),
  });
}

export function updateTimeEntry(id: string, input: TimeEntryInput) {
  return apiFetch<TimeEntry>(`/time-entries/${id}`, {
    method: 'PUT',
    body: JSON.stringify(input),
  });
}

export function deleteTimeEntry(id: string) {
  return apiFetch<void>(`/time-entries/${id}`, { method: 'DELETE' });
}

export function submitTimeEntry(id: string) {
  return apiFetch<TimeEntry>(`/time-entries/${id}/submit`, { method: 'POST' });
}

export function approveTimeEntry(id: string) {
  return apiFetch<TimeEntry>(`/time-entries/${id}/approve`, { method: 'POST' });
}

export function rejectTimeEntry(id: string, reason: string) {
  return apiFetch<TimeEntry>(`/time-entries/${id}/reject`, {
    method: 'POST',
    body: JSON.stringify({ reason }),
  });
}