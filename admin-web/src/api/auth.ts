import { clearToken, request, setToken } from '@/api/http';

export interface AuthenticatedUser {
  userId: number;
  username: string;
  displayName: string;
  roleCodes: string[];
  storeIds: number[];
}

export interface MenuSummary {
  id: number;
  parentId: number | null;
  name: string;
  route: string;
  permissionCode: string;
  sortOrder: number;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  user: AuthenticatedUser;
  menus: MenuSummary[];
}

export async function login(payload: LoginRequest): Promise<LoginResponse> {
  const response = await request<LoginResponse>(
    '/api/v1/auth/login',
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
    undefined,
    { auth: false },
  );
  setToken(response.accessToken);
  return response;
}

export async function fetchCurrentUser(): Promise<AuthenticatedUser> {
  return request<AuthenticatedUser>('/api/v1/auth/me');
}

export async function fetchCurrentMenus(): Promise<MenuSummary[]> {
  return request<MenuSummary[]>('/api/v1/menus/current');
}

export async function logout(): Promise<void> {
  try {
    await request<void>(
      '/api/v1/auth/logout',
      {
        method: 'POST',
      },
    );
  } finally {
    clearToken();
  }
}
