import { request } from '@/api/http';

export interface StoreSummary {
  id: number;
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

export async function fetchStores(): Promise<StoreSummary[]> {
  const raw = await request<unknown[]>('/api/v1/stores');
  return raw.map((item) => {
    const source = asRecord(item);
    return {
      id: asNumber(source.id),
      name: asString(source.name),
      countryCode: asString(source.countryCode),
      businessStatus: asString(source.businessStatus || source.status),
      businessTypes: asStringList(source.businessTypes || source.businessModes),
    };
  });
}
