<template>
  <PageShell :title="t('page.tableAreas.title')" :description="t('page.tableAreas.description')">
    <div v-loading="loading">
      <el-form :inline="true" class="filter-form">
        <el-form-item :label="t('tableArea.filter.store')">
          <el-select v-model="storeId" clearable :placeholder="t('tableArea.filter.storePlaceholder')">
            <el-option
              v-for="item in storeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('tableArea.filter.keyword')">
          <el-input v-model="keyword" clearable :placeholder="t('tableArea.filter.keywordPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTableAreas">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

      <el-row :gutter="12" class="summary-row">
        <el-col :xs="12" :md="8">
          <el-card shadow="hover">
            <el-statistic :title="t('tableArea.summary.total')" :value="filteredRows.length" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="8">
          <el-card shadow="hover">
            <el-statistic :title="t('tableArea.summary.enabled')" :value="enabledCount" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="8">
          <el-card shadow="hover">
            <el-statistic :title="t('tableArea.summary.tableCount')" :value="totalTableCount" />
          </el-card>
        </el-col>
      </el-row>

      <el-table :data="filteredRows" stripe border>
        <el-table-column prop="storeName" :label="t('tableArea.columns.storeName')" min-width="180" />
        <el-table-column prop="areaName" :label="t('tableArea.columns.areaName')" min-width="180" />
        <el-table-column prop="areaCode" :label="t('tableArea.columns.areaCode')" min-width="160" />
        <el-table-column prop="sortOrder" :label="t('tableArea.columns.sortOrder')" min-width="100" />
        <el-table-column :label="t('tableArea.columns.enabled')" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? t('dict.enableStatus.ENABLED') : t('dict.enableStatus.DISABLED') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tableCount" :label="t('tableArea.columns.tableCount')" min-width="120" />
      </el-table>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import type { StoreSummary } from '@/api/stores';
import { fetchStores } from '@/api/stores';
import type { TableAreaSummary } from '@/api/table-areas';
import { fetchTableAreas } from '@/api/table-areas';
import PageShell from '@/components/PageShell.vue';

const { t } = useI18n();

const loading = ref(false);
const errorMessage = ref('');
const storeId = ref<number | undefined>();
const keyword = ref('');
const storeOptions = ref<StoreSummary[]>([]);
const rows = ref<TableAreaSummary[]>([]);

const storeNameMap = computed(() =>
  storeOptions.value.reduce<Record<number, string>>((result, item) => {
    result[item.id] = item.name;
    return result;
  }, {}),
);

const filteredRows = computed(() =>
  rows.value
    .map((item) => ({
      ...item,
      storeName: item.storeName || storeNameMap.value[item.storeId] || '',
    }))
    .filter((item) => {
      const keywordValue = keyword.value.trim().toLowerCase();
      return (
        !keywordValue ||
        item.areaName.toLowerCase().includes(keywordValue) ||
        item.areaCode.toLowerCase().includes(keywordValue) ||
        item.storeName.toLowerCase().includes(keywordValue)
      );
    }),
);

const enabledCount = computed(() => filteredRows.value.filter((item) => item.enabled).length);
const totalTableCount = computed(() => filteredRows.value.reduce((sum, item) => sum + item.tableCount, 0));

function resetFilters() {
  storeId.value = undefined;
  keyword.value = '';
}

async function loadStores() {
  try {
    storeOptions.value = await fetchStores();
  } catch {
    storeOptions.value = [];
  }
}

async function loadTableAreas() {
  loading.value = true;
  errorMessage.value = '';
  try {
    rows.value = await fetchTableAreas({
      storeId: storeId.value,
      keyword: keyword.value || undefined,
    });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  await loadStores();
  await loadTableAreas();
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
