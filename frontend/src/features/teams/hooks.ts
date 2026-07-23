import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createTeam, deleteTeam, fetchTeams, restoreTeam, updateTeam, type TeamInput } from './api';

export function useTeams() {
  return useQuery({
    queryKey: ['teams'],
    queryFn: fetchTeams,
  });
}

function useInvalidateTeams() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: ['teams'] });
}

export function useCreateTeam() {
  const invalidate = useInvalidateTeams();
  return useMutation({
    mutationFn: (input: TeamInput) => createTeam(input),
    onSuccess: invalidate,
  });
}

export function useUpdateTeam() {
  const invalidate = useInvalidateTeams();
  return useMutation({
    mutationFn: ({ id, input }: { id: string; input: TeamInput }) => updateTeam(id, input),
    onSuccess: invalidate,
  });
}

export function useDeleteTeam() {
  const invalidate = useInvalidateTeams();
  return useMutation({
    mutationFn: (id: string) => deleteTeam(id),
    onSuccess: invalidate,
  });
}

export function useRestoreTeam() {
  const invalidate = useInvalidateTeams();
  return useMutation({
    mutationFn: (id: string) => restoreTeam(id),
    onSuccess: invalidate,
  });
}