<template>
  <PageShell :title="t('page.stores.title')" :description="t('page.stores.description')">
    <div v-loading="loading">
      <el-form :inline="true" class="filter-form">
        <el-form-item :label="t('store.filter.keyword')">
          <el-input v-model="keyword" :placeholder="t('store.filter.keywordPlaceholder')" clearable />
        </el-form-item>
        <el-form-item :label="t('store.filter.status')">
          <el-select v-model="statusFilter" clearable :placeholder="t('store.filter.statusPlaceholder')">
            <el-option :label="t('dict.storeStatus.OPEN')" value="OPEN" />
            <el-option :label="t('dict.storeStatus.REST')" value="REST" />
            <el-option :label="t('dict.storeStatus.DISABLED')" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStores">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

      <el-table :data="filteredStores" stripe border>
        <el-table-column prop="id" :label="t('store.columns.id')" min-width="100" />
        <el-table-column prop="name" :label="t('store.columns.name')" min-width="220" />
        <el-table-column prop="countryCode" :label="t('store.columns.countryCode')" min-width="120" />
        <el-table-column :label="t('store.columns.businessTypes')" min-width="220">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="item in row.businessTypes" :key="item" type="info">{{ item }}</el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column :label="t('store.columns.status')" min-width="160">
          <template #default="{ row }">
            <el-tag :type="statusType(row.businessStatus)">
              {{ t(`dict.storeStatus.${row.businessStatus}`) }}
            </el-tag>
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
import PageShell from '@/components/PageShell.vue';

const { t } = useI18n();

const loading = ref(false);
const errorMessage = ref('');
const keyword = ref('');
const statusFilter = ref('');
const stores = ref<StoreSummary[]>([]);

const filteredStores = computed(() =>
  stores.value.filter((item) => {
    const matchKeyword =
      !keyword.value ||
      item.name.toLowerCase().includes(keyword.value.toLowerCase()) ||
      String(item.id).includes(keyword.value);
    const matchStatus = !statusFilter.value || item.businessStatus === statusFilter.value;
    return matchKeyword && matchStatus;
  }),
);

function statusType(status: string): 'success' | 'warning' | 'danger' | 'info' {
  if (status === 'OPEN') {
    return 'success';
  }
  if (status === 'REST') {
    return 'warning';
  }
  if (status === 'DISABLED') {
    return 'danger';
  }
  return 'info';
}

function resetFilters() {
  keyword.value = '';
  statusFilter.value = '';
}

async function loadStores() {
  loading.value = true;
  errorMessage.value = '';
  try {
    stores.value = await fetchStores();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadStores();
});
</script>

<style scoped>
.filter-form {
  margin-bottom: 12px;
}
</style>
