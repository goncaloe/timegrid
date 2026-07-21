import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  cancelAbsence,
  createAbsence,
  deleteAbsence,
  fetchAbsenceHistory,
  fetchAbsences,
  submitAbsence,
  updateAbsence,
  type AbsenceRequestInput,
} from './api';

export function useAbsences(year: number) {
  return useQuery({
    queryKey: ['absences', year],
    queryFn: () => fetchAbsences(year),
  });
}

export function useAbsenceHistory(id: string | null) {
  return useQuery({
    queryKey: ['absences', id, 'history'],
    queryFn: () => fetchAbsenceHistory(id as string),
    enabled: id !== null,
  });
}

function useInvalidateAbsences() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: ['absences'] });
}

export function useCreateAbsence() {
  const invalidate = useInvalidateAbsences();
  return useMutation({
    mutationFn: (input: AbsenceRequestInput) => createAbsence(input),
    onSuccess: invalidate,
  });
}

export function useUpdateAbsence() {
  const invalidate = useInvalidateAbsences();
  return useMutation({
    mutationFn: ({ id, input }: { id: string; input: AbsenceRequestInput }) => updateAbsence(id, input),
    onSuccess: invalidate,
  });
}

export function useDeleteAbsence() {
  const invalidate = useInvalidateAbsences();
  return useMutation({
    mutationFn: (id: string) => deleteAbsence(id),
    onSuccess: invalidate,
  });
}

export function useSubmitAbsence() {
  const invalidate = useInvalidateAbsences();
  return useMutation({
    mutationFn: (id: string) => submitAbsence(id),
    onSuccess: invalidate,
  });
}

export function useCancelAbsence() {
  const invalidate = useInvalidateAbsences();
  return useMutation({
    mutationFn: (id: string) => cancelAbsence(id),
    onSuccess: invalidate,
  });
}