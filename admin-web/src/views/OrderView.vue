<template>
  <PageShell :title="t('page.orders.title')" :description="t('page.orders.description')">
    <div v-loading="loading">
      <el-form :inline="true" class="filter-form">
        <el-form-item :label="t('order.filter.store')">
          <el-select v-model="storeId" clearable :placeholder="t('order.filter.storePlaceholder')">
            <el-option
              v-for="item in storeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('order.filter.status')">
          <el-select v-model="statusFilter" clearable :placeholder="t('order.filter.statusPlaceholder')">
            <el-option
              v-for="item in orderStatuses"
              :key="item"
              :label="t(`dict.orderStatus.${item}`)"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('order.filter.keyword')">
          <el-input v-model="keyword" clearable :placeholder="t('order.filter.keywordPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadOrders">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

      <el-row :gutter="12" class="summary-row">
        <el-col :xs="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('order.summary.total')" :value="filteredRows.length" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="6">
          <el-card shadow="hover">
            <el-statistic
              :title="t('order.summary.pending')"
              :value="countByStatus('PLACED') + countByStatus('IN_PROGRESS')"
            />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="6">
          <el-card shadow="hover">
            <el-statistic
              :title="t('order.summary.waitingCheckout')"
              :value="countByStatus('WAITING_CHECKOUT')"
            />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('order.summary.completed')" :value="countByStatus('COMPLETED')" />
          </el-card>
        </el-col>
      </el-row>

      <el-table :data="filteredRows" stripe border>
        <el-table-column prop="orderNo" :label="t('order.columns.orderNo')" min-width="180" />
        <el-table-column prop="storeName" :label="t('order.columns.storeName')" min-width="180" />
        <el-table-column prop="tableName" :label="t('order.columns.tableName')" min-width="140" />
        <el-table-column prop="memberName" :label="t('order.columns.memberName')" min-width="140" />
        <el-table-column :label="t('order.columns.orderStatus')" min-width="160">
          <template #default="{ row }">
            <el-tag :type="statusType(row.orderStatus)">
              {{ t(`dict.orderStatus.${row.orderStatus}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" :label="t('order.columns.paymentStatus')" min-width="140" />
        <el-table-column :label="t('order.columns.payableAmount')" min-width="140">
          <template #default="{ row }">
            {{ formatCurrency(row.payableAmountInCent) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('order.columns.discountAmount')" min-width="140">
          <template #default="{ row }">
            {{ formatCurrency(row.discountAmountInCent) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('order.columns.appendOrder')" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.isAppendOrder ? 'warning' : 'info'">
              {{ row.isAppendOrder ? t('common.yes') : t('common.no') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="t('order.columns.createdAt')" min-width="180" />
        <el-table-column :label="t('order.columns.actions')" min-width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goToDetail(row.id)">
              {{ t('order.actions.viewDetail') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import type { OrderSummary } from '@/api/orders';
import { fetchOrders } from '@/api/orders';
import type { StoreSummary } from '@/api/stores';
import { fetchStores } from '@/api/stores';
import PageShell from '@/components/PageShell.vue';

const { t } = useI18n();
const router = useRouter();

const loading = ref(false);
const errorMessage = ref('');
const storeId = ref<number | undefined>();
const statusFilter = ref('');
const keyword = ref('');
const orderRows = ref<OrderSummary[]>([]);
const storeOptions = ref<StoreSummary[]>([]);
const orderStatuses = [
  'PENDING_CONFIRM',
  'PLACED',
  'IN_PROGRESS',
  'COOKING',
  'SERVED',
  'WAITING_CHECKOUT',
  'COMPLETED',
  'CANCELLED',
  'REFUNDED',
];
const filteredRows = computed(() =>
  orderRows.value.filter((item) => {
    const matchStatus = !statusFilter.value || item.orderStatus === statusFilter.value;
    const keywordValue = keyword.value.trim().toLowerCase();
    const matchKeyword =
      !keywordValue ||
      item.orderNo.toLowerCase().includes(keywordValue) ||
      item.memberName.toLowerCase().includes(keywordValue) ||
      item.tableName.toLowerCase().includes(keywordValue) ||
      item.storeName.toLowerCase().includes(keywordValue);
    return matchStatus && matchKeyword;
  }),
);

function formatCurrency(valueInCent: number): string {
  return `¥ ${(valueInCent / 100).toFixed(2)}`;
}

function countByStatus(status: string): number {
  return filteredRows.value.filter((item) => item.orderStatus === status).length;
}

function statusType(status: string): 'success' | 'warning' | 'danger' | 'info' {
  if (status === 'COMPLETED') {
    return 'success';
  }
  if (status === 'WAITING_CHECKOUT' || status === 'PLACED' || status === 'IN_PROGRESS') {
    return 'warning';
  }
  if (status === 'CANCELLED' || status === 'REFUNDED') {
    return 'danger';
  }
  return 'info';
}

function resetFilters() {
  storeId.value = undefined;
  statusFilter.value = '';
  keyword.value = '';
}

function goToDetail(orderId: number) {
  router.push(`/orders/${orderId}`);
}

async function loadOrders() {
  loading.value = true;
  errorMessage.value = '';
  try {
    orderRows.value = await fetchOrders({
      storeId: storeId.value,
      status: statusFilter.value || undefined,
    });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

async function loadStores() {
  try {
    storeOptions.value = await fetchStores();
  } catch {
    storeOptions.value = [];
  }
}

onMounted(async () => {
  await loadStores();
  await loadOrders();
});
</script>

<style scoped>
.filter-form {
  margin-bottom: 12px;
}

.summary-row {
  margin-bottom: 12px;
}
</style>
