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

export async function fetchProductCategories(query: ProductCategoryQuery): Promise<ProductCategorySummary[]> {
  const raw = await request<unknown[]>(
    '/api/v1/product-categories',
    undefined,
    query as Record<string, string | number | boolean | null | undefined>,
  );
  return raw.map((item) => {
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
  });
}
