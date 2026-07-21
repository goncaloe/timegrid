import { useQuery } from '@tanstack/react-query';
import { fetchHolidays } from './api';

export function useHolidays(year: number) {
  return useQuery({
    queryKey: ['holidays', year],
    queryFn: () => fetchHolidays(year),
  });
}