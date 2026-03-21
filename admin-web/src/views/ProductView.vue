<template>
  <PageShell :title="t('page.products.title')" :description="t('page.products.description')">
    <template #actions>
      <el-button :icon="RefreshRight" @click="loadData">{{ t('common.refresh') }}</el-button>
      <el-button v-if="canCreateProduct" type="primary" :icon="Plus" @click="openCreateDialog">
        {{ t('product.toolbar.add') }}
      </el-button>
    </template>

    <template #meta>
      <div class="stats-grid stats-grid--three">
        <div class="stat-card">
          <div class="stat-card__label">{{ t('product.summary.total') }}</div>
          <div class="stat-card__value">{{ rows.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('product.summary.active') }}</div>
          <div class="stat-card__value">{{ activeCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('product.summary.inactive') }}</div>
          <div class="stat-card__value">{{ inactiveCount }}</div>
        </div>
      </div>
    </template>

    <el-card shadow="never" class="page-card filter-card">
      <el-form :inline="true">
        <el-form-item :label="t('product.filter.store')">
          <el-select v-model="filters.storeId" clearable :placeholder="t('product.filter.storePlaceholder')">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('product.filter.category')">
          <el-select v-model="filters.categoryCode" clearable :placeholder="t('product.filter.categoryPlaceholder')">
            <el-option
              v-for="item in categoryFilterOptions"
              :key="`${item.storeId}-${item.categoryCode}`"
              :label="item.categoryName || item.categoryCode"
              :value="item.categoryCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('product.filter.status')">
          <el-select v-model="filters.active" clearable :placeholder="t('product.filter.statusPlaceholder')">
            <el-option :label="t('dict.productStatus.ACTIVE')" :value="true" />
            <el-option :label="t('dict.productStatus.INACTIVE')" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('product.filter.keyword')">
          <el-input
            v-model="filters.keyword"
            clearable
            :placeholder="t('product.filter.keywordPlaceholder')"
            @keyup.enter="loadProducts"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadProducts">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page-card data-card">
      <div class="action-bar">
        <div class="action-bar__hint">{{ t('page.products.description') }}</div>
      </div>

      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="false"
        style="margin: 16px 0"
      />

      <el-table v-loading="loading" :data="tableRows" stripe>
        <el-table-column prop="storeName" :label="t('product.columns.storeName')" min-width="180" />
        <el-table-column prop="name" :label="t('product.columns.name')" min-width="180" />
        <el-table-column prop="code" :label="t('product.columns.code')" min-width="160" />
        <el-table-column prop="categoryCode" :label="t('product.columns.categoryCode')" min-width="160" />
        <el-table-column :label="t('product.columns.price')" min-width="120">
          <template #default="{ row }">
            {{ formatCurrency(row.priceInCent) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('product.columns.status')" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.active ? 'success' : 'danger'">
              {{ row.active ? t('dict.productStatus.ACTIVE') : t('dict.productStatus.INACTIVE') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canOperateProduct" :label="t('common.actions')" min-width="180" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="canUpdateProduct" link type="primary" @click="openEditDialog(row)">
                {{ t('common.edit') }}
              </el-button>
              <el-button
                v-if="canUpdateProductStatus && !row.active"
                link
                type="success"
                @click="changeStatus(row, true)"
              >
                {{ t('product.actions.enable') }}
              </el-button>
              <el-button
                v-if="canUpdateProductStatus && row.active"
                link
                type="danger"
                @click="changeStatus(row, false)"
              >
                {{ t('product.actions.disable') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? t('product.toolbar.add') : t('common.edit')"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <el-form-item :label="t('product.form.store')" prop="storeId">
          <el-select v-model="form.storeId" :placeholder="t('product.form.storePlaceholder')">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('product.form.name')" prop="name">
          <el-input v-model="form.name" :placeholder="t('product.form.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('product.form.code')" prop="code">
          <el-input v-model="form.code" :placeholder="t('product.form.codePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('product.form.categoryCode')" prop="categoryCode">
          <el-select v-model="form.categoryCode" :placeholder="t('product.form.categoryCodePlaceholder')">
            <el-option
              v-for="item in categoryFormOptions"
              :key="`${item.storeId}-${item.categoryCode}`"
              :label="item.categoryName || item.categoryCode"
              :value="item.categoryCode"
            />
          </el-select>
          <div v-if="form.storeId && categoryFormOptions.length === 0" class="helper-text">
            {{ t('product.form.emptyCategories') }}
          </div>
        </el-form-item>
        <el-form-item :label="t('product.form.priceInCent')" prop="priceInCent">
          <el-input-number v-model="form.priceInCent" :min="0" :step="100" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('product.form.active')" prop="active">
          <el-switch v-model="form.active" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { Plus, RefreshRight } from '@element-plus/icons-vue';
import { useI18n } from 'vue-i18n';
import {
  createProduct,
  fetchProducts,
  updateProduct,
  updateProductStatus,
  type ProductPayload,
  type ProductSummary,
} from '@/api/products';
import { fetchProductCategories, type ProductCategorySummary } from '@/api/product-categories';
import { fetchStores, type StoreSummary } from '@/api/stores';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';

interface ProductFilters {
  storeId?: number;
  categoryCode: string;
  active?: boolean;
  keyword: string;
}

interface ProductFormModel {
  id?: number;
  storeId?: number;
  name: string;
  code: string;
  categoryCode: string;
  priceInCent: number;
  active: boolean;
}

const { t } = useI18n();
const authStore = useAuthStore();

const loading = ref(false);
const submitting = ref(false);
const errorMessage = ref('');
const rows = ref<ProductSummary[]>([]);
const storeOptions = ref<StoreSummary[]>([]);
const categoryOptions = ref<ProductCategorySummary[]>([]);
const formRef = ref<FormInstance>();
const dialogVisible = ref(false);
const dialogMode = ref<'create' | 'edit'>('create');

const filters = reactive<ProductFilters>({
  categoryCode: '',
  keyword: '',
});
const form = reactive<ProductFormModel>({
  name: '',
  code: '',
  categoryCode: '',
  priceInCent: 0,
  active: true,
});

const canCreateProduct = computed(() => authStore.hasPermission('product:create'));
const canUpdateProduct = computed(() => authStore.hasPermission('product:update'));
const canUpdateProductStatus = computed(() => authStore.hasPermission('product:status'));
const canOperateProduct = computed(() => canUpdateProduct.value || canUpdateProductStatus.value);
const activeCount = computed(() => rows.value.filter((item) => item.active).length);
const inactiveCount = computed(() => rows.value.filter((item) => !item.active).length);
const storeNameMap = computed(() =>
  storeOptions.value.reduce<Record<number, string>>((result, item) => {
    result[item.id] = item.name;
    return result;
  }, {}),
);
const categoryFilterOptions = computed(() =>
  categoryOptions.value.filter((item) => !filters.storeId || item.storeId === filters.storeId),
);
const categoryFormOptions = computed(() =>
  categoryOptions.value.filter((item) => !form.storeId || item.storeId === form.storeId),
);
const tableRows = computed(() =>
  rows.value.map((item) => ({
    ...item,
    storeName: item.storeName || storeNameMap.value[item.storeId] || '',
  })),
);

const rules: FormRules<ProductFormModel> = {
  storeId: [{ required: true, message: t('product.form.storePlaceholder'), trigger: 'change' }],
  name: [{ required: true, message: t('product.form.namePlaceholder'), trigger: 'blur' }],
  code: [{ required: true, message: t('product.form.codePlaceholder'), trigger: 'blur' }],
  categoryCode: [{ required: true, message: t('product.form.categoryCodePlaceholder'), trigger: 'change' }],
  priceInCent: [{ required: true, message: t('product.form.priceInCentPlaceholder'), trigger: 'change' }],
};

watch(
  () => filters.storeId,
  (value) => {
    if (!value) {
      return;
    }
    if (filters.categoryCode && !categoryFilterOptions.value.some((item) => item.categoryCode === filters.categoryCode)) {
      filters.categoryCode = '';
    }
  },
);

watch(
  () => form.storeId,
  (value) => {
    if (!value) {
      form.categoryCode = '';
      return;
    }
    if (form.categoryCode && !categoryFormOptions.value.some((item) => item.categoryCode === form.categoryCode)) {
      form.categoryCode = '';
    }
  },
);

function formatCurrency(valueInCent: number): string {
  return `$${(valueInCent / 100).toFixed(2)}`;
}

function resetFilters() {
  filters.storeId = undefined;
  filters.categoryCode = '';
  filters.active = undefined;
  filters.keyword = '';
  loadProducts();
}

function resetForm() {
  form.id = undefined;
  form.storeId = storeOptions.value.length === 1 ? storeOptions.value[0].id : undefined;
  form.name = '';
  form.code = '';
  form.categoryCode = '';
  form.priceInCent = 0;
  form.active = true;
  formRef.value?.clearValidate();
}

function openCreateDialog() {
  if (!canCreateProduct.value) {
    return;
  }
  dialogMode.value = 'create';
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(row: ProductSummary) {
  if (!canUpdateProduct.value) {
    return;
  }
  dialogMode.value = 'edit';
  form.id = row.id;
  form.storeId = row.storeId;
  form.name = row.name;
  form.code = row.code;
  form.categoryCode = row.categoryCode;
  form.priceInCent = row.priceInCent;
  form.active = row.active;
  dialogVisible.value = true;
}

async function loadStores() {
  try {
    storeOptions.value = await fetchStores();
  } catch {
    storeOptions.value = [];
  }
}

async function loadCategories() {
  try {
    categoryOptions.value = await fetchProductCategories();
  } catch {
    categoryOptions.value = [];
  }
}

async function loadProducts() {
  loading.value = true;
  errorMessage.value = '';
  try {
    rows.value = await fetchProducts({
      storeId: filters.storeId,
      categoryCode: filters.categoryCode || undefined,
      active: filters.active,
      keyword: filters.keyword || undefined,
    });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

async function loadData() {
  await Promise.all([loadStores(), loadCategories()]);
  await loadProducts();
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid || !form.storeId) {
    return;
  }

  const payload: ProductPayload = {
    storeId: form.storeId,
    name: form.name.trim(),
    code: form.code.trim(),
    categoryCode: form.categoryCode.trim(),
    priceInCent: form.priceInCent,
    active: form.active,
  };

  submitting.value = true;
  try {
    if (dialogMode.value === 'create') {
      if (!canCreateProduct.value) {
        return;
      }
      await createProduct(payload);
    } else if (form.id) {
      if (!canUpdateProduct.value) {
        return;
      }
      await updateProduct(form.id, payload);
    }
    dialogVisible.value = false;
    ElMessage.success(t('common.save'));
    await loadData();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  } finally {
    submitting.value = false;
  }
}

async function changeStatus(row: ProductSummary, nextActive: boolean) {
  if (!canUpdateProductStatus.value) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      `${row.name} - ${nextActive ? t('product.actions.enable') : t('product.actions.disable')}`,
      t('common.actions'),
      { type: 'warning' },
    );
    await updateProductStatus(row.id, nextActive);
    ElMessage.success(t('common.save'));
    await loadProducts();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  }
}

onMounted(() => {
  loadData();
});
</script>
