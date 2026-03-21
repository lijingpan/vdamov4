<template>
  <PageShell :title="t('page.salesReports.title')" :description="t('page.salesReports.description')">
    <template #actions>
      <el-button :icon="RefreshRight" @click="loadReport">{{ t('common.refresh') }}</el-button>
      <el-button
        v-if="canExportSalesReport"
        type="primary"
        :icon="Download"
        :loading="exporting"
        @click="handleExport"
      >
        {{ t('common.export') }}
      </el-button>
    </template>

    <div v-loading="loading">
      <el-form :inline="true" class="filter-form">
        <el-form-item :label="t('salesReport.filter.store')">
          <el-select v-model="storeId" clearable :placeholder="t('salesReport.filter.storePlaceholder')">
            <el-option
              v-for="item in storeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('salesReport.filter.dateRange')">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            :start-placeholder="t('salesReport.filter.startDate')"
            :end-placeholder="t('salesReport.filter.endDate')"
            value-format="YYYY-MM-DD"
            unlink-panels
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadReport">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

      <el-row :gutter="12" class="summary-row">
        <el-col :xs="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('salesReport.summary.totalOrders')" :value="summary.totalOrders" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('salesReport.summary.completedOrders')" :value="summary.completedOrders" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('salesReport.summary.inProgressOrders')" :value="summary.inProgressOrders" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('salesReport.summary.revenue')" :value="formatCurrency(summary.revenueInCent)" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('salesReport.summary.paid')" :value="formatCurrency(summary.paidInCent)" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('salesReport.summary.discount')" :value="formatCurrency(summary.discountInCent)" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('salesReport.summary.appendOrders')" :value="summary.appendOrderCount" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="6">
          <el-card shadow="hover">
            <el-statistic
              :title="t('salesReport.summary.avgOrderAmount')"
              :value="formatCurrency(summary.averageOrderAmountInCent)"
            />
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="section-row">
        <template #header>
          <span>{{ t('salesReport.sections.byStore') }}</span>
        </template>
        <el-table :data="storeRows" stripe border>
          <el-table-column prop="storeName" :label="t('salesReport.byStore.columns.storeName')" min-width="180" />
          <el-table-column prop="totalOrders" :label="t('salesReport.byStore.columns.totalOrders')" min-width="110" />
          <el-table-column
            prop="completedOrders"
            :label="t('salesReport.byStore.columns.completedOrders')"
            min-width="110"
          />
          <el-table-column
            prop="inProgressOrders"
            :label="t('salesReport.byStore.columns.inProgressOrders')"
            min-width="110"
          />
          <el-table-column :label="t('salesReport.byStore.columns.revenue')" min-width="130">
            <template #default="{ row }">
              {{ formatCurrency(row.revenueInCent) }}
            </template>
          </el-table-column>
          <el-table-column :label="t('salesReport.byStore.columns.paid')" min-width="130">
            <template #default="{ row }">
              {{ formatCurrency(row.paidInCent) }}
            </template>
          </el-table-column>
          <el-table-column :label="t('salesReport.byStore.columns.discount')" min-width="130">
            <template #default="{ row }">
              {{ formatCurrency(row.discountInCent) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="appendOrderCount"
            :label="t('salesReport.byStore.columns.appendOrders')"
            min-width="110"
          />
          <el-table-column :label="t('salesReport.byStore.columns.avgOrderAmount')" min-width="130">
            <template #default="{ row }">
              {{ formatCurrency(row.averageOrderAmountInCent) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card shadow="never" class="section-row">
        <template #header>
          <span>{{ t('salesReport.sections.byDate') }}</span>
        </template>
        <el-table :data="dateRows" stripe border>
          <el-table-column prop="date" :label="t('salesReport.byDate.columns.date')" min-width="140" />
          <el-table-column prop="totalOrders" :label="t('salesReport.byDate.columns.totalOrders')" min-width="110" />
          <el-table-column
            prop="completedOrders"
            :label="t('salesReport.byDate.columns.completedOrders')"
            min-width="110"
          />
          <el-table-column
            prop="inProgressOrders"
            :label="t('salesReport.byDate.columns.inProgressOrders')"
            min-width="110"
          />
          <el-table-column :label="t('salesReport.byDate.columns.revenue')" min-width="130">
            <template #default="{ row }">
              {{ formatCurrency(row.revenueInCent) }}
            </template>
          </el-table-column>
          <el-table-column :label="t('salesReport.byDate.columns.paid')" min-width="130">
            <template #default="{ row }">
              {{ formatCurrency(row.paidInCent) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Download, RefreshRight } from '@element-plus/icons-vue';
import { useI18n } from 'vue-i18n';
import type { SalesByDate, SalesByStore, SalesSummary } from '@/api/sales-reports';
import { exportSalesReport, fetchSalesReport } from '@/api/sales-reports';
import type { StoreSummary } from '@/api/stores';
import { fetchStores } from '@/api/stores';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';

type DateRangeModel = [string, string] | [];

const { t } = useI18n();
const authStore = useAuthStore();

const loading = ref(false);
const exporting = ref(false);
const errorMessage = ref('');
const storeId = ref<number | undefined>();
const dateRange = ref<DateRangeModel>([]);
const storeOptions = ref<StoreSummary[]>([]);
const summary = ref<SalesSummary>({
  totalOrders: 0,
  completedOrders: 0,
  inProgressOrders: 0,
  revenueInCent: 0,
  paidInCent: 0,
  discountInCent: 0,
  appendOrderCount: 0,
  averageOrderAmountInCent: 0,
});
const storeRows = ref<SalesByStore[]>([]);
const dateRows = ref<SalesByDate[]>([]);
const canExportSalesReport = computed(() => authStore.hasPermission('sales.report:export'));

function formatCurrency(valueInCent: number): string {
  return `¥ ${(valueInCent / 100).toFixed(2)}`;
}

function resetFilters() {
  storeId.value = undefined;
  dateRange.value = [];
}

function buildQuery() {
  const [startDate, endDate] = dateRange.value.length === 2 ? dateRange.value : [undefined, undefined];
  return {
    storeId: storeId.value,
    startDate,
    endDate,
  };
}

async function loadStores() {
  try {
    storeOptions.value = await fetchStores();
  } catch {
    storeOptions.value = [];
  }
}

async function loadReport() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await fetchSalesReport(buildQuery());
    summary.value = response.summary;
    storeRows.value = response.byStore;
    dateRows.value = response.byDate;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

async function handleExport() {
  if (!canExportSalesReport.value) {
    return;
  }
  exporting.value = true;
  try {
    await exportSalesReport(buildQuery());
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  } finally {
    exporting.value = false;
  }
}

onMounted(async () => {
  await loadStores();
  await loadReport();
});
</script>

<style scoped>
.filter-form {
  margin-bottom: 12px;
}

.summary-row {
  margin-bottom: 12px;
}

.section-row {
  margin-bottom: 12px;
}
</style>
