const API_BASE_URL = import.meta.env.VITE_API_BASSE_URL ?? 'http://localhost:7081';
const TOKEN_STORAGE_KEY = 'ordering-admin-token';
const LOCALE_STORAGE_KEY = 'ordering-admin-locale';

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export class ApiError extends Error {
  status: number;

  constructor(message: string, status = 500) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_STORAGE_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_STORAGE_KEY);
}

export function getLocale(): string {
  return localStorage.getItem(LOCALE_STORAGE_KEY) ?? 'zh-CN';
}

export function toQueryString(
  query?: Record<string, string | number | boolean | null | undefined>,
): string {
  if (!query) {
    return '';
  }
  const search = new URLSearchParams();
  Object.entries(query).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return;
    }
    search.append(key, String(value));
  });
  const value = search.toString();
  return value ? `?${value}` : '';
}

export async function request<T>(
  path: string,
  init?: RequestInit,
  query?: Record<string, string | number | boolean | null | undefined>,
  options?: {
    auth?: boolean;
  },
): Promise<T> {
  const headers = new Headers(init?.headers);
  headers.set('Accept-Language', getLocale());
  if (!(init?.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  const requiresAuth = options?.auth !== false;
  const token = getToken();
  if (requiresAuth) {
    if (!token) {
      throw new ApiError('Unauthorized', 401);
    }
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}${toQueryString(query)}`, {
    ...init,
    headers,
  });

  if (response.status === 401) {
    clearToken();
  }

  let payload: ApiResponse<T> | null = null;
  try {
    payload = (await response.json()) as ApiResponse<T>;
  } catch {
    payload = null;
  }

  if (!response.ok || !payload || payload.code !== 0) {
    throw new ApiError(payload?.message || 'Request failed', response.status);
  }
  return payload.data;
}
