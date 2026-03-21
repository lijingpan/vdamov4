import { request } from '@/api/http';

export interface OrderQuery {
  storeId?: number;
  status?: string;
  paymentStatus?: string;
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

export interface OrderDetailItem {
  id: number;
  productId: number;
  productName: string;
  skuName: string;
  quantity: number;
  unitPriceInCent: number;
  totalPriceInCent: number;
  remark: string;
}

export interface OrderAppendRecord {
  id: number;
  appendNo: string;
  operatorName: string;
  quantity: number;
  amountInCent: number;
  remark: string;
  createdAt: string;
}

export interface OrderPaymentRecord {
  id: number;
  paymentNo: string;
  paymentMethod: string;
  paymentStatus: string;
  amountInCent: number;
  operatorName: string;
  paidAt: string;
}

export interface OrderDetail {
  id: number;
  orderNo: string;
  storeId: number;
  storeName: string;
  tableName: string;
  memberName: string;
  orderStatus: string;
  paymentStatus: string;
  remark: string;
  isAppendOrder: boolean;
  createdAt: string;
  originalAmountInCent: number;
  discountAmountInCent: number;
  payableAmountInCent: number;
  paidAmountInCent: number;
  outstandingAmountInCent: number;
  items: OrderDetailItem[];
  appendRecords: OrderAppendRecord[];
  paymentRecords: OrderPaymentRecord[];
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

function asArray(value: unknown): unknown[] {
  return Array.isArray(value) ? value : [];
}

function mapOrderItem(item: unknown): OrderDetailItem {
  const source = asRecord(item);
  return {
    id: asNumber(source.id),
    productId: asNumber(source.productId),
    productName: asString(source.productName || source.itemName || source.name),
    skuName: asString(source.skuName || source.itemCode || source.specName),
    quantity: asNumber(source.quantity),
    unitPriceInCent: asNumber(source.unitPriceInCent || source.priceInCent),
    totalPriceInCent: asNumber(
      source.totalPriceInCent || source.payableAmountInCent || source.amountInCent || source.originalAmountInCent,
    ),
    remark: asString(source.remark || source.note),
  };
}

function mapAppendRecord(item: unknown): OrderAppendRecord {
  const source = asRecord(item);
  const appendRound = asNumber(source.appendRound);
  return {
    id: asNumber(source.id),
    appendNo: asString(source.appendNo || source.recordNo || source.appendOrderNo) || `APPEND-${appendRound || 0}`,
    operatorName: asString(source.operatorName || source.createdByName),
    quantity: asNumber(source.quantity || source.appendItemCount || source.appendQuantity),
    amountInCent: asNumber(source.amountInCent || source.appendAmountInCent),
    remark: asString(source.remark || source.note || source.actionType),
    createdAt: asString(source.createdAt || source.operateTime || source.createTime),
  };
}

function mapPaymentRecord(item: unknown): OrderPaymentRecord {
  const source = asRecord(item);
  return {
    id: asNumber(source.id),
    paymentNo: asString(source.paymentNo || source.transactionNo),
    paymentMethod: asString(source.paymentMethod || source.channelCode),
    paymentStatus: asString(source.paymentStatus || source.status),
    amountInCent: asNumber(source.amountInCent || source.paidAmountInCent),
    operatorName: asString(source.operatorName || source.cashierName || source.createdByName),
    paidAt: asString(source.paidAt || source.paidTime || source.payTime || source.createdAt),
  };
}

function mapOrderDetail(item: unknown): OrderDetail {
  const source = asRecord(item);
  const header = asRecord(source.header);
  const amountSummary = asRecord(source.amountSummary);
  return {
    id: asNumber(source.id || header.id),
    orderNo: asString(source.orderNo || source.orderNumber || header.orderNo),
    storeId: asNumber(source.storeId || header.storeId),
    storeName: asString(source.storeName || header.storeName),
    tableName: asString(source.tableName || header.tableName),
    memberName: asString(source.memberDisplayName || source.memberName || header.memberDisplayName || header.memberName),
    orderStatus: asString(source.orderStatus || source.status || header.orderStatus),
    paymentStatus: asString(source.paymentStatus || header.paymentStatus),
    remark: asString(source.remark || source.note),
    isAppendOrder:
      asBoolean(source.isAppendOrder || source.appendOrder || source.hasAppend || header.hasAppend) ||
      asNumber(header.appendCount) > 0,
    createdAt: asString(source.createdAt || source.createTime || header.createdAt || header.createTime),
    originalAmountInCent: asNumber(
      source.originalAmountInCent || source.totalAmountInCent || amountSummary.originalAmountInCent,
    ),
    discountAmountInCent: asNumber(source.discountAmountInCent || amountSummary.discountAmountInCent),
    payableAmountInCent: asNumber(source.payableAmountInCent || amountSummary.payableAmountInCent),
    paidAmountInCent: asNumber(source.paidAmountInCent || source.paymentAmountInCent || amountSummary.paidAmountInCent),
    outstandingAmountInCent: asNumber(
      source.outstandingAmountInCent || amountSummary.outstandingAmountInCent || amountSummary.unpaidAmountInCent,
    ),
    items: asArray(source.items || source.orderItems).map((entry) => mapOrderItem(entry)),
    appendRecords: asArray(source.appendRecords || source.additions || source.appendLogs).map((entry) =>
      mapAppendRecord(entry),
    ),
    paymentRecords: asArray(source.paymentRecords || source.payments || source.settlementRecords).map((entry) =>
      mapPaymentRecord(entry),
    ),
  };
}

export async function fetchOrders(query: OrderQuery): Promise<OrderSummary[]> {
  const raw = await request<unknown[]>(
    '/api/v1/orders',
    undefined,
    {
      storeId: query.storeId,
      keyword: query.keyword,
      orderStatus: query.status,
      paymentStatus: query.paymentStatus,
    },
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

export async function fetchOrderDetail(orderId: number): Promise<OrderDetail> {
  const raw = await request<unknown>(`/api/v1/orders/${orderId}`);
  return mapOrderDetail(raw);
}

export interface OrderStatusUpdatePayload {
  orderStatus: string;
}

export interface OrderPaymentStatusUpdatePayload {
  paymentStatus: string;
}

export interface BatchOrderStatusUpdatePayload extends OrderStatusUpdatePayload {
  orderIds: number[];
}

export interface BatchOrderPaymentStatusUpdatePayload extends OrderPaymentStatusUpdatePayload {
  orderIds: number[];
}

export interface BatchOrderCompletePayload {
  orderIds: number[];
}

export async function updateOrderStatus(orderId: number, payload: OrderStatusUpdatePayload): Promise<void> {
  await request<unknown>(`/api/v1/orders/${orderId}/status`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });
}

export async function updateOrderPaymentStatus(orderId: number, payload: OrderPaymentStatusUpdatePayload): Promise<void> {
  await request<unknown>(`/api/v1/orders/${orderId}/payment-status`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });
}

export async function updateOrderStatusBatch(payload: BatchOrderStatusUpdatePayload): Promise<void> {
  await request<unknown>('/api/v1/orders/status', {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });
}

export async function updateOrderPaymentStatusBatch(payload: BatchOrderPaymentStatusUpdatePayload): Promise<void> {
  await request<unknown>('/api/v1/orders/payment-status', {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });
}

export async function completeOrder(orderId: number): Promise<void> {
  await request<unknown>(`/api/v1/orders/${orderId}/complete`, {
    method: 'PATCH',
  });
}

export async function completeOrdersBatch(payload: BatchOrderCompletePayload): Promise<void> {
  await request<unknown>('/api/v1/orders/complete', {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });
}
