<template>
  <PageShell :title="t('page.products.title')" :description="t('page.products.description')">
    <div v-loading="loading">
      <el-form :inline="true" class="filter-form">
        <el-form-item :label="t('product.filter.store')">
          <el-select v-model="storeId" clearable :placeholder="t('product.filter.storePlaceholder')">
            <el-option
              v-for="item in storeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('product.filter.category')">
          <el-select v-model="categoryFilter" clearable :placeholder="t('product.filter.categoryPlaceholder')">
            <el-option
              v-for="item in categoryOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('product.filter.status')">
          <el-select v-model="activeFilter" clearable :placeholder="t('product.filter.statusPlaceholder')">
            <el-option :label="t('dict.productStatus.ACTIVE')" :value="true" />
            <el-option :label="t('dict.productStatus.INACTIVE')" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('product.filter.keyword')">
          <el-input v-model="keyword" clearable :placeholder="t('product.filter.keywordPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadProducts">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

      <el-row :gutter="12" class="summary-row">
        <el-col :xs="12" :md="8">
          <el-card shadow="hover">
            <el-statistic :title="t('product.summary.total')" :value="filteredRows.length" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="8">
          <el-card shadow="hover">
            <el-statistic :title="t('product.summary.active')" :value="activeCount" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="8">
          <el-card shadow="hover">
            <el-statistic :title="t('product.summary.inactive')" :value="inactiveCount" />
          </el-card>
        </el-col>
      </el-row>

      <el-table :data="filteredRows" stripe border>
        <el-table-column prop="storeName" :label="t('product.columns.storeName')" min-width="180" />
        <el-table-column prop="name" :label="t('product.columns.name')" min-width="180" />
        <el-table-column prop="code" :label="t('product.columns.code')" min-width="160" />
        <el-table-column prop="categoryCode" :label="t('product.columns.categoryCode')" min-width="140" />
        <el-table-column :label="t('product.columns.price')" min-width="120">
          <template #default="{ row }">
            {{ formatCurrency(row.priceInCent) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('product.columns.status')" min-width="130">
          <template #default="{ row }">
            <el-tag :type="row.active ? 'success' : 'info'">
              {{ row.active ? t('dict.productStatus.ACTIVE') : t('dict.productStatus.INACTIVE') }}
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
import type { ProductSummary } from '@/api/products';
import { fetchProducts } from '@/api/products';
import type { StoreSummary } from '@/api/stores';
import { fetchStores } from '@/api/stores';
import PageShell from '@/components/PageShell.vue';

const { t } = useI18n();

const loading = ref(false);
const errorMessage = ref('');
const storeId = ref<number | undefined>();
const categoryFilter = ref('');
const activeFilter = ref<boolean | undefined>();
const keyword = ref('');
const rows = ref<ProductSummary[]>([]);
const storeOptions = ref<StoreSummary[]>([]);

const storeNameMap = computed(() =>
  storeOptions.value.reduce<Record<number, string>>((result, item) => {
    result[item.id] = item.name;
    return result;
  }, {}),
);

const categoryOptions = computed(() => {
  const set = new Set<string>();
  rows.value.forEach((item) => {
    if (item.categoryCode) {
      set.add(item.categoryCode);
    }
  });
  return Array.from(set);
});

const filteredRows = computed(() =>
  rows.value
    .map((item) => ({
      ...item,
      storeName: item.storeName || storeNameMap.value[item.storeId] || '',
    }))
    .filter((item) => {
      const matchCategory = !categoryFilter.value || item.categoryCode === categoryFilter.value;
      const matchStatus = activeFilter.value === undefined || item.active === activeFilter.value;
      const keywordValue = keyword.value.trim().toLowerCase();
      const matchKeyword =
        !keywordValue ||
        item.name.toLowerCase().includes(keywordValue) ||
        item.code.toLowerCase().includes(keywordValue) ||
        item.categoryCode.toLowerCase().includes(keywordValue) ||
        item.storeName.toLowerCase().includes(keywordValue);
      return matchCategory && matchStatus && matchKeyword;
    }),
);

const activeCount = computed(() => filteredRows.value.filter((item) => item.active).length);
const inactiveCount = computed(() => filteredRows.value.filter((item) => !item.active).length);

function formatCurrency(valueInCent: number): string {
  return `¥ ${(valueInCent / 100).toFixed(2)}`;
}

function resetFilters() {
  storeId.value = undefined;
  categoryFilter.value = '';
  activeFilter.value = undefined;
  keyword.value = '';
}

async function loadStores() {
  try {
    storeOptions.value = await fetchStores();
  } catch {
    storeOptions.value = [];
  }
}

async function loadProducts() {
  loading.value = true;
  errorMessage.value = '';
  try {
    rows.value = await fetchProducts({
      storeId: storeId.value,
      categoryCode: categoryFilter.value || undefined,
      active: activeFilter.value,
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
  await loadProducts();
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
