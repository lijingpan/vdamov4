import { request } from '@/api/http';

export interface UserQuery {
  keyword?: string;
  enabled?: boolean;
  storeId?: number;
}

export interface UserSummary {
  id: number;
  username: string;
  displayName: string;
  enabled: boolean;
  roleCodes: string[];
  storeIds: number[];
  storeNames: string[];
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

function asBoolean(value: unknown): boolean {
  if (typeof value === 'boolean') {
    return value;
  }
  if (typeof value === 'number') {
    return value !== 0;
  }
  if (typeof value === 'string') {
    const normalized = value.toUpperCase();
    return normalized === 'TRUE' || normalized === 'ENABLED' || normalized === 'ENABLE' || normalized === '1';
  }
  return false;
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

export async function fetchUsers(query?: UserQuery): Promise<UserSummary[]> {
  const raw = await request<unknown[]>(
    '/api/v1/users',
    undefined,
    query as Record<string, string | number | boolean | null | undefined>,
  );
  return raw.map((item) => {
    const source = asRecord(item);
    return {
      id: asNumber(source.id),
      username: asString(source.username),
      displayName: asString(source.displayName || source.name),
      enabled: asBoolean(source.enabled || source.status),
      roleCodes: asStringList(source.roleCodes),
      storeIds: asNumberList(source.storeIds),
      storeNames: asStringList(source.storeNames),
    };
  });
}
