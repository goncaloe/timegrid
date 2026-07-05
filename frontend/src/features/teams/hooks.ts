import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { deleteTeam, fetchTeams, restoreTeam, type TeamInput } from './api';

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