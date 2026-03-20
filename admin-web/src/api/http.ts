const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';
const TOKEN_STORAGE_KEY = 'ordering-admin-token';
const LOCALE_STORAGE_KEY = 'ordering-admin-locale';

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

interface LoginData {
  token: string;
}

let loginTask: Promise<void> | null = null;

function getToken(): string | null {
  return localStorage.getItem(TOKEN_STORAGE_KEY);
}

function setToken(token: string): void {
  localStorage.setItem(TOKEN_STORAGE_KEY, token);
}

function getLocale(): string {
  return localStorage.getItem(LOCALE_STORAGE_KEY) ?? 'zh-CN';
}

async function doLogin(): Promise<void> {
  const username = import.meta.env.VITE_DEMO_USERNAME ?? 'admin';
  const password = import.meta.env.VITE_DEMO_PASSWORD ?? 'admin123';
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept-Language': getLocale(),
    },
    body: JSON.stringify({ username, password }),
  });
  const payload = (await response.json()) as ApiResponse<LoginData>;
  if (!response.ok || payload.code !== 0 || !payload.data?.token) {
    throw new Error(payload.message || 'Login failed');
  }
  setToken(payload.data.token);
}

async function ensureAuthToken(): Promise<void> {
  if (getToken()) {
    return;
  }
  if (!loginTask) {
    loginTask = doLogin().finally(() => {
      loginTask = null;
    });
  }
  await loginTask;
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
): Promise<T> {
  await ensureAuthToken();

  const headers = new Headers(init?.headers);
  headers.set('Accept-Language', getLocale());
  headers.set('Content-Type', 'application/json');

  const token = getToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}${toQueryString(query)}`, {
    ...init,
    headers,
  });

  if (response.status === 401) {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    await ensureAuthToken();
    return request<T>(path, init, query);
  }

  const payload = (await response.json()) as ApiResponse<T>;
  if (!response.ok || payload.code !== 0) {
    throw new Error(payload.message || 'Request failed');
  }
  return payload.data;
}
