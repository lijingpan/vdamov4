<template>
  <PageShell :title="t('page.productCategories.title')" :description="t('page.productCategories.description')">
    <template #actions>
      <el-button :icon="RefreshRight" @click="loadData">{{ t('common.refresh') }}</el-button>
      <el-button v-if="canCreateProductCategory" type="primary" :icon="Plus" @click="openCreateDialog">
        {{ t('productCategory.toolbar.add') }}
      </el-button>
    </template>

    <template #meta>
      <div class="stats-grid stats-grid--three">
        <div class="stat-card">
          <div class="stat-card__label">{{ t('productCategory.summary.total') }}</div>
          <div class="stat-card__value">{{ rows.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('productCategory.summary.enabled') }}</div>
          <div class="stat-card__value">{{ enabledCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('productCategory.summary.productCount') }}</div>
          <div class="stat-card__value">{{ totalProductCount }}</div>
        </div>
      </div>
    </template>

    <el-card shadow="never" class="page-card filter-card">
      <el-form :inline="true">
        <el-form-item :label="t('productCategory.filter.store')">
          <el-select v-model="filters.storeId" clearable :placeholder="t('productCategory.filter.storePlaceholder')">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCategory.filter.enabled')">
          <el-select v-model="filters.enabled" clearable :placeholder="t('productCategory.filter.enabledPlaceholder')">
            <el-option :label="t('dict.enableStatus.ENABLED')" :value="true" />
            <el-option :label="t('dict.enableStatus.DISABLED')" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCategory.filter.keyword')">
          <el-input
            v-model="filters.keyword"
            clearable
            :placeholder="t('productCategory.filter.keywordPlaceholder')"
            @keyup.enter="loadRows"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRows">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page-card data-card">
      <div class="action-bar">
        <div class="action-bar__hint">{{ t('page.productCategories.description') }}</div>
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
        <el-table-column
          v-if="canOperateProductCategory"
          :label="t('common.actions')"
          min-width="190"
          fixed="right"
        >
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="canUpdateProductCategory" link type="primary" @click="openEditDialog(row)">
                {{ t('common.edit') }}
              </el-button>
              <el-button
                v-if="canEnableProductCategory && !row.enabled"
                link
                type="success"
                @click="toggleEnabled(row, true)"
              >
                {{ t('productCategory.actions.enable') }}
              </el-button>
              <el-button
                v-if="canEnableProductCategory && row.enabled"
                link
                type="danger"
                @click="toggleEnabled(row, false)"
              >
                {{ t('productCategory.actions.disable') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? t('productCategory.toolbar.add') : t('common.edit')"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <el-form-item :label="t('productCategory.form.store')" prop="storeId">
          <el-select v-model="form.storeId" :placeholder="t('productCategory.form.storePlaceholder')">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCategory.form.categoryName')" prop="categoryName">
          <el-input v-model="form.categoryName" :placeholder="t('productCategory.form.categoryNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('productCategory.form.categoryCode')" prop="categoryCode">
          <el-input v-model="form.categoryCode" :placeholder="t('productCategory.form.categoryCodePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('productCategory.form.sortOrder')" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :step="1" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('productCategory.form.enabled')" prop="enabled">
          <el-switch v-model="form.enabled" />
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
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { Plus, RefreshRight } from '@element-plus/icons-vue';
import { useI18n } from 'vue-i18n';
import {
  createProductCategory,
  fetchProductCategories,
  updateProductCategory,
  updateProductCategoryEnabled,
  type ProductCategoryPayload,
  type ProductCategorySummary,
} from '@/api/product-categories';
import { fetchStores, type StoreSummary } from '@/api/stores';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';

interface CategoryFilters {
  storeId?: number;
  enabled?: boolean;
  keyword: string;
}

interface CategoryFormModel {
  id?: number;
  storeId?: number;
  categoryName: string;
  categoryCode: string;
  sortOrder: number;
  enabled: boolean;
}

const { t } = useI18n();
const authStore = useAuthStore();

const loading = ref(false);
const submitting = ref(false);
const errorMessage = ref('');
const rows = ref<ProductCategorySummary[]>([]);
const storeOptions = ref<StoreSummary[]>([]);
const formRef = ref<FormInstance>();
const dialogVisible = ref(false);
const dialogMode = ref<'create' | 'edit'>('create');

const filters = reactive<CategoryFilters>({
  keyword: '',
});
const form = reactive<CategoryFormModel>({
  categoryName: '',
  categoryCode: '',
  sortOrder: 10,
  enabled: true,
});

const canCreateProductCategory = computed(() => authStore.hasPermission('product.category:create'));
const canUpdateProductCategory = computed(() => authStore.hasPermission('product.category:update'));
const canEnableProductCategory = computed(() => authStore.hasPermission('product.category:enable'));
const canOperateProductCategory = computed(() => canUpdateProductCategory.value || canEnableProductCategory.value);
const enabledCount = computed(() => rows.value.filter((item) => item.enabled).length);
const totalProductCount = computed(() => rows.value.reduce((sum, item) => sum + item.productCount, 0));
const storeNameMap = computed(() =>
  storeOptions.value.reduce<Record<number, string>>((result, item) => {
    result[item.id] = item.name;
    return result;
  }, {}),
);
const tableRows = computed(() =>
  rows.value.map((item) => ({
    ...item,
    storeName: item.storeName || storeNameMap.value[item.storeId] || '',
  })),
);

const rules: FormRules<CategoryFormModel> = {
  storeId: [{ required: true, message: t('productCategory.form.storePlaceholder'), trigger: 'change' }],
  categoryName: [{ required: true, message: t('productCategory.form.categoryNamePlaceholder'), trigger: 'blur' }],
  categoryCode: [{ required: true, message: t('productCategory.form.categoryCodePlaceholder'), trigger: 'blur' }],
  sortOrder: [{ required: true, message: t('productCategory.form.sortOrder'), trigger: 'change' }],
};

function resetFilters() {
  filters.storeId = undefined;
  filters.enabled = undefined;
  filters.keyword = '';
  loadRows();
}

function resetForm() {
  form.id = undefined;
  form.storeId = storeOptions.value.length === 1 ? storeOptions.value[0].id : undefined;
  form.categoryName = '';
  form.categoryCode = '';
  form.sortOrder = 10;
  form.enabled = true;
  formRef.value?.clearValidate();
}

function openCreateDialog() {
  if (!canCreateProductCategory.value) {
    return;
  }
  dialogMode.value = 'create';
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(row: ProductCategorySummary) {
  if (!canUpdateProductCategory.value) {
    return;
  }
  dialogMode.value = 'edit';
  form.id = row.id;
  form.storeId = row.storeId;
  form.categoryName = row.categoryName;
  form.categoryCode = row.categoryCode;
  form.sortOrder = row.sortOrder;
  form.enabled = row.enabled;
  dialogVisible.value = true;
}

async function loadStores() {
  try {
    storeOptions.value = await fetchStores();
  } catch {
    storeOptions.value = [];
  }
}

async function loadRows() {
  loading.value = true;
  errorMessage.value = '';
  try {
    rows.value = await fetchProductCategories({
      storeId: filters.storeId,
      keyword: filters.keyword || undefined,
      enabled: filters.enabled,
    });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

async function loadData() {
  await loadStores();
  await loadRows();
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid || !form.storeId) {
    return;
  }

  const payload: ProductCategoryPayload = {
    storeId: form.storeId,
    categoryName: form.categoryName.trim(),
    categoryCode: form.categoryCode.trim(),
    sortOrder: form.sortOrder,
    enabled: form.enabled,
  };

  submitting.value = true;
  try {
    if (dialogMode.value === 'create') {
      if (!canCreateProductCategory.value) {
        return;
      }
      await createProductCategory(payload);
    } else if (form.id) {
      if (!canUpdateProductCategory.value) {
        return;
      }
      await updateProductCategory(form.id, payload);
    }
    dialogVisible.value = false;
    ElMessage.success(t('common.save'));
    await loadRows();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  } finally {
    submitting.value = false;
  }
}

async function toggleEnabled(row: ProductCategorySummary, enabled: boolean) {
  if (!canEnableProductCategory.value) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      `${row.categoryName} - ${enabled ? t('productCategory.actions.enable') : t('productCategory.actions.disable')}`,
      t('common.actions'),
      { type: 'warning' },
    );
    await updateProductCategoryEnabled(row.id, enabled);
    ElMessage.success(t('common.save'));
    await loadRows();
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
