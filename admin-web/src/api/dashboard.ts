import { request } from '@/api/http';

export interface DashboardSummary {
  storeCount: number;
  productCount: number;
  memberCount: number;
  deviceCount: number;
  tableCount: number;
  activeOrderCount: number;
  todayOrderCount: number;
  tableStatusStats: Record<string, number>;
}

function asRecord(value: unknown): Record<string, unknown> {
  if (!value || typeof value !== 'object') {
    return {};
  }
  return value as Record<string, unknown>;
}

function asNumber(value: unknown): number {
  return typeof value === 'number' ? value : 0;
}

function asStats(value: unknown): Record<string, number> {
  if (!value || typeof value !== 'object') {
    return {};
  }
  const source = value as Record<string, unknown>;
  const result: Record<string, number> = {};
  Object.entries(source).forEach(([key, raw]) => {
    if (typeof raw === 'number') {
      const normalizedKey = String(key).toUpperCase();
      if (normalizedKey === 'AVAILABLE') {
        result.IDLE = raw;
        return;
      }
      if (normalizedKey === 'OCCUPIED') {
        result.IN_USE = raw;
        return;
      }
      if (normalizedKey === 'CHECKOUT') {
        result.WAITING_CHECKOUT = raw;
        return;
      }
      if (normalizedKey === 'DISABLED') {
        result.DISABLED = raw;
        return;
      }
      result[normalizedKey] = raw;
    }
  });
  return result;
}

export async function fetchDashboardSummary(): Promise<DashboardSummary> {
  const raw = await request<unknown>('/api/v1/dashboard/summary');
  const source = asRecord(raw);
  return {
    storeCount: asNumber(source.storeCount || source.totalStores),
    productCount: asNumber(source.productCount || source.activeProducts),
    memberCount: asNumber(source.memberCount || source.totalMembers),
    deviceCount: asNumber(source.deviceCount || source.totalDevices),
    tableCount: asNumber(source.tableCount || source.totalTables),
    activeOrderCount: asNumber(source.activeOrderCount || source.activeOrders),
    todayOrderCount: asNumber(source.todayOrderCount || source.todayOrders),
    tableStatusStats: asStats(source.tableStatusStats || source.tableStatus),
  };
}
