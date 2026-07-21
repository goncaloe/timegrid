import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createTimeEntry,
  deleteTimeEntry,
  fetchTimeEntries,
  fetchTimeEntryHistory,
  submitTimeEntry,
  updateTimeEntry,
  type TimeEntryInput,
} from './api';

const listKey = (from: string, to: string) => ['time-entries', from, to] as const;

export function useTimeEntries(from: string, to: string) {
  return useQuery({
    queryKey: listKey(from, to),
    queryFn: () => fetchTimeEntries(from, to),
  });
}

export function useTimeEntryHistory(id: string | null) {
  return useQuery({
    queryKey: ['time-entries', id, 'history'],
    queryFn: () => fetchTimeEntryHistory(id as string),
    enabled: id !== null,
  });
}

function useInvalidateTimeEntries() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: ['time-entries'] });
}

export function useCreateTimeEntry() {
  const invalidate = useInvalidateTimeEntries();
  return useMutation({
    mutationFn: (input: TimeEntryInput) => createTimeEntry(input),
    onSuccess: invalidate,
  });
}

export function useUpdateTimeEntry() {
  const invalidate = useInvalidateTimeEntries();
  return useMutation({
    mutationFn: ({ id, input }: { id: string; input: TimeEntryInput }) => updateTimeEntry(id, input),
    onSuccess: invalidate,
  });
}

export function useDeleteTimeEntry() {
  const invalidate = useInvalidateTimeEntries();
  return useMutation({
    mutationFn: (id: string) => deleteTimeEntry(id),
    onSuccess: invalidate,
  });
}

export function useSubmitTimeEntry() {
  const invalidate = useInvalidateTimeEntries();
  return useMutation({
    mutationFn: (id: string) => submitTimeEntry(id),
    onSuccess: invalidate,
  });
}