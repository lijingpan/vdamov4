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
                  {{ detail.paymentStatus || '-' }}
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
            />
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
import type { OrderDetail } from '@/api/orders';
import { fetchOrderDetail } from '@/api/orders';
import PageShell from '@/components/PageShell.vue';

const route = useRoute();
const router = useRouter();
const { t } = useI18n();

const loading = ref(false);
const errorMessage = ref('');
const detail = ref<OrderDetail | null>(null);

const orderId = computed(() => Number(route.params.id));

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
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.requestFailed');
  } finally {
    loading.value = false;
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
