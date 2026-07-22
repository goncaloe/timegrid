import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchPendingAbsences, fetchPendingTimeEntries, fetchUserTimeBank } from './api';
import { approveTimeEntry, rejectTimeEntry } from '../timeEntries/api';
import { approveAbsence, rejectAbsence } from '../absences/api';

export function usePendingTimeEntries() {
  return useQuery({
    queryKey: ['time-entries', 'pending'],
    queryFn: fetchPendingTimeEntries,
  });
}

export function usePendingAbsences() {
  return useQuery({
    queryKey: ['absences', 'pending'],
    queryFn: fetchPendingAbsences,
  });
}

export function useUserTimeBank(userId: string | null, from: string, to: string) {
  return useQuery({
    queryKey: ['time-bank', 'users', userId],
    queryFn: () => fetchUserTimeBank(userId as string, from, to),
    enabled: userId !== null,
  });
}

function useInvalidatePending() {
  const queryClient = useQueryClient();
  return () => {
    queryClient.invalidateQueries({ queryKey: ['time-entries'] });
    queryClient.invalidateQueries({ queryKey: ['absences'] });
  };
}

export function useApproveTimeEntry() {
  const invalidate = useInvalidatePending();
  return useMutation({
    mutationFn: (id: string) => approveTimeEntry(id),
    onSuccess: invalidate,
  });
}

export function useRejectTimeEntry() {
  const invalidate = useInvalidatePending();
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => rejectTimeEntry(id, reason),
    onSuccess: invalidate,
  });
}

export function useApproveAbsence() {
  const invalidate = useInvalidatePending();
  return useMutation({
    mutationFn: (id: string) => approveAbsence(id),
    onSuccess: invalidate,
  });
}

export function useRejectAbsence() {
  const invalidate = useInvalidatePending();
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => rejectAbsence(id, reason),
    onSuccess: invalidate,
  });
}