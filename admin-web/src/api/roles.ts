import { request } from '@/api/http';

export interface RoleQuery {
  keyword?: string;
}

export interface RoleSummary {
  id: number;
  code: string;
  name: string;
  menuCount: number;
  userCount: number;
  permissionCodes: string[];
  menuIds: number[];
}

export interface RolePayload {
  code: string;
  name: string;
  menuIds: number[];
}

function asRecord(value: unknown): Record<string, unknown> {
  if (!value || typeof value !== 'object') {
    return {};
  }
  return value as Record<string, unknown>;
}

function asString(value: unknown): string {
  return typeof value === 'string' ? value : '';
}

function asNumber(value: unknown): number {
  return typeof value === 'number' ? value : 0;
}

function asStringList(value: unknown): string[] {
  if (!Array.isArray(value)) {
    return [];
  }
  return value.filter((item): item is string => typeof item === 'string');
}

function asNumberList(value: unknown): number[] {
  if (!Array.isArray(value)) {
    return [];
  }
  return value.filter((item): item is number => typeof item === 'number');
}

function toRoleSummary(item: unknown): RoleSummary {
  const source = asRecord(item);
  const permissionCodes = asStringList(source.permissionCodes || source.menuCodes);
  const menuIds = asNumberList(source.menuIds);
  return {
    id: asNumber(source.id),
    code: asString(source.code),
    name: asString(source.name),
    menuCount: asNumber(source.menuCount || menuIds.length || permissionCodes.length),
    userCount: asNumber(source.userCount),
    permissionCodes,
    menuIds,
  };
}

export async function fetchRoles(query?: RoleQuery): Promise<RoleSummary[]> {
  const raw = await request<unknown[]>(
    '/api/v1/roles',
    undefined,
    query as Record<string, string | number | boolean | null | undefined>,
  );
  return raw.map(toRoleSummary);
}

export async function createRole(payload: RolePayload): Promise<RoleSummary> {
  const raw = await request<unknown>('/api/v1/roles', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
  return toRoleSummary(raw);
}

export async function updateRole(id: number, payload: RolePayload): Promise<RoleSummary> {
  const raw = await request<unknown>(`/api/v1/roles/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
  return toRoleSummary(raw);
}
