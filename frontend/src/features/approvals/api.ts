import { apiFetch } from '../../api/client';
import type { TimeEntry } from '../timeEntries/api';
import type { AbsenceRequest } from '../absences/api';
import type { TimeBankBalance } from '../timebank/api';

export function fetchPendingTimeEntries() {
  return apiFetch<TimeEntry[]>('/time-entries/pending');
}

export function fetchPendingAbsences() {
  return apiFetch<AbsenceRequest[]>('/absences/pending');
}

export function fetchUserTimeBank(userId: string, from: string, to: string) {
  return apiFetch<TimeBankBalance>(`/time-bank/users/${userId}?from=${from}&to=${to}`);
}