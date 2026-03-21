import { request } from '@/api/http';

export interface ProductQuery {
  storeId?: number;
  categoryCode?: string;
  active?: boolean;
  keyword?: string;
}

export type ProductType = 'NORMAL' | 'WEIGHED' | 'ADD_ON' | 'SET_MEAL';
export type ProductSpecMode = 'SINGLE' | 'MULTI';

export interface ProductSummary {
  id: number;
  storeId: number;
  storeName: string;
  name: string;
  code: string;
  categoryCode: string;
  productType: ProductType;
  specMode: ProductSpecMode;
  priceInCent: number;
  maxPriceInCent: number;
  skuCount: number;
  attrEnabled: boolean;
  weighedEnabled: boolean;
  active: boolean;
}

export interface ProductSpecValue {
  id?: number;
  name: string;
  sortOrder: number;
}

export interface ProductSpec {
  id?: number;
  name: string;
  sortOrder: number;
  values: ProductSpecValue[];
}

export interface ProductAttrValue {
  id?: number;
  name: string;
  isDefault: boolean;
  sortOrder: number;
}

export interface ProductAttr {
  id?: number;
  name: string;
  sortOrder: number;
  values: ProductAttrValue[];
}

export interface ProductSku {
  id?: number;
  specKey: string;
  specName: string;
  skuCode: string;
  barcode: string;
  priceInCent: number;
  linePriceInCent: number;
  costPriceInCent: number;
  boxFeeInCent: number;
  stockQty: number;
  autoReplenish: boolean;
  weightUnitGram?: number;
  sortOrder: number;
  active: boolean;
}

export interface ProductDetail {
  id: number;
  storeId: number;
  storeName: string;
  name: string;
  code: string;
  categoryCode: string;
  productType: ProductType;
  specMode: ProductSpecMode;
  description: string;
  attrEnabled: boolean;
  materialEnabled: boolean;
  weighedEnabled: boolean;
  active: boolean;
  specs: ProductSpec[];
  skus: ProductSku[];
  attrs: ProductAttr[];
}

export interface ProductPayload {
  storeId: number;
  name: string;
  code: string;
  categoryCode: string;
  productType: ProductType;
  specMode: ProductSpecMode;
  description?: string;
  attrEnabled: boolean;
  materialEnabled: boolean;
  weighedEnabled: boolean;
  active: boolean;
  specs: ProductSpec[];
  skus: ProductSku[];
  attrs: ProductAttr[];
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

function asArray(value: unknown): unknown[] {
  return Array.isArray(value) ? value : [];
}

function toProductSummary(item: unknown): ProductSummary {
  const source = asRecord(item);
  return {
    id: asNumber(source.id),
    storeId: asNumber(source.storeId),
    storeName: asString(source.storeName),
    name: asString(source.name),
    code: asString(source.code),
    categoryCode: asString(source.categoryCode),
    productType: (asString(source.productType) || 'NORMAL') as ProductType,
    specMode: (asString(source.specMode) || 'SINGLE') as ProductSpecMode,
    priceInCent: asNumber(source.priceInCent),
    maxPriceInCent: asNumber(source.maxPriceInCent || source.priceInCent),
    skuCount: asNumber(source.skuCount),
    attrEnabled: asBoolean(source.attrEnabled),
    weighedEnabled: asBoolean(source.weighedEnabled),
    active: asBoolean(source.active || source.enabled || source.status),
  };
}

function toSpecValue(item: unknown): ProductSpecValue {
  const source = asRecord(item);
  return {
    id: asNumber(source.id) || undefined,
    name: asString(source.name),
    sortOrder: asNumber(source.sortOrder),
  };
}

function toSpec(item: unknown): ProductSpec {
  const source = asRecord(item);
  return {
    id: asNumber(source.id) || undefined,
    name: asString(source.name),
    sortOrder: asNumber(source.sortOrder),
    values: asArray(source.values).map(toSpecValue),
  };
}

function toAttrValue(item: unknown): ProductAttrValue {
  const source = asRecord(item);
  return {
    id: asNumber(source.id) || undefined,
    name: asString(source.name),
    isDefault: asBoolean(source.isDefault),
    sortOrder: asNumber(source.sortOrder),
  };
}

function toAttr(item: unknown): ProductAttr {
  const source = asRecord(item);
  return {
    id: asNumber(source.id) || undefined,
    name: asString(source.name),
    sortOrder: asNumber(source.sortOrder),
    values: asArray(source.values).map(toAttrValue),
  };
}

function toSku(item: unknown): ProductSku {
  const source = asRecord(item);
  const weightUnitGram = asNumber(source.weightUnitGram);
  return {
    id: asNumber(source.id) || undefined,
    specKey: asString(source.specKey),
    specName: asString(source.specName),
    skuCode: asString(source.skuCode),
    barcode: asString(source.barcode),
    priceInCent: asNumber(source.priceInCent),
    linePriceInCent: asNumber(source.linePriceInCent),
    costPriceInCent: asNumber(source.costPriceInCent),
    boxFeeInCent: asNumber(source.boxFeeInCent),
    stockQty: asNumber(source.stockQty),
    autoReplenish: asBoolean(source.autoReplenish),
    weightUnitGram: weightUnitGram > 0 ? weightUnitGram : undefined,
    sortOrder: asNumber(source.sortOrder),
    active: asBoolean(source.active),
  };
}

function toProductDetail(item: unknown): ProductDetail {
  const source = asRecord(item);
  return {
    id: asNumber(source.id),
    storeId: asNumber(source.storeId),
    storeName: asString(source.storeName),
    name: asString(source.name),
    code: asString(source.code),
    categoryCode: asString(source.categoryCode),
    productType: (asString(source.productType) || 'NORMAL') as ProductType,
    specMode: (asString(source.specMode) || 'SINGLE') as ProductSpecMode,
    description: asString(source.description),
    attrEnabled: asBoolean(source.attrEnabled),
    materialEnabled: asBoolean(source.materialEnabled),
    weighedEnabled: asBoolean(source.weighedEnabled),
    active: asBoolean(source.active),
    specs: asArray(source.specs).map(toSpec),
    skus: asArray(source.skus).map(toSku),
    attrs: asArray(source.attrs).map(toAttr),
  };
}

export async function fetchProducts(query?: ProductQuery): Promise<ProductSummary[]> {
  const raw = await request<unknown[]>(
    '/api/v1/products',
    undefined,
    query as Record<string, string | number | boolean | null | undefined>,
  );
  return raw.map(toProductSummary);
}

export async function fetchProductDetail(id: number): Promise<ProductDetail> {
  const raw = await request<unknown>(`/api/v1/products/${id}`);
  return toProductDetail(raw);
}

export async function createProduct(payload: ProductPayload): Promise<ProductSummary> {
  const raw = await request<unknown>('/api/v1/products', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
  return toProductSummary(raw);
}

export async function updateProduct(id: number, payload: ProductPayload): Promise<ProductSummary> {
  const raw = await request<unknown>(`/api/v1/products/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
  return toProductSummary(raw);
}

export async function updateProductStatus(id: number, active: boolean): Promise<ProductSummary> {
  const raw = await request<unknown>(`/api/v1/products/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ active }),
  });
  return toProductSummary(raw);
}

export async function deleteProduct(id: number): Promise<void> {
  await request<void>(`/api/v1/products/${id}`, {
    method: 'DELETE',
  });
}
