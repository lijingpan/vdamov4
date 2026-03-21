<template>
  <PageShell :title="t('page.orderDetail.title')" :description="t('page.orderDetail.description')">
    <div v-loading="loading">
      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

      <template v-else-if="detail">
        <el-row :gutter="12" class="section-row">
          <el-col :xs="24" :lg="15">
            <el-card shadow="never">
              <template #header>
                <span>{{ t('order.detail.sections.basic') }}</span>
              </template>
              <el-descriptions :column="2" border>
                <el-descriptions-item :label="t('order.detail.fields.orderNo')">
                  {{ detail.orderNo || '-' }}
                </el-descriptions-item>
                <el-descriptions-item :label="t('order.detail.fields.createdAt')">
                  {{ detail.createdAt || '-' }}
                </el-descriptions-item>
                <el-descriptions-item :label="t('order.detail.fields.storeName')">
                  {{ detail.storeName || '-' }}
                </el-descriptions-item>
                <el-descriptions-item :label="t('order.detail.fields.tableName')">
                  {{ detail.tableName || '-' }}
                </el-descriptions-item>
                <el-descriptions-item :label="t('order.detail.fields.memberName')">
                  {{ detail.memberName || '-' }}
                </el-descriptions-item>
                <el-descriptions-item :label="t('order.detail.fields.orderStatus')">
                  <el-tag :type="statusType(detail.orderStatus)">
                    {{ t(`dict.orderStatus.${detail.orderStatus}`) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item :label="t('order.detail.fields.paymentStatus')">
                  {{ paymentStatusLabel(detail.paymentStatus) }}
                </el-descriptions-item>
                <el-descriptions-item :label="t('order.detail.fields.appendOrder')">
                  {{ detail.isAppendOrder ? t('common.yes') : t('common.no') }}
                </el-descriptions-item>
                <el-descriptions-item :label="t('order.detail.fields.remark')" :span="2">
                  {{ detail.remark || '-' }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="9">
            <el-card shadow="never">
              <template #header>
                <span>{{ t('order.detail.sections.amount') }}</span>
              </template>
              <el-descriptions :column="1" border>
                <el-descriptions-item :label="t('order.detail.amounts.originalAmount')">
                  {{ formatCurrency(detail.originalAmountInCent) }}
                </el-descriptions-item>
                <el-descriptions-item :label="t('order.detail.amounts.discountAmount')">
                  {{ formatCurrency(detail.discountAmountInCent) }}
                </el-descriptions-item>
                <el-descriptions-item :label="t('order.detail.amounts.payableAmount')">
                  {{ formatCurrency(detail.payableAmountInCent) }}
                </el-descriptions-item>
                <el-descriptions-item :label="t('order.detail.amounts.paidAmount')">
                  {{ formatCurrency(detail.paidAmountInCent) }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
        </el-row>

        <el-card v-if="canManageOrder" shadow="never" class="section-row">
          <template #header>
            <span>{{ t('order.manage.title') }}</span>
          </template>
          <el-row :gutter="12">
            <el-col :xs="24" :lg="12">
              <el-form label-position="top">
                <el-form-item :label="t('order.manage.orderStatus')">
                  <el-select v-model="nextOrderStatus" :disabled="!canUpdateOrderStatus">
                    <el-option
                      v-for="item in orderStatusOptions"
                      :key="item"
                      :label="t(`dict.orderStatus.${item}`)"
                      :value="item"
                    />
                  </el-select>
                </el-form-item>
                <el-button
                  v-if="canUpdateOrderStatus"
                  type="primary"
                  :loading="savingOrderStatus"
                  :disabled="!nextOrderStatus || nextOrderStatus === detail.orderStatus"
                  @click="submitOrderStatus"
                >
                  {{ t('order.manage.saveOrderStatus') }}
                </el-button>
                <el-button
                  v-if="canCompleteOrder"
                  type="success"
                  :loading="completingOrder"
                  :disabled="detail.orderStatus === 'COMPLETED' || detail.paymentStatus !== 'PAID'"
                  @click="submitCompleteOrder"
                >
                  {{ t('order.manage.complete') }}
                </el-button>
              </el-form>
            </el-col>
            <el-col :xs="24" :lg="12">
              <el-form label-position="top">
                <el-form-item :label="t('order.manage.paymentStatus')">
                  <el-select v-model="nextPaymentStatus" :disabled="!canUpdatePaymentStatus">
                    <el-option
                      v-for="item in paymentStatusOptions"
                      :key="item"
                      :label="t(`dict.paymentStatus.${item}`)"
                      :value="item"
                    />
                  </el-select>
                </el-form-item>
                <el-button
                  v-if="canUpdatePaymentStatus"
                  type="primary"
                  :loading="savingPaymentStatus"
                  :disabled="!nextPaymentStatus || nextPaymentStatus === detail.paymentStatus"
                  @click="submitPaymentStatus"
                >
                  {{ t('order.manage.savePaymentStatus') }}
                </el-button>
              </el-form>
            </el-col>
          </el-row>
        </el-card>

        <el-card shadow="never" class="section-row">
          <template #header>
            <span>{{ t('order.detail.sections.items') }}</span>
          </template>
          <el-table :data="detail.items" stripe border>
            <el-table-column prop="productName" :label="t('order.detail.items.columns.productName')" min-width="180" />
            <el-table-column prop="skuName" :label="t('order.detail.items.columns.skuName')" min-width="160" />
            <el-table-column prop="quantity" :label="t('order.detail.items.columns.quantity')" min-width="100" />
            <el-table-column :label="t('order.detail.items.columns.unitPrice')" min-width="140">
              <template #default="{ row }">
                {{ formatCurrency(row.unitPriceInCent) }}
              </template>
            </el-table-column>
            <el-table-column :label="t('order.detail.items.columns.totalPrice')" min-width="140">
              <template #default="{ row }">
                {{ formatCurrency(row.totalPriceInCent) }}
              </template>
            </el-table-column>
            <el-table-column prop="remark" :label="t('order.detail.items.columns.remark')" min-width="200" />
          </el-table>
        </el-card>

        <el-card shadow="never" class="section-row">
          <template #header>
            <span>{{ t('order.detail.sections.appendRecords') }}</span>
          </template>
          <el-table :data="detail.appendRecords" stripe border>
            <el-table-column prop="appendNo" :label="t('order.detail.append.columns.appendNo')" min-width="160" />
            <el-table-column prop="operatorName" :label="t('order.detail.append.columns.operatorName')" min-width="140" />
            <el-table-column prop="quantity" :label="t('order.detail.append.columns.quantity')" min-width="100" />
            <el-table-column :label="t('order.detail.append.columns.amount')" min-width="140">
              <template #default="{ row }">
                {{ formatCurrency(row.amountInCent) }}
              </template>
            </el-table-column>
            <el-table-column prop="remark" :label="t('order.detail.append.columns.remark')" min-width="180" />
            <el-table-column prop="createdAt" :label="t('order.detail.append.columns.createdAt')" min-width="180" />
          </el-table>
        </el-card>

        <el-card shadow="never" class="section-row">
          <template #header>
            <span>{{ t('order.detail.sections.paymentRecords') }}</span>
          </template>
          <el-table :data="detail.paymentRecords" stripe border>
            <el-table-column prop="paymentNo" :label="t('order.detail.payment.columns.paymentNo')" min-width="180" />
            <el-table-column
              prop="paymentMethod"
              :label="t('order.detail.payment.columns.paymentMethod')"
              min-width="140"
            />
            <el-table-column
              prop="paymentStatus"
              :label="t('order.detail.payment.columns.paymentStatus')"
              min-width="140"
            >
              <template #default="{ row }">
                {{ paymentStatusLabel(row.paymentStatus) }}
              </template>
            </el-table-column>
            <el-table-column :label="t('order.detail.payment.columns.amount')" min-width="140">
              <template #default="{ row }">
                {{ formatCurrency(row.amountInCent) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="operatorName"
              :label="t('order.detail.payment.columns.operatorName')"
              min-width="140"
            />
            <el-table-column prop="paidAt" :label="t('order.detail.payment.columns.paidAt')" min-width="180" />
          </el-table>
        </el-card>
      </template>

      <el-empty v-else :description="t('order.detail.empty')" />

      <div class="actions">
        <el-button @click="goBack">{{ t('order.detail.actions.backToList') }}</el-button>
      </div>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ElMessage } from 'element-plus';
import type { OrderDetail } from '@/api/orders';
import { completeOrder, fetchOrderDetail, updateOrderPaymentStatus, updateOrderStatus } from '@/api/orders';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const { t } = useI18n();
const authStore = useAuthStore();

const loading = ref(false);
const errorMessage = ref('');
const detail = ref<OrderDetail | null>(null);
const savingOrderStatus = ref(false);
const savingPaymentStatus = ref(false);
const completingOrder = ref(false);
const nextOrderStatus = ref('');
const nextPaymentStatus = ref('');

const orderId = computed(() => Number(route.params.id));
const canUpdateOrderStatus = computed(
  () => authStore.hasPermission('order:update-status') || authStore.hasPermission('order:update'),
);
const canUpdatePaymentStatus = computed(
  () => authStore.hasPermission('order:update-payment') || authStore.hasPermission('order:update'),
);
const canCompleteOrder = computed(() => authStore.hasPermission('order:complete'));
const canManageOrder = computed(
  () => canUpdateOrderStatus.value || canUpdatePaymentStatus.value || canCompleteOrder.value,
);
const orderStatusOptions = [
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
const paymentStatusOptions = ['UNPAID', 'PARTIAL', 'PAID', 'REFUNDED'];

function formatCurrency(valueInCent: number): string {
  return `¥ ${(valueInCent / 100).toFixed(2)}`;
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

function paymentStatusLabel(status: string): string {
  if (!status) {
    return '-';
  }
  const key = `dict.paymentStatus.${status}`;
  const label = t(key);
  return label === key ? status : label;
}

function goBack() {
  router.push('/orders');
}

async function loadDetail() {
  if (!Number.isFinite(orderId.value) || orderId.value <= 0) {
    errorMessage.value = t('order.detail.invalidOrderId');
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  try {
    detail.value = await fetchOrderDetail(orderId.value);
    nextOrderStatus.value = detail.value.orderStatus || '';
    nextPaymentStatus.value = detail.value.paymentStatus || '';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

async function submitOrderStatus() {
  if (!detail.value || !nextOrderStatus.value || nextOrderStatus.value === detail.value.orderStatus) {
    return;
  }
  savingOrderStatus.value = true;
  try {
    await updateOrderStatus(orderId.value, { orderStatus: nextOrderStatus.value });
    ElMessage.success(t('order.manage.updateSuccess'));
    await loadDetail();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  } finally {
    savingOrderStatus.value = false;
  }
}

async function submitPaymentStatus() {
  if (!detail.value || !nextPaymentStatus.value || nextPaymentStatus.value === detail.value.paymentStatus) {
    return;
  }
  savingPaymentStatus.value = true;
  try {
    await updateOrderPaymentStatus(orderId.value, { paymentStatus: nextPaymentStatus.value });
    ElMessage.success(t('order.manage.updateSuccess'));
    await loadDetail();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  } finally {
    savingPaymentStatus.value = false;
  }
}

async function submitCompleteOrder() {
  if (!detail.value || detail.value.orderStatus === 'COMPLETED' || detail.value.paymentStatus !== 'PAID') {
    return;
  }
  completingOrder.value = true;
  try {
    await completeOrder(orderId.value);
    ElMessage.success(t('order.manage.updateSuccess'));
    await loadDetail();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  } finally {
    completingOrder.value = false;
  }
}

onMounted(loadDetail);
</script>

<style scoped>
.section-row {
  margin-bottom: 12px;
}

.actions {
  margin-top: 12px;
}
</style>
