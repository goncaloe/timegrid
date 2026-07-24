import type { ProblemDetail } from './types';
import { isJsonContentType } from '../utils/http';

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

export interface DownloadResult {
  blob: Blob;
  filename: string;
}

// download de ficheiros (exportações): devolve o blob e o nome que veio no
// Content-Disposition, com o mesmo refresh em 401 do apiFetch
function downloadWithToken(path: string): Promise<Response> {
  const headers = new Headers();
  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`);
  }
  return fetch(`/api${path}`, { headers });
}

async function parseFilename(response: Response, fallback: string): Promise<string> {
  const disposition = response.headers.get('content-disposition');
  const match = disposition?.match(/filename\*=UTF-8''([^;]+)/i);
  if (match) {
    return decodeURIComponent(match[1]);
  }
  const plainMatch = disposition?.match(/filename="?([^";]+)"?/i);
  return plainMatch ? plainMatch[1] : fallback;
}

export async function downloadFile(path: string): Promise<DownloadResult> {
  let response = await downloadWithToken(path);
  if (response.status === 401) {
    const refreshed = await refreshAccessToken();
    if (refreshed) {
      response = await downloadWithToken(path);
    } else {
      setAccessToken(null);
      setStoredRefreshToken(null);
      onSessionExpired?.();
    }
  }
  const contentType = response.headers.get('content-type') ?? '';
  if (!response.ok) {
    const body = isJsonContentType(contentType) ? await response.json() : undefined;
    throw new ApiError(response.status, body ?? {});
  }
  const blob = await response.blob();
  const filename = await parseFilename(response, 'timegrid-export');
  return { blob, filename };
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

  const contentType = response.headers.get('content-type') ?? '';
  const body = isJsonContentType(contentType) ? await response.json() : undefined;

  if (!response.ok) {
    throw new ApiError(response.status, (body as ProblemDetail) ?? {});
  }

  return body as T;
}