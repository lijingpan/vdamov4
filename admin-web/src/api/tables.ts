import { request } from '@/api/http';

export interface TableQuery {
  storeId?: number;
  status?: string;
  keyword?: string;
}

export interface TableSummary {
  id: number;
  storeId: number;
  storeName: string;
  tableCode: string;
  tableName: string;
  areaName: string;
  capacity: number;
  status: string;
  currentOrderNo: string;
  currentOrderStatus: string;
  currentAmountInCent: number;
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

export async function fetchTables(query: TableQuery): Promise<TableSummary[]> {
  const raw = await request<unknown[]>(
    '/api/v1/tables',
    undefined,
    { storeId: query.storeId },
  );
  return raw.map((item) => {
    const source = asRecord(item);
    const currentOrder = asRecord(source.currentOrder);
    return {
      id: asNumber(source.id),
      storeId: asNumber(source.storeId),
      storeName: asString(source.storeName),
      tableCode: asString(source.tableCode || source.code || source.tableName || source.name),
      tableName: asString(source.tableName || source.name),
      areaName: asString(source.areaName || source.area),
      capacity: asNumber(source.capacity),
      status: asString(source.status),
      currentOrderNo: asString(source.currentOrderNo || source.orderNo || currentOrder.orderNo),
      currentOrderStatus: asString(source.currentOrderStatus || source.orderStatus || currentOrder.orderStatus),
      currentAmountInCent: asNumber(
        source.currentAmountInCent || source.currentOrderAmountInCent || currentOrder.payableAmountInCent,
      ),
    };
  });
}
