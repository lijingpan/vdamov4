import { request } from '@/api/http';

export interface OrderQuery {
  storeId?: number;
  status?: string;
  keyword?: string;
}

export interface OrderSummary {
  id: number;
  orderNo: string;
  storeId: number;
  storeName: string;
  tableName: string;
  memberName: string;
  orderStatus: string;
  paymentStatus: string;
  payableAmountInCent: number;
  discountAmountInCent: number;
  isAppendOrder: boolean;
  createdAt: string;
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
  return typeof value === 'boolean' ? value : false;
}

export async function fetchOrders(query: OrderQuery): Promise<OrderSummary[]> {
  const raw = await request<unknown[]>(
    '/api/v1/orders',
    undefined,
    { storeId: query.storeId },
  );
  return raw.map((item) => {
    const source = asRecord(item);
    return {
      id: asNumber(source.id),
      orderNo: asString(source.orderNo || source.orderNumber),
      storeId: asNumber(source.storeId),
      storeName: asString(source.storeName),
      tableName: asString(source.tableName),
      memberName: asString(source.memberDisplayName || source.memberName),
      orderStatus: asString(source.orderStatus || source.status),
      paymentStatus: asString(source.paymentStatus),
      payableAmountInCent: asNumber(source.payableAmountInCent),
      discountAmountInCent: asNumber(source.discountAmountInCent),
      isAppendOrder: asBoolean(source.isAppendOrder || source.appendOrder || source.hasAppend),
      createdAt: asString(source.createdAt || source.createTime),
    };
  });
}
