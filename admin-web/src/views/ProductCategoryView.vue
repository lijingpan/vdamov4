<template>
  <PageShell :title="t('page.productCategories.title')" :description="t('page.productCategories.description')">
    <div v-loading="loading">
      <el-form :inline="true" class="filter-form">
        <el-form-item :label="t('productCategory.filter.store')">
          <el-select v-model="storeId" clearable :placeholder="t('productCategory.filter.storePlaceholder')">
            <el-option
              v-for="item in storeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCategory.filter.enabled')">
          <el-select v-model="enabledFilter" clearable :placeholder="t('productCategory.filter.enabledPlaceholder')">
            <el-option :label="t('dict.enableStatus.ENABLED')" :value="true" />
            <el-option :label="t('dict.enableStatus.DISABLED')" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCategory.filter.keyword')">
          <el-input v-model="keyword" clearable :placeholder="t('productCategory.filter.keywordPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadProductCategories">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

      <el-row :gutter="12" class="summary-row">
        <el-col :xs="12" :md="8">
          <el-card shadow="hover">
            <el-statistic :title="t('productCategory.summary.total')" :value="filteredRows.length" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="8">
          <el-card shadow="hover">
            <el-statistic :title="t('productCategory.summary.enabled')" :value="enabledCount" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="8">
          <el-card shadow="hover">
            <el-statistic :title="t('productCategory.summary.productCount')" :value="totalProductCount" />
          </el-card>
        </el-col>
      </el-row>

      <el-table :data="filteredRows" stripe border>
        <el-table-column prop="storeName" :label="t('productCategory.columns.storeName')" min-width="180" />
        <el-table-column prop="categoryName" :label="t('productCategory.columns.categoryName')" min-width="180" />
        <el-table-column prop="categoryCode" :label="t('productCategory.columns.categoryCode')" min-width="160" />
        <el-table-column prop="sortOrder" :label="t('productCategory.columns.sortOrder')" min-width="100" />
        <el-table-column :label="t('productCategory.columns.enabled')" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? t('dict.enableStatus.ENABLED') : t('dict.enableStatus.DISABLED') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="productCount" :label="t('productCategory.columns.productCount')" min-width="120" />
      </el-table>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import type { ProductCategorySummary } from '@/api/product-categories';
import { fetchProductCategories } from '@/api/product-categories';
import type { StoreSummary } from '@/api/stores';
import { fetchStores } from '@/api/stores';
import PageShell from '@/components/PageShell.vue';

const { t } = useI18n();

const loading = ref(false);
const errorMessage = ref('');
const storeId = ref<number | undefined>();
const enabledFilter = ref<boolean | undefined>();
const keyword = ref('');
const rows = ref<ProductCategorySummary[]>([]);
const storeOptions = ref<StoreSummary[]>([]);

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
      const matchEnabled = enabledFilter.value === undefined || item.enabled === enabledFilter.value;
      const keywordValue = keyword.value.trim().toLowerCase();
      const matchKeyword =
        !keywordValue ||
        item.categoryName.toLowerCase().includes(keywordValue) ||
        item.categoryCode.toLowerCase().includes(keywordValue) ||
        item.storeName.toLowerCase().includes(keywordValue);
      return matchEnabled && matchKeyword;
    }),
);

const enabledCount = computed(() => filteredRows.value.filter((item) => item.enabled).length);
const totalProductCount = computed(() => filteredRows.value.reduce((sum, item) => sum + item.productCount, 0));

function resetFilters() {
  storeId.value = undefined;
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

async function loadProductCategories() {
  loading.value = true;
  errorMessage.value = '';
  try {
    rows.value = await fetchProductCategories({
      storeId: storeId.value,
      keyword: keyword.value || undefined,
      enabled: enabledFilter.value,
    });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  await loadStores();
  await loadProductCategories();
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
