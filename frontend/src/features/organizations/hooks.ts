import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchOrganization, updateOrganization, type UpdateOrganizationInput } from './api';

export function useOrganization() {
  return useQuery({
    queryKey: ['organization'],
    queryFn: fetchOrganization,
    select: (organizations) => organizations[0],
  });
}

export function useUpdateOrganization() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, input }: { id: string; input: UpdateOrganizationInput }) => updateOrganization(id, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['organization'] }),
  });
}