import { request } from '@/api/http';

export interface ProductCategoryQuery {
  storeId?: number;
  keyword?: string;
  enabled?: boolean;
}

export interface ProductCategorySummary {
  id: number;
  storeId: number;
  storeName: string;
  categoryName: string;
  categoryCode: string;
  sortOrder: number;
  enabled: boolean;
  productCount: number;
}

export interface ProductCategoryPayload {
  storeId: number;
  categoryName: string;
  categoryCode: string;
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

function toProductCategorySummary(item: unknown): ProductCategorySummary {
  const source = asRecord(item);
  return {
    id: asNumber(source.id),
    storeId: asNumber(source.storeId),
    storeName: asString(source.storeName),
    categoryName: asString(source.categoryName || source.name),
    categoryCode: asString(source.categoryCode || source.code),
    sortOrder: asNumber(source.sortOrder),
    enabled: asBoolean(source.enabled || source.status || source.active),
    productCount: asNumber(source.productCount || source.productsCount),
  };
}

export async function fetchProductCategories(query?: ProductCategoryQuery): Promise<ProductCategorySummary[]> {
  const raw = await request<unknown[]>(
    '/api/v1/product-categories',
    undefined,
    query as Record<string, string | number | boolean | null | undefined>,
  );
  return raw.map(toProductCategorySummary);
}

export async function createProductCategory(payload: ProductCategoryPayload): Promise<ProductCategorySummary> {
  const raw = await request<unknown>('/api/v1/product-categories', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
  return toProductCategorySummary(raw);
}

export async function updateProductCategory(id: number, payload: ProductCategoryPayload): Promise<ProductCategorySummary> {
  const raw = await request<unknown>(`/api/v1/product-categories/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
  return toProductCategorySummary(raw);
}

export async function updateProductCategoryEnabled(id: number, enabled: boolean): Promise<ProductCategorySummary> {
  const raw = await request<unknown>(`/api/v1/product-categories/${id}/enabled`, {
    method: 'PATCH',
    body: JSON.stringify({ enabled }),
  });
  return toProductCategorySummary(raw);
}
