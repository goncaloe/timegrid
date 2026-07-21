import { apiFetch } from '../../api/client';

export interface Holiday {
  id: string | null;
  date: string;
  name: string;
  national: boolean;
}

export function fetchHolidays(year: number) {
  return apiFetch<Holiday[]>(`/holidays?year=${year}`);
}