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

export async function fetchRoles(query?: RoleQuery): Promise<RoleSummary[]> {
  const raw = await request<unknown[]>(
    '/api/v1/roles',
    undefined,
    query as Record<string, string | number | boolean | null | undefined>,
  );
  return raw.map((item) => {
    const source = asRecord(item);
    const permissionCodes = asStringList(source.permissionCodes || source.menuCodes);
    return {
      id: asNumber(source.id),
      code: asString(source.code),
      name: asString(source.name),
      menuCount: asNumber(source.menuCount || permissionCodes.length),
      userCount: asNumber(source.userCount),
      permissionCodes,
    };
  });
}
