<template>
  <PageShell :title="t('page.devices.title')" :description="t('page.devices.description')">
    <div v-loading="loading">
      <el-form :inline="true" class="filter-form">
        <el-form-item :label="t('device.filter.store')">
          <el-select v-model="storeId" clearable :placeholder="t('device.filter.storePlaceholder')">
            <el-option
              v-for="item in storeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('device.filter.type')">
          <el-select v-model="typeFilter" clearable :placeholder="t('device.filter.typePlaceholder')">
            <el-option
              v-for="item in deviceTypes"
              :key="item"
              :label="t(`dict.deviceType.${item}`)"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('device.filter.status')">
          <el-select v-model="enabledFilter" clearable :placeholder="t('device.filter.statusPlaceholder')">
            <el-option :label="t('dict.enableStatus.ENABLED')" :value="true" />
            <el-option :label="t('dict.enableStatus.DISABLED')" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('device.filter.keyword')">
          <el-input v-model="keyword" clearable :placeholder="t('device.filter.keywordPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadDevices">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

      <el-row :gutter="12" class="summary-row">
        <el-col :xs="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('device.summary.total')" :value="filteredRows.length" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('device.summary.enabled')" :value="enabledCount" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('device.summary.disabled')" :value="disabledCount" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('device.summary.printers')" :value="printerCount" />
          </el-card>
        </el-col>
      </el-row>

      <el-table :data="filteredRows" stripe border>
        <el-table-column prop="storeName" :label="t('device.columns.storeName')" min-width="180" />
        <el-table-column prop="name" :label="t('device.columns.name')" min-width="180" />
        <el-table-column :label="t('device.columns.type')" min-width="150">
          <template #default="{ row }">
            {{ t(`dict.deviceType.${row.type}`) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('device.columns.purpose')" min-width="160">
          <template #default="{ row }">
            {{ t(`dict.devicePurpose.${row.purpose}`) }}
          </template>
        </el-table-column>
        <el-table-column prop="brand" :label="t('device.columns.brand')" min-width="140" />
        <el-table-column prop="sn" :label="t('device.columns.sn')" min-width="180" />
        <el-table-column prop="size" :label="t('device.columns.size')" min-width="120" />
        <el-table-column :label="t('device.columns.onlineStatus')" min-width="140">
          <template #default="{ row }">
            <el-tag :type="row.onlineStatus === 'ONLINE' ? 'success' : 'warning'">
              {{ t(`dict.onlineStatus.${row.onlineStatus}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('device.columns.status')" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? t('dict.enableStatus.ENABLED') : t('dict.enableStatus.DISABLED') }}
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
import type { DeviceSummary } from '@/api/devices';
import { fetchDevices } from '@/api/devices';
import type { StoreSummary } from '@/api/stores';
import { fetchStores } from '@/api/stores';
import PageShell from '@/components/PageShell.vue';

const { t } = useI18n();

const loading = ref(false);
const errorMessage = ref('');
const storeId = ref<number | undefined>();
const typeFilter = ref('');
const enabledFilter = ref<boolean | undefined>();
const keyword = ref('');
const storeOptions = ref<StoreSummary[]>([]);
const rows = ref<DeviceSummary[]>([]);
const deviceTypes = ['PRINTER', 'SPEAKER'];

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
      const matchType = !typeFilter.value || item.type === typeFilter.value;
      const matchEnabled = enabledFilter.value === undefined || item.enabled === enabledFilter.value;
      const keywordValue = keyword.value.trim().toLowerCase();
      const matchKeyword =
        !keywordValue ||
        item.name.toLowerCase().includes(keywordValue) ||
        item.brand.toLowerCase().includes(keywordValue) ||
        item.sn.toLowerCase().includes(keywordValue) ||
        item.storeName.toLowerCase().includes(keywordValue);
      return matchType && matchEnabled && matchKeyword;
    }),
);

const enabledCount = computed(() => filteredRows.value.filter((item) => item.enabled).length);
const disabledCount = computed(() => filteredRows.value.filter((item) => !item.enabled).length);
const printerCount = computed(() => filteredRows.value.filter((item) => item.type.includes('PRINTER')).length);

function resetFilters() {
  storeId.value = undefined;
  typeFilter.value = '';
  enabledFilter.value = undefined;
  keyword.value = '';
}

async function loadStores() {
  try {
    storeOptions.value = await fetchStores();
  } catch {
    storeOptions.value = [];
  }
}

async function loadDevices() {
  loading.value = true;
  errorMessage.value = '';
  try {
    rows.value = await fetchDevices({
      storeId: storeId.value,
      type: typeFilter.value || undefined,
      enabled: enabledFilter.value,
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
  await loadDevices();
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
