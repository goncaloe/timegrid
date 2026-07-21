import { apiFetch } from '../../api/client';

export type TimeBankReason = 'WEEKEND' | 'HOLIDAY' | 'ABSENCE';

export interface TimeBankDay {
  date: string;
  expectedMinutes: number;
  workedMinutes: number;
  reason: TimeBankReason | null;
}

export interface TimeBankBalance {
  userId: string;
  from: string;
  to: string;
  expectedMinutes: number;
  workedMinutes: number;
  balanceMinutes: number;
  days: TimeBankDay[];
}

export function fetchTimeBankMe(from: string, to: string) {
  return apiFetch<TimeBankBalance>(`/time-bank/me?from=${from}&to=${to}`);
}