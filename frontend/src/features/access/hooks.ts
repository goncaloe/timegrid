import { useQuery } from '@tanstack/react-query';
import { fetchPermissions, fetchRoles } from './api';

export function useRoles() {
  return useQuery({
    queryKey: ['roles'],
    queryFn: fetchRoles,
  });
}

export function usePermissions() {
  return useQuery({
    queryKey: ['permissions'],
    queryFn: fetchPermissions,
  });
}