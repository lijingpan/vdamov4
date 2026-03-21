import { downloadFile, request } from '@/api/http';

export interface SalesReportQuery {
  storeId?: number;
  startDate?: string;
  endDate?: string;
}

export interface SalesSummary {
  totalOrders: number;
  completedOrders: number;
  inProgressOrders: number;
  revenueInCent: number;
  paidInCent: number;
  discountInCent: number;
  appendOrderCount: number;
  averageOrderAmountInCent: number;
}

export interface SalesByStore {
  storeId: number;
  storeName: string;
  totalOrders: number;
  completedOrders: number;
  inProgressOrders: number;
  revenueInCent: number;
  paidInCent: number;
  discountInCent: number;
  appendOrderCount: number;
  averageOrderAmountInCent: number;
}

export interface SalesByDate {
  date: string;
  totalOrders: number;
  completedOrders: number;
  inProgressOrders: number;
  revenueInCent: number;
  paidInCent: number;
}

export interface SalesByProduct {
  productId: number;
  productName: string;
  productCode: string;
  quantity: number;
  orderCount: number;
  amountInCent: number;
}

export interface SalesByPaymentMethod {
  paymentMethod: string;
  paymentCount: number;
  amountInCent: number;
}

export interface SalesReportData {
  summary: SalesSummary;
  byStore: SalesByStore[];
  byDate: SalesByDate[];
  byProduct: SalesByProduct[];
  byPaymentMethod: SalesByPaymentMethod[];
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

function asString(value: unknown): string {
  return typeof value === 'string' ? value : '';
}

function asArray(value: unknown): unknown[] {
  return Array.isArray(value) ? value : [];
}

function mapSummary(raw: unknown): SalesSummary {
  const source = asRecord(raw);
  return {
    totalOrders: asNumber(source.totalOrders || source.orderCount),
    completedOrders: asNumber(source.completedOrders || source.completedOrderCount),
    inProgressOrders: asNumber(source.inProgressOrders || source.processingOrderCount || source.activeOrders),
    revenueInCent: asNumber(source.revenueInCent || source.grossAmountInCent || source.turnoverInCent),
    paidInCent: asNumber(source.paidInCent || source.receivedAmountInCent || source.netAmountInCent),
    discountInCent: asNumber(source.discountInCent || source.discountAmountInCent),
    appendOrderCount: asNumber(source.appendOrderCount || source.appendOrders),
    averageOrderAmountInCent: asNumber(
      source.averageOrderAmountInCent || source.avgOrderAmountInCent || source.customerUnitPriceInCent,
    ),
  };
}

function mapByStore(raw: unknown): SalesByStore {
  const source = asRecord(raw);
  const totalOrders = asNumber(source.totalOrders || source.orderCount);
  const revenueInCent = asNumber(source.revenueInCent || source.grossAmountInCent || source.turnoverInCent);
  return {
    storeId: asNumber(source.storeId),
    storeName: asString(source.storeName),
    totalOrders,
    completedOrders: asNumber(source.completedOrders || source.completedOrderCount),
    inProgressOrders: asNumber(source.inProgressOrders || source.processingOrderCount || source.activeOrders),
    revenueInCent,
    paidInCent: asNumber(source.paidInCent || source.receivedAmountInCent || source.netAmountInCent),
    discountInCent: asNumber(source.discountInCent || source.discountAmountInCent),
    appendOrderCount: asNumber(source.appendOrderCount || source.appendOrders),
    averageOrderAmountInCent:
      asNumber(source.averageOrderAmountInCent || source.avgOrderAmountInCent || source.customerUnitPriceInCent) ||
      (totalOrders === 0 ? 0 : Math.floor(revenueInCent / totalOrders)),
  };
}

function mapByDate(raw: unknown): SalesByDate {
  const source = asRecord(raw);
  return {
    date: asString(source.date || source.reportDate || source.businessDate),
    totalOrders: asNumber(source.totalOrders || source.orderCount),
    completedOrders: asNumber(source.completedOrders || source.completedOrderCount),
    inProgressOrders: asNumber(source.inProgressOrders || source.processingOrderCount || source.activeOrders),
    revenueInCent: asNumber(source.revenueInCent || source.grossAmountInCent || source.turnoverInCent),
    paidInCent: asNumber(source.paidInCent || source.receivedAmountInCent || source.netAmountInCent),
  };
}

function mapByProduct(raw: unknown): SalesByProduct {
  const source = asRecord(raw);
  return {
    productId: asNumber(source.productId),
    productName: asString(source.productName || source.itemName),
    productCode: asString(source.productCode || source.itemCode),
    quantity: asNumber(source.quantity || source.totalQuantity),
    orderCount: asNumber(source.orderCount || source.totalOrders),
    amountInCent: asNumber(source.amountInCent || source.totalAmountInCent || source.revenueInCent),
  };
}

function mapByPaymentMethod(raw: unknown): SalesByPaymentMethod {
  const source = asRecord(raw);
  return {
    paymentMethod: asString(source.paymentMethod || source.method || source.paymentChannel),
    paymentCount: asNumber(source.paymentCount || source.count),
    amountInCent: asNumber(source.amountInCent || source.totalAmountInCent || source.paidInCent),
  };
}

export async function fetchSalesReport(query: SalesReportQuery): Promise<SalesReportData> {
  const raw = await request<unknown>(
    '/api/v1/reports/sales',
    undefined,
    query as Record<string, string | number | boolean | null | undefined>,
  );
  const source = asRecord(raw);
  return {
    summary: mapSummary(source.summary || source.overview || source),
    byStore: asArray(source.byStore || source.storeRows || source.storeStats || source.storeSummaries).map((item) =>
      mapByStore(item),
    ),
    byDate: asArray(source.byDate || source.dailyTrend || source.dateTrends || source.dailyStats).map((item) =>
      mapByDate(item),
    ),
    byProduct: asArray(source.byProduct || source.productRows || source.productStats || source.topProducts).map((item) =>
      mapByProduct(item),
    ),
    byPaymentMethod: asArray(
      source.byPaymentMethod || source.paymentMethodRows || source.paymentStats || source.paymentMethods,
    ).map((item) => mapByPaymentMethod(item)),
  };
}

export async function exportSalesReport(query: SalesReportQuery): Promise<void> {
  await downloadFile(
    '/api/v1/reports/sales/export',
    query as Record<string, string | number | boolean | null | undefined>,
    {
      fileName: 'sales-report.csv',
    },
  );
}
