import { request } from '@/api/http';

export interface StoreQuery {
  keyword?: string;
  status?: string;
}

export interface StoreSummary {
  id: number;
  name: string;
  countryCode: string;
  businessStatus: string;
  businessTypes: string[];
}

export interface StorePayload {
  name: string;
  countryCode: string;
  businessStatus: string;
  businessTypes: string[];
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

function toStoreSummary(item: unknown): StoreSummary {
  const source = asRecord(item);
  return {
    id: asNumber(source.id),
    name: asString(source.name),
    countryCode: asString(source.countryCode),
    businessStatus: asString(source.businessStatus || source.status),
    businessTypes: asStringList(source.businessTypes || source.businessModes),
  };
}

export async function fetchStores(query?: StoreQuery): Promise<StoreSummary[]> {
  const raw = await request<unknown[]>(
    '/api/v1/stores',
    undefined,
    query as Record<string, string | number | boolean | null | undefined> | undefined,
  );
  return raw.map(toStoreSummary);
}

export async function createStore(payload: StorePayload): Promise<StoreSummary> {
  const raw = await request<unknown>('/api/v1/stores', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
  return toStoreSummary(raw);
}

export async function updateStore(id: number, payload: StorePayload): Promise<StoreSummary> {
  const raw = await request<unknown>(`/api/v1/stores/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
  return toStoreSummary(raw);
}

export async function updateStoreStatus(id: number, businessStatus: string): Promise<StoreSummary> {
  const raw = await request<unknown>(`/api/v1/stores/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ businessStatus }),
  });
  return toStoreSummary(raw);
}
