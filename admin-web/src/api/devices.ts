import { request } from '@/api/http';

export interface DeviceQuery {
  storeId?: number;
  type?: string;
  enabled?: boolean;
  keyword?: string;
}

export interface DeviceSummary {
  id: number;
  storeId: number;
  storeName: string;
  name: string;
  type: string;
  purpose: string;
  brand: string;
  sn: string;
  size: string;
  onlineStatus: string;
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
    return normalized === 'TRUE' || normalized === 'ENABLE' || normalized === 'ENABLED' || normalized === '1';
  }
  return false;
}

export async function fetchDevices(query: DeviceQuery): Promise<DeviceSummary[]> {
  const raw = await request<unknown[]>(
    '/api/v1/devices',
    undefined,
    query as Record<string, string | number | boolean | null | undefined>,
  );
  return raw.map((item) => {
    const source = asRecord(item);
    return {
      id: asNumber(source.id),
      storeId: asNumber(source.storeId),
      storeName: asString(source.storeName),
      name: asString(source.name),
      type: asString(source.type || source.deviceType),
      purpose: asString(source.purpose),
      brand: asString(source.brand),
      sn: asString(source.sn || source.serialNo),
      size: asString(source.size),
      onlineStatus: asString(source.onlineStatus),
      enabled: asBoolean(source.enabled || source.isEnabled || source.status),
    };
  });
}
