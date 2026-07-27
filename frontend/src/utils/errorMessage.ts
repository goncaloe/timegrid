import { ApiError } from '../api/client';

const FALLBACK = 'Não foi possível carregar os dados. Tenta novamente.';

export function errorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return error.problem.detail ?? error.problem.title ?? FALLBACK;
  }
  return FALLBACK;
}