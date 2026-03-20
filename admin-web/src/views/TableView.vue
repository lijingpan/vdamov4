<template>
  <PageShell :title="t('page.tables.title')" :description="t('page.tables.description')">
    <div v-loading="loading">
      <el-form :inline="true" class="filter-form">
        <el-form-item :label="t('table.filter.store')">
          <el-select v-model="storeId" clearable :placeholder="t('table.filter.storePlaceholder')">
            <el-option
              v-for="item in storeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('table.filter.status')">
          <el-select v-model="statusFilter" clearable :placeholder="t('table.filter.statusPlaceholder')">
            <el-option
              v-for="item in statusOptions"
              :key="item"
              :label="t(`dict.tableStatus.${item}`)"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('table.filter.keyword')">
          <el-input v-model="keyword" clearable :placeholder="t('table.filter.keywordPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTables">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

      <el-row :gutter="12" class="stats-row">
        <el-col v-for="item in stats" :key="item.status" :xs="12" :md="4">
          <el-card shadow="hover" class="stats-card">
            <div>{{ t(`dict.tableStatus.${item.status}`) }}</div>
            <strong>{{ item.count }}</strong>
          </el-card>
        </el-col>
      </el-row>

      <el-table :data="filteredRows" stripe border>
        <el-table-column prop="storeName" :label="t('table.columns.storeName')" min-width="180" />
        <el-table-column prop="areaName" :label="t('table.columns.areaName')" min-width="140" />
        <el-table-column prop="tableName" :label="t('table.columns.tableName')" min-width="160" />
        <el-table-column prop="tableCode" :label="t('table.columns.tableCode')" min-width="150" />
        <el-table-column prop="capacity" :label="t('table.columns.capacity')" min-width="110" />
        <el-table-column :label="t('table.columns.status')" min-width="160">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ t(`dict.tableStatus.${row.status}`) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentOrderNo" :label="t('table.columns.currentOrderNo')" min-width="180" />
        <el-table-column :label="t('table.columns.currentAmount')" min-width="130">
          <template #default="{ row }">
            {{ formatCurrency(row.currentAmountInCent) }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import type { StoreSummary } from '@/api/stores';
import { fetchStores } from '@/api/stores';
import type { TableSummary } from '@/api/tables';
import { fetchTables } from '@/api/tables';
import PageShell from '@/components/PageShell.vue';

const { t } = useI18n();

const loading = ref(false);
const errorMessage = ref('');
const storeId = ref<number | undefined>();
const statusFilter = ref('');
const keyword = ref('');
const storeOptions = ref<StoreSummary[]>([]);
const tableRows = ref<TableSummary[]>([]);
const statusOptions = ['IDLE', 'IN_USE', 'WAITING_CHECKOUT', 'WAITING_CLEAN', 'DISABLED'];
const storeNameMap = computed(() =>
  storeOptions.value.reduce<Record<number, string>>((result, item) => {
    result[item.id] = item.name;
    return result;
  }, {}),
);
const filteredRows = computed(() =>
  tableRows.value
    .map((item) => ({
      ...item,
      storeName: item.storeName || storeNameMap.value[item.storeId] || '',
    }))
    .filter((item) => {
      const matchStatus = !statusFilter.value || item.status === statusFilter.value;
      const keywordValue = keyword.value.trim().toLowerCase();
      const matchKeyword =
        !keywordValue ||
        item.tableName.toLowerCase().includes(keywordValue) ||
        item.tableCode.toLowerCase().includes(keywordValue) ||
        item.areaName.toLowerCase().includes(keywordValue) ||
        item.currentOrderNo.toLowerCase().includes(keywordValue) ||
        item.storeName.toLowerCase().includes(keywordValue);
      return matchStatus && matchKeyword;
    }),
);

const stats = computed(() =>
  statusOptions.map((status) => ({
    status,
    count: filteredRows.value.filter((item) => item.status === status).length,
  })),
);

function statusTagType(status: string): 'success' | 'warning' | 'danger' | 'info' {
  if (status === 'IDLE') {
    return 'success';
  }
  if (status === 'IN_USE' || status === 'WAITING_CHECKOUT') {
    return 'warning';
  }
  if (status === 'DISABLED') {
    return 'danger';
  }
  return 'info';
}

function formatCurrency(valueInCent: number): string {
  return `¥ ${(valueInCent / 100).toFixed(2)}`;
}

function resetFilters() {
  storeId.value = undefined;
  statusFilter.value = '';
  keyword.value = '';
}

async function loadTables() {
  loading.value = true;
  errorMessage.value = '';
  try {
    tableRows.value = await fetchTables({
      storeId: storeId.value,
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
  await loadTables();
});
</script>

<style scoped>
.filter-form {
  margin-bottom: 12px;
}

.stats-row {
  margin-bottom: 12px;
}

.stats-card strong {
  display: block;
  margin-top: 6px;
  color: #1f3f73;
  font-size: 18px;
}
</style>
