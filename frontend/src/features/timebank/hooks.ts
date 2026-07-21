import { useQuery } from '@tanstack/react-query';
import { fetchTimeBankMe } from './api';

export function useTimeBankMe(from: string, to: string) {
  return useQuery({
    queryKey: ['time-bank', 'me', from, to],
    queryFn: () => fetchTimeBankMe(from, to),
  });
}