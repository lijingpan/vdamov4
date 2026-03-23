import { request } from '@/api/http';

export interface DiscountQuery {
  storeId?: number;
  enabled?: boolean;
  discountType?: string;
  keyword?: string;
}

export interface DiscountSummary {
  id: number;
  storeId: number;
  storeName: string;
  name: string;
  code: string;
  discountType: string;
  amountType: string;
  amountValue: number;
  thresholdAmountInCent: number;
  stackable: boolean;
  enabled: boolean;
  startTime: string;
  endTime: string;
  remark: string;
}

export interface DiscountPayload {
  storeId: number;
  name: string;
  code: string;
  discountType: string;
  amountType: string;
  amountValue: number;
  thresholdAmountInCent: number;
  stackable: boolean;
  enabled: boolean;
  startTime?: string;
  endTime?: string;
  remark?: string;
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
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value;
  }
  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value);
    if (Number.isFinite(parsed)) {
      return parsed;
    }
  }
  return 0;
}

function asBoolean(value: unknown): boolean {
  if (typeof value === 'boolean') {
    return value;
  }
  if (typeof value === 'number') {
    return value !== 0;
  }
  if (typeof value === 'string') {
    const normalized = value.trim().toUpperCase();
    return normalized === 'TRUE' || normalized === 'ENABLE' || normalized === 'ENABLED' || normalized === '1';
  }
  return false;
}

function toDiscountSummary(item: unknown): DiscountSummary {
  const source = asRecord(item);
  return {
    id: asNumber(source.id),
    storeId: asNumber(source.storeId),
    storeName: asString(source.storeName),
    name: asString(source.name || source.discountName),
    code: asString(source.code || source.discountCode),
    discountType: asString(source.discountType || source.type),
    amountType: asString(source.amountType || source.valueType),
    amountValue: asNumber(source.amountValue || source.discountValue || source.value),
    thresholdAmountInCent: asNumber(
      source.thresholdAmountInCent || source.minAmountInCent || source.conditionAmountInCent,
    ),
    stackable: asBoolean(source.stackable || source.allowStack || source.canStack),
    enabled: asBoolean(source.enabled || source.active || source.status),
    startTime: asString(source.startTime || source.effectiveFrom || source.startAt),
    endTime: asString(source.endTime || source.effectiveTo || source.endAt),
    remark: asString(source.remark || source.note),
  };
}

export async function fetchDiscounts(query?: DiscountQuery): Promise<DiscountSummary[]> {
  const raw = await request<unknown[]>(
    '/api/v1/discounts',
    undefined,
    query as Record<string, string | number | boolean | null | undefined> | undefined,
  );
  return raw.map(toDiscountSummary);
}

export async function createDiscount(payload: DiscountPayload): Promise<DiscountSummary> {
  const raw = await request<unknown>('/api/v1/discounts', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
  return toDiscountSummary(raw);
}

export async function updateDiscount(id: number, payload: DiscountPayload): Promise<DiscountSummary> {
  const raw = await request<unknown>(`/api/v1/discounts/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
  return toDiscountSummary(raw);
}

export async function updateDiscountEnabled(id: number, enabled: boolean): Promise<DiscountSummary> {
  const raw = await request<unknown>(`/api/v1/discounts/${id}/enabled`, {
    method: 'PATCH',
    body: JSON.stringify({ enabled }),
  });
  return toDiscountSummary(raw);
}

export async function deleteDiscount(id: number): Promise<void> {
  await request<void>(`/api/v1/discounts/${id}`, {
    method: 'DELETE',
  });
}
