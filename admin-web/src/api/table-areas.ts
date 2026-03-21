import { request } from '@/api/http';

export interface TableAreaQuery {
  storeId?: number;
  keyword?: string;
  enabled?: boolean;
}

export interface TableAreaSummary {
  id: number;
  storeId: number;
  storeName: string;
  areaName: string;
  areaCode: string;
  sortOrder: number;
  enabled: boolean;
  tableCount: number;
}

export interface TableAreaPayload {
  storeId: number;
  areaName: string;
  areaCode: string;
  sortOrder: number;
  enabled: boolean;
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
    return normalized === 'TRUE' || normalized === 'ENABLE' || normalized === 'ENABLED' || normalized === 'ACTIVE' || normalized === '1';
  }
  return false;
}

function toTableAreaSummary(item: unknown): TableAreaSummary {
  const source = asRecord(item);
  return {
    id: asNumber(source.id),
    storeId: asNumber(source.storeId),
    storeName: asString(source.storeName),
    areaName: asString(source.areaName || source.name),
    areaCode: asString(source.areaCode || source.code),
    sortOrder: asNumber(source.sortOrder),
    enabled: asBoolean(source.enabled || source.status),
    tableCount: asNumber(source.tableCount || source.tablesCount),
  };
}

export async function fetchTableAreas(query: TableAreaQuery): Promise<TableAreaSummary[]> {
  const raw = await request<unknown[]>(
    '/api/v1/table-areas',
    undefined,
    query as Record<string, string | number | boolean | null | undefined>,
  );
  return raw.map(toTableAreaSummary);
}

export async function createTableArea(payload: TableAreaPayload): Promise<TableAreaSummary> {
  const raw = await request<unknown>('/api/v1/table-areas', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
  return toTableAreaSummary(raw);
}

export async function updateTableArea(id: number, payload: TableAreaPayload): Promise<TableAreaSummary> {
  const raw = await request<unknown>(`/api/v1/table-areas/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
  return toTableAreaSummary(raw);
}

export async function updateTableAreaEnabled(id: number, enabled: boolean): Promise<TableAreaSummary> {
  const raw = await request<unknown>(`/api/v1/table-areas/${id}/enabled`, {
    method: 'PATCH',
    body: JSON.stringify({ enabled }),
  });
  return toTableAreaSummary(raw);
}

export async function deleteTableArea(id: number): Promise<void> {
  await request<void>(`/api/v1/table-areas/${id}`, {
    method: 'DELETE',
  });
}
