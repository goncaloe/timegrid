import type { ProblemDetail } from './types';

export class ApiError extends Error {
  readonly status: number;
  readonly problem: ProblemDetail;

  constructor(status: number, problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? `erro ${status}`);
    this.status = status;
    this.problem = problem;
  }
}

const REFRESH_TOKEN_KEY = 'timegrid.refreshToken';

let accessToken: string | null = null;

export function setAccessToken(token: string | null) {
  accessToken = token;
}

export function getAccessToken() {
  return accessToken;
}

export function getStoredRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function setStoredRefreshToken(token: string | null) {
  if (token) {
    localStorage.setItem(REFRESH_TOKEN_KEY, token);
  } else {
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }
}

let onSessionExpired: (() => void) | null = null;

export function setSessionExpiredHandler(handler: (() => void) | null) {
  onSessionExpired = handler;
}

let refreshPromise: Promise<boolean> | null = null;

function refreshAccessToken(): Promise<boolean> {
  const refreshToken = getStoredRefreshToken();
  if (!refreshToken) {
    return Promise.resolve(false);
  }
  if (!refreshPromise) {
    refreshPromise = fetch('/api/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    })
      .then(async (response) => {
        if (!response.ok) {
          return false;
        }
        const body = await response.json();
        setAccessToken(body.accessToken);
        setStoredRefreshToken(body.refreshToken);
        return true;
      })
      .catch(() => false)
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

interface RequestOptions extends RequestInit {
  auth?: boolean;
}

export async function apiFetch<T>(path: string, options: RequestOptions = {}, retryOn401 = true): Promise<T> {
  const { auth = true, headers, ...rest } = options;
  const finalHeaders = new Headers(headers);
  if (rest.body && !finalHeaders.has('Content-Type')) {
    finalHeaders.set('Content-Type', 'application/json');
  }
  if (auth && accessToken) {
    finalHeaders.set('Authorization', `Bearer ${accessToken}`);
  }

  const response = await fetch(`/api${path}`, { ...rest, headers: finalHeaders });

  if (auth && response.status === 401 && retryOn401) {
    const refreshed = await refreshAccessToken();
    if (refreshed) {
      return apiFetch<T>(path, options, false);
    }
    setAccessToken(null);
    setStoredRefreshToken(null);
    onSessionExpired?.();
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const isJson = response.headers.get('content-type')?.includes('application/json');
  const body = isJson ? await response.json() : undefined;

  if (!response.ok) {
    throw new ApiError(response.status, (body as ProblemDetail) ?? {});
  }

  return body as T;
}
