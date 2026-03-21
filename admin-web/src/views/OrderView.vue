<template>
  <PageShell :title="t('page.orders.title')" :description="t('page.orders.description')">
    <template #actions>
      <el-button :icon="RefreshRight" @click="loadData">{{ t('common.refresh') }}</el-button>
    </template>

    <template #meta>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-card__label">{{ t('order.summary.total') }}</div>
          <div class="stat-card__value">{{ filteredRows.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('order.summary.pending') }}</div>
          <div class="stat-card__value">{{ countByStatuses(['PLACED', 'IN_PROGRESS', 'COOKING', 'SERVED']) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('order.summary.waitingCheckout') }}</div>
          <div class="stat-card__value">{{ countByStatuses(['WAITING_CHECKOUT']) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('order.summary.completed') }}</div>
          <div class="stat-card__value">{{ countByStatuses(['COMPLETED']) }}</div>
        </div>
      </div>
    </template>

    <el-card shadow="never" class="page-card filter-card">
      <el-form :inline="true">
        <el-form-item :label="t('order.filter.store')">
          <el-select v-model="filters.storeId" clearable :placeholder="t('order.filter.storePlaceholder')">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('order.filter.status')">
          <el-select v-model="filters.status" clearable :placeholder="t('order.filter.statusPlaceholder')">
            <el-option
              v-for="item in orderStatusOptions"
              :key="item"
              :label="orderStatusLabel(item)"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('order.filter.paymentStatus')">
          <el-select
            v-model="filters.paymentStatus"
            clearable
            :placeholder="t('order.filter.paymentStatusPlaceholder')"
          >
            <el-option
              v-for="item in paymentStatusOptions"
              :key="item"
              :label="paymentStatusLabel(item)"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('order.filter.keyword')">
          <el-input
            v-model="filters.keyword"
            clearable
            :placeholder="t('order.filter.keywordPlaceholder')"
            @keyup.enter="loadOrders"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadOrders">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page-card data-card">
      <div class="action-bar">
        <div class="action-bar__hint">{{ t('page.orders.description') }}</div>
      </div>

      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="false"
        style="margin: 16px 0"
      />

      <el-table v-loading="loading" :data="filteredRows" stripe>
        <el-table-column prop="orderNo" :label="t('order.columns.orderNo')" min-width="180" />
        <el-table-column prop="storeName" :label="t('order.columns.storeName')" min-width="180" />
        <el-table-column prop="tableName" :label="t('order.columns.tableName')" min-width="140" />
        <el-table-column prop="memberName" :label="t('order.columns.memberName')" min-width="140" />
        <el-table-column :label="t('order.columns.orderStatus')" min-width="160">
          <template #default="{ row }">
            <el-tag :type="statusType(row.orderStatus)">
              {{ orderStatusLabel(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('order.columns.paymentStatus')" min-width="140">
          <template #default="{ row }">
            {{ paymentStatusLabel(row.paymentStatus) }}
          </template>
        </el-table-column>
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
        <el-table-column :label="t('order.columns.actions')" min-width="220" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button type="primary" link @click="goToDetail(row.id)">
                {{ t('order.actions.viewDetail') }}
              </el-button>
              <el-button v-if="canManageOrder" link type="primary" @click="openManageDialog(row)">
                {{ t('order.actions.manage') }}
              </el-button>
              <el-button
                v-if="canCompleteOrder && row.paymentStatus === 'PAID' && row.orderStatus !== 'COMPLETED'"
                link
                type="success"
                @click="submitCompleteOrder(row)"
              >
                {{ t('order.actions.complete') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="t('order.manage.title')"
      width="560px"
      destroy-on-close
    >
      <template v-if="currentRow">
        <el-form label-position="top" class="dialog-form">
          <el-form-item :label="t('order.manage.orderStatus')">
            <el-select v-model="dialogOrderStatus" :disabled="!canUpdateOrderStatus">
              <el-option
                v-for="item in orderStatusOptions"
                :key="item"
                :label="orderStatusLabel(item)"
                :value="item"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('order.manage.paymentStatus')">
            <el-select v-model="dialogPaymentStatus" :disabled="!canUpdatePaymentStatus">
              <el-option
                v-for="item in paymentStatusOptions"
                :key="item"
                :label="paymentStatusLabel(item)"
                :value="item"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </template>

      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button
          v-if="canCompleteOrder && currentRow?.paymentStatus === 'PAID' && currentRow?.orderStatus !== 'COMPLETED'"
          type="success"
          :loading="completingOrder"
          @click="submitCompleteOrder(currentRow)"
        >
          {{ t('order.manage.complete') }}
        </el-button>
        <el-button type="primary" :loading="saving" @click="submitManageDialog">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ElMessage, ElMessageBox } from 'element-plus';
import { RefreshRight } from '@element-plus/icons-vue';
import type { OrderSummary } from '@/api/orders';
import { completeOrder, fetchOrders, updateOrderPaymentStatus, updateOrderStatus } from '@/api/orders';
import type { StoreSummary } from '@/api/stores';
import { fetchStores } from '@/api/stores';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';

interface OrderFilters {
  storeId?: number;
  status: string;
  paymentStatus: string;
  keyword: string;
}

const { t } = useI18n();
const router = useRouter();
const authStore = useAuthStore();

const loading = ref(false);
const saving = ref(false);
const completingOrder = ref(false);
const errorMessage = ref('');
const orderRows = ref<OrderSummary[]>([]);
const storeOptions = ref<StoreSummary[]>([]);
const dialogVisible = ref(false);
const currentRow = ref<OrderSummary | null>(null);
const dialogOrderStatus = ref('');
const dialogPaymentStatus = ref('');

const filters = reactive<OrderFilters>({
  status: '',
  paymentStatus: '',
  keyword: '',
});

const canUpdateOrderStatus = computed(() => authStore.hasPermission('order:update-status'));
const canUpdatePaymentStatus = computed(() => authStore.hasPermission('order:update-payment'));
const canCompleteOrder = computed(() => authStore.hasPermission('order:complete'));
const canManageOrder = computed(() => canUpdateOrderStatus.value || canUpdatePaymentStatus.value);

const orderStatusOptions = [
  'PENDING_CONFIRM',
  'PENDING',
  'PLACED',
  'CONFIRMED',
  'IN_PROGRESS',
  'COOKING',
  'SERVED',
  'WAITING_CHECKOUT',
  'COMPLETED',
  'CANCELLED',
  'REFUNDED',
];
const paymentStatusOptions = ['UNPAID', 'PARTIAL', 'PAID', 'REFUNDED'];

const filteredRows = computed(() =>
  orderRows.value.filter((item) => {
    const keywordValue = filters.keyword.trim().toLowerCase();
    if (!keywordValue) {
      return true;
    }
    return (
      item.orderNo.toLowerCase().includes(keywordValue) ||
      (item.memberName || '').toLowerCase().includes(keywordValue) ||
      (item.tableName || '').toLowerCase().includes(keywordValue) ||
      (item.storeName || '').toLowerCase().includes(keywordValue)
    );
  }),
);

function formatCurrency(valueInCent: number): string {
  return `¥ ${(valueInCent / 100).toFixed(2)}`;
}

function orderStatusLabel(status: string): string {
  if (!status) {
    return '-';
  }
  const key = `dict.orderStatus.${status}`;
  const label = t(key);
  return label === key ? status : label;
}

function paymentStatusLabel(status: string): string {
  if (!status) {
    return '-';
  }
  const key = `dict.paymentStatus.${status}`;
  const label = t(key);
  return label === key ? status : label;
}

function countByStatuses(statuses: string[]): number {
  return filteredRows.value.filter((item) => statuses.includes(item.orderStatus)).length;
}

function statusType(status: string): 'success' | 'warning' | 'danger' | 'info' {
  if (status === 'COMPLETED') {
    return 'success';
  }
  if (status === 'WAITING_CHECKOUT' || status === 'PLACED' || status === 'IN_PROGRESS' || status === 'COOKING') {
    return 'warning';
  }
  if (status === 'CANCELLED' || status === 'REFUNDED') {
    return 'danger';
  }
  return 'info';
}

function resetFilters() {
  filters.storeId = undefined;
  filters.status = '';
  filters.paymentStatus = '';
  filters.keyword = '';
  loadOrders();
}

function goToDetail(orderId: number) {
  router.push(`/orders/${orderId}`);
}

function openManageDialog(row: OrderSummary) {
  currentRow.value = row;
  dialogOrderStatus.value = row.orderStatus;
  dialogPaymentStatus.value = row.paymentStatus;
  dialogVisible.value = true;
}

async function loadOrders() {
  loading.value = true;
  errorMessage.value = '';
  try {
    orderRows.value = await fetchOrders({
      storeId: filters.storeId,
      status: filters.status || undefined,
      paymentStatus: filters.paymentStatus || undefined,
      keyword: filters.keyword || undefined,
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

async function loadData() {
  await loadStores();
  await loadOrders();
}

async function submitManageDialog() {
  if (!currentRow.value) {
    return;
  }

  const paymentChanged = canUpdatePaymentStatus.value && dialogPaymentStatus.value !== currentRow.value.paymentStatus;
  const orderChanged = canUpdateOrderStatus.value && dialogOrderStatus.value !== currentRow.value.orderStatus;
  if (!paymentChanged && !orderChanged) {
    dialogVisible.value = false;
    return;
  }

  saving.value = true;
  try {
    if (paymentChanged) {
      await updateOrderPaymentStatus(currentRow.value.id, { paymentStatus: dialogPaymentStatus.value });
    }
    if (orderChanged) {
      await updateOrderStatus(currentRow.value.id, { orderStatus: dialogOrderStatus.value });
    }
    ElMessage.success(t('order.manage.updateSuccess'));
    dialogVisible.value = false;
    await loadOrders();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  } finally {
    saving.value = false;
  }
}

async function submitCompleteOrder(row: OrderSummary) {
  try {
    await ElMessageBox.confirm(t('order.manage.complete'), t('order.manage.title'), {
      type: 'warning',
    });
  } catch {
    return;
  }

  completingOrder.value = true;
  try {
    await completeOrder(row.id);
    ElMessage.success(t('order.manage.updateSuccess'));
    dialogVisible.value = false;
    await loadOrders();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  } finally {
    completingOrder.value = false;
  }
}

onMounted(loadData);
</script>
