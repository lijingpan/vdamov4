import { request } from '@/api/http';

export interface MenuQuery {
  keyword?: string;
}

export interface MenuSummary {
  id: number;
  parentId: number | null;
  parentName: string;
  name: string;
  route: string;
  permissionCode: string;
  sortOrder: number;
}

export interface MenuPayload {
  parentId?: number | null;
  name: string;
  route?: string;
  permissionCode: string;
  sortOrder: number;
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

function asNullableNumber(value: unknown): number | null {
  return typeof value === 'number' ? value : null;
}

export async function fetchMenus(query?: MenuQuery): Promise<MenuSummary[]> {
  const raw = await request<unknown[]>(
    '/api/v1/menus',
    undefined,
    query as Record<string, string | number | boolean | null | undefined>,
  );
  return raw.map(toMenuSummary);
}

function toMenuSummary(item: unknown): MenuSummary {
  const source = asRecord(item);
  return {
    id: asNumber(source.id),
    parentId: asNullableNumber(source.parentId),
    parentName: asString(source.parentName),
    name: asString(source.name),
    route: asString(source.route),
    permissionCode: asString(source.permissionCode),
    sortOrder: asNumber(source.sortOrder),
  };
}

export async function createMenu(payload: MenuPayload): Promise<MenuSummary> {
  const raw = await request<unknown>('/api/v1/menus', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
  return toMenuSummary(raw);
}

export async function updateMenu(id: number, payload: MenuPayload): Promise<MenuSummary> {
  const raw = await request<unknown>(`/api/v1/menus/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
  return toMenuSummary(raw);
}
