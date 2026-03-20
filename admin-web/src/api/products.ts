import { request } from '@/api/http';

export interface ProductQuery {
  storeId?: number;
  categoryCode?: string;
  active?: boolean;
  keyword?: string;
}

export interface ProductSummary {
  id: number;
  storeId: number;
  storeName: string;
  name: string;
  code: string;
  categoryCode: string;
  priceInCent: number;
  active: boolean;
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

export async function fetchProducts(query: ProductQuery): Promise<ProductSummary[]> {
  const raw = await request<unknown[]>(
    '/api/v1/products',
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
      code: asString(source.code),
      categoryCode: asString(source.categoryCode),
      priceInCent: asNumber(source.priceInCent || source.price),
      active: asBoolean(source.active || source.enabled || source.status),
    };
  });
}
