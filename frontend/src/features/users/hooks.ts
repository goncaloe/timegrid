import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createUser,
  deleteUser,
  fetchUsers,
  restoreUser,
  updateUserDailyWorkMinutes,
  updateUserRoles,
  updateUserTeam,
  type CreateUserInput,
} from './api';

export function useUsers(page = 0, size = 20) {
  return useQuery({
    queryKey: ['users', page, size],
    queryFn: () => fetchUsers(page, size),
  });
}

function useInvalidateUsers() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: ['users'] });
}

export function useCreateUser() {
  const invalidate = useInvalidateUsers();
  return useMutation({
    mutationFn: (input: CreateUserInput) => createUser(input),
    onSuccess: invalidate,
  });
}

export function useDeleteUser() {
  const invalidate = useInvalidateUsers();
  return useMutation({
    mutationFn: (id: string) => deleteUser(id),
    onSuccess: invalidate,
  });
}

export function useRestoreUser() {
  const invalidate = useInvalidateUsers();
  return useMutation({
    mutationFn: (id: string) => restoreUser(id),
    onSuccess: invalidate,
  });
}

export function useUpdateUserRoles() {
  const invalidate = useInvalidateUsers();
  return useMutation({
    mutationFn: ({ id, roles }: { id: string; roles: string[] }) => updateUserRoles(id, roles),
    onSuccess: invalidate,
  });
}

export function useUpdateUserTeam() {
  const invalidate = useInvalidateUsers();
  return useMutation({
    mutationFn: ({ id, teamId }: { id: string; teamId: string | null }) => updateUserTeam(id, teamId),
    onSuccess: invalidate,
  });
}

export function useUpdateUserDailyWorkMinutes() {
  const invalidate = useInvalidateUsers();
  return useMutation({
    mutationFn: ({ id, dailyWorkMinutes }: { id: string; dailyWorkMinutes: number }) =>
      updateUserDailyWorkMinutes(id, dailyWorkMinutes),
    onSuccess: invalidate,
  });
}