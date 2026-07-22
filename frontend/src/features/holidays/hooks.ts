import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createHoliday, deleteHoliday, fetchHolidays, type HolidayInput } from './api';

export function useHolidays(year: number) {
  return useQuery({
    queryKey: ['holidays', year],
    queryFn: () => fetchHolidays(year),
  });
}

export function useCreateHoliday() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: HolidayInput) => createHoliday(input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['holidays'] }),
  });
}

export function useDeleteHoliday() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteHoliday(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['holidays'] }),
  });
}