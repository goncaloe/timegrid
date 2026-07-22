import { apiFetch } from '../../api/client';

export interface Holiday {
  id: string | null;
  date: string;
  name: string;
  national: boolean;
}

export interface HolidayInput {
  date: string;
  name: string;
}

export function fetchHolidays(year: number) {
  return apiFetch<Holiday[]>(`/holidays?year=${year}`);
}

export function createHoliday(input: HolidayInput) {
  return apiFetch<Holiday>('/holidays', {
    method: 'POST',
    body: JSON.stringify(input),
  });
}

export function deleteHoliday(id: string) {
  return apiFetch<void>(`/holidays/${id}`, { method: 'DELETE' });
}