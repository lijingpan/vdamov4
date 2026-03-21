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
        <el-form-item :label="t('product.filter.type')">
          <el-select v-model="filters.productType" clearable :placeholder="t('product.filter.typePlaceholder')">
            <el-option v-for="item in productTypeOptions" :key="item" :label="t(`dict.productType.${item}`)" :value="item" />
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
        <el-table-column prop="storeName" :label="t('product.columns.storeName')" min-width="160" />
        <el-table-column prop="name" :label="t('product.columns.name')" min-width="180" />
        <el-table-column prop="code" :label="t('product.columns.code')" min-width="150" />
        <el-table-column prop="categoryCode" :label="t('product.columns.categoryCode')" min-width="140" />
        <el-table-column :label="t('product.columns.type')" min-width="130">
          <template #default="{ row }">
            <el-tag>{{ t(`dict.productType.${row.productType}`) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('product.columns.specMode')" min-width="130">
          <template #default="{ row }">
            <el-tag type="info">{{ t(`dict.productSpecMode.${row.specMode}`) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('product.columns.price')" min-width="150">
          <template #default="{ row }">
            {{ formatPriceRange(row.priceInCent, row.maxPriceInCent) }}
          </template>
        </el-table-column>
        <el-table-column prop="skuCount" :label="t('product.columns.skuCount')" min-width="100" />
        <el-table-column :label="t('product.columns.attrEnabled')" min-width="120">
          <template #default="{ row }">
            {{ row.attrEnabled ? t('common.yes') : t('common.no') }}
          </template>
        </el-table-column>
        <el-table-column :label="t('product.columns.status')" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.active ? 'success' : 'danger'">
              {{ row.active ? t('dict.productStatus.ACTIVE') : t('dict.productStatus.INACTIVE') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canOperateProduct" :label="t('common.actions')" min-width="250" fixed="right">
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
              <el-button v-if="canDeleteProduct" link type="danger" @click="handleDelete(row)">
                {{ t('common.delete') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? t('product.toolbar.add') : t('common.edit')"
      width="1180px"
      destroy-on-close
    >
      <div v-loading="dialogLoading">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
          <el-card shadow="never" class="dialog-section">
            <template #header>{{ t('product.sections.basic') }}</template>
            <div class="section-grid">
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
              <el-form-item :label="t('product.form.productType')" prop="productType">
                <el-select v-model="form.productType">
                  <el-option v-for="item in productTypeOptions" :key="item" :label="t(`dict.productType.${item}`)" :value="item" />
                </el-select>
              </el-form-item>
              <el-form-item :label="t('product.form.specMode')" prop="specMode">
                <el-radio-group v-model="form.specMode">
                  <el-radio-button v-for="item in specModeOptions" :key="item" :label="item">
                    {{ t(`dict.productSpecMode.${item}`) }}
                  </el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item :label="t('product.form.description')" class="section-grid__full">
                <el-input v-model="form.description" type="textarea" :rows="2" :placeholder="t('product.form.descriptionPlaceholder')" />
              </el-form-item>
            </div>

            <div class="switch-grid">
              <div class="switch-card">
                <div class="switch-card__label">{{ t('product.form.attrEnabled') }}</div>
                <el-switch v-model="form.attrEnabled" />
              </div>
              <div class="switch-card">
                <div class="switch-card__label">{{ t('product.form.materialEnabled') }}</div>
                <el-switch v-model="form.materialEnabled" />
                <div class="helper-text">{{ t('product.form.materialHint') }}</div>
              </div>
              <div class="switch-card">
                <div class="switch-card__label">{{ t('product.form.active') }}</div>
                <el-switch v-model="form.active" />
              </div>
            </div>
          </el-card>

          <el-card shadow="never" class="dialog-section">
            <template #header>{{ t('product.sections.specs') }}</template>
            <div v-if="form.specMode === 'MULTI'" class="group-editor">
              <div class="section-toolbar">
                <span class="section-toolbar__title">{{ t('product.specs.groups') }}</span>
                <el-button link type="primary" @click="addSpecGroup">{{ t('product.specs.addGroup') }}</el-button>
              </div>

              <div v-if="form.specs.length === 0" class="helper-text">{{ t('product.specs.empty') }}</div>

              <div v-for="(spec, specIndex) in form.specs" :key="`spec-${specIndex}`" class="group-card">
                <div class="group-card__head">
                  <el-input v-model="spec.name" :placeholder="t('product.specs.groupNamePlaceholder')" @input="rebuildSkuRows" />
                  <el-button link type="danger" @click="removeSpecGroup(specIndex)">{{ t('product.specs.removeGroup') }}</el-button>
                </div>
                <div class="group-values">
                  <div v-for="(value, valueIndex) in spec.values" :key="`spec-${specIndex}-value-${valueIndex}`" class="group-values__row">
                    <el-input v-model="value.name" :placeholder="t('product.specs.valuePlaceholder')" @input="rebuildSkuRows" />
                    <el-button link type="danger" @click="removeSpecValue(specIndex, valueIndex)">
                      {{ t('product.specs.removeValue') }}
                    </el-button>
                  </div>
                </div>
                <el-button link type="primary" @click="addSpecValue(specIndex)">{{ t('product.specs.addValue') }}</el-button>
              </div>
            </div>

            <div class="section-toolbar">
              <span class="section-toolbar__title">{{ t('product.specs.skuTable') }}</span>
              <el-button v-if="form.specMode === 'MULTI'" link type="primary" @click="rebuildSkuRows">
                {{ t('product.specs.rebuild') }}
              </el-button>
            </div>

            <el-table :data="form.skus" size="small" border>
              <el-table-column :label="t('product.sku.columns.specName')" min-width="180">
                <template #default="{ row }">
                  {{ row.specName || t('product.sku.defaultSpec') }}
                </template>
              </el-table-column>
              <el-table-column :label="t('product.sku.columns.price')" min-width="130">
                <template #default="{ row }">
                  <el-input-number v-model="row.priceInCent" :min="0" :step="100" />
                </template>
              </el-table-column>
              <el-table-column :label="t('product.sku.columns.linePrice')" min-width="130">
                <template #default="{ row }">
                  <el-input-number v-model="row.linePriceInCent" :min="0" :step="100" />
                </template>
              </el-table-column>
              <el-table-column :label="t('product.sku.columns.costPrice')" min-width="130">
                <template #default="{ row }">
                  <el-input-number v-model="row.costPriceInCent" :min="0" :step="100" />
                </template>
              </el-table-column>
              <el-table-column :label="t('product.sku.columns.boxFee')" min-width="130">
                <template #default="{ row }">
                  <el-input-number v-model="row.boxFeeInCent" :min="0" :step="100" />
                </template>
              </el-table-column>
              <el-table-column :label="t('product.sku.columns.stockQty')" min-width="120">
                <template #default="{ row }">
                  <el-input-number v-model="row.stockQty" :min="0" :step="1" />
                </template>
              </el-table-column>
              <el-table-column :label="t('product.sku.columns.weightUnitGram')" min-width="150">
                <template #default="{ row }">
                  <el-input-number v-model="row.weightUnitGram" :min="1" :step="1" :disabled="!form.weighedEnabled" />
                </template>
              </el-table-column>
              <el-table-column :label="t('product.sku.columns.barcode')" min-width="150">
                <template #default="{ row }">
                  <el-input v-model="row.barcode" />
                </template>
              </el-table-column>
              <el-table-column :label="t('product.sku.columns.skuCode')" min-width="150">
                <template #default="{ row }">
                  <el-input v-model="row.skuCode" />
                </template>
              </el-table-column>
              <el-table-column :label="t('product.sku.columns.autoReplenish')" min-width="120">
                <template #default="{ row }">
                  <el-switch v-model="row.autoReplenish" />
                </template>
              </el-table-column>
            </el-table>
          </el-card>

          <el-card shadow="never" class="dialog-section">
            <template #header>{{ t('product.sections.attrs') }}</template>
            <div v-if="!form.attrEnabled" class="helper-text">{{ t('product.attrs.disabledHint') }}</div>
            <template v-else>
              <div class="section-toolbar">
                <span class="section-toolbar__title">{{ t('product.attrs.groups') }}</span>
                <el-button link type="primary" @click="addAttrGroup">{{ t('product.attrs.addGroup') }}</el-button>
              </div>
              <div v-if="form.attrs.length === 0" class="helper-text">{{ t('product.attrs.empty') }}</div>
              <div v-for="(attr, attrIndex) in form.attrs" :key="`attr-${attrIndex}`" class="group-card">
                <div class="group-card__head">
                  <el-input v-model="attr.name" :placeholder="t('product.attrs.groupNamePlaceholder')" />
                  <el-button link type="danger" @click="removeAttrGroup(attrIndex)">{{ t('product.attrs.removeGroup') }}</el-button>
                </div>
                <div class="group-values">
                  <div v-for="(value, valueIndex) in attr.values" :key="`attr-${attrIndex}-value-${valueIndex}`" class="attr-row">
                    <el-input v-model="value.name" :placeholder="t('product.attrs.valuePlaceholder')" />
                    <el-radio :model-value="value.isDefault" :label="true" @change="markDefaultAttrValue(attrIndex, valueIndex)">
                      {{ t('product.attrs.defaultLabel') }}
                    </el-radio>
                    <el-button link type="danger" @click="removeAttrValue(attrIndex, valueIndex)">
                      {{ t('product.attrs.removeValue') }}
                    </el-button>
                  </div>
                </div>
                <el-button link type="primary" @click="addAttrValue(attrIndex)">{{ t('product.attrs.addValue') }}</el-button>
              </div>
            </template>
          </el-card>
        </el-form>
      </div>

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
  deleteProduct,
  fetchProductDetail,
  fetchProducts,
  updateProduct,
  updateProductStatus,
  type ProductAttr,
  type ProductAttrValue,
  type ProductPayload,
  type ProductSku,
  type ProductSpec,
  type ProductSpecValue,
  type ProductSpecMode,
  type ProductSummary,
  type ProductType,
} from '@/api/products';
import { fetchProductCategories, type ProductCategorySummary } from '@/api/product-categories';
import { fetchStores, type StoreSummary } from '@/api/stores';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';

interface ProductFilters {
  storeId?: number;
  categoryCode: string;
  productType?: ProductType;
  active?: boolean;
  keyword: string;
}

interface ProductFormModel {
  id?: number;
  storeId?: number;
  name: string;
  code: string;
  categoryCode: string;
  productType: ProductType;
  specMode: ProductSpecMode;
  description: string;
  attrEnabled: boolean;
  materialEnabled: boolean;
  weighedEnabled: boolean;
  active: boolean;
  specs: ProductSpec[];
  skus: ProductSku[];
  attrs: ProductAttr[];
}

const { t } = useI18n();
const authStore = useAuthStore();

const productTypeOptions: ProductType[] = ['NORMAL', 'WEIGHED', 'ADD_ON', 'SET_MEAL'];
const specModeOptions: ProductSpecMode[] = ['SINGLE', 'MULTI'];

const loading = ref(false);
const dialogLoading = ref(false);
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
  productType: 'NORMAL',
  specMode: 'SINGLE',
  description: '',
  attrEnabled: false,
  materialEnabled: false,
  weighedEnabled: false,
  active: true,
  specs: [],
  skus: [],
  attrs: [],
});

const canCreateProduct = computed(() => authStore.hasPermission('product:create'));
const canUpdateProduct = computed(() => authStore.hasPermission('product:update'));
const canUpdateProductStatus = computed(() => authStore.hasPermission('product:status'));
const canDeleteProduct = computed(() => authStore.hasPermission('product:delete'));
const canOperateProduct = computed(() => canUpdateProduct.value || canUpdateProductStatus.value || canDeleteProduct.value);
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
  rows.value
    .filter((item) => !filters.productType || item.productType === filters.productType)
    .map((item) => ({
      ...item,
      storeName: item.storeName || storeNameMap.value[item.storeId] || '',
    })),
);

const rules: FormRules<ProductFormModel> = {
  storeId: [{ required: true, message: t('product.form.storePlaceholder'), trigger: 'change' }],
  name: [{ required: true, message: t('product.form.namePlaceholder'), trigger: 'blur' }],
  code: [{ required: true, message: t('product.form.codePlaceholder'), trigger: 'blur' }],
  categoryCode: [{ required: true, message: t('product.form.categoryCodePlaceholder'), trigger: 'change' }],
  productType: [{ required: true, message: t('product.form.productTypePlaceholder'), trigger: 'change' }],
  specMode: [{ required: true, message: t('product.form.specModePlaceholder'), trigger: 'change' }],
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

watch(
  () => form.productType,
  (value) => {
    form.weighedEnabled = value === 'WEIGHED';
  },
);

watch(
  () => form.specMode,
  () => {
    rebuildSkuRows();
  },
);

watch(
  () => form.specs,
  () => {
    if (form.specMode === 'MULTI') {
      rebuildSkuRows();
    }
  },
  { deep: true },
);

function createEmptySpecValue(): ProductSpecValue {
  return { name: '', sortOrder: 1 };
}

function createEmptySpec(): ProductSpec {
  return { name: '', sortOrder: form.specs.length + 1, values: [createEmptySpecValue()] };
}

function createEmptyAttrValue(): ProductAttrValue {
  return { name: '', isDefault: false, sortOrder: 1 };
}

function createEmptyAttr(): ProductAttr {
  return { name: '', sortOrder: form.attrs.length + 1, values: [createEmptyAttrValue()] };
}

function createDefaultSku(): ProductSku {
  return {
    specKey: 'DEFAULT',
    specName: 'Default',
    skuCode: '',
    barcode: '',
    priceInCent: 0,
    linePriceInCent: 0,
    costPriceInCent: 0,
    boxFeeInCent: 0,
    stockQty: 0,
    autoReplenish: false,
    weightUnitGram: form.weighedEnabled ? 100 : undefined,
    sortOrder: 1,
    active: true,
  };
}

function normalizedSpecGroups(): ProductSpec[] {
  return form.specs
    .map((spec, specIndex) => ({
      ...spec,
      sortOrder: specIndex + 1,
      name: spec.name.trim(),
      values: spec.values
        .map((value, valueIndex) => ({
          ...value,
          sortOrder: valueIndex + 1,
          name: value.name.trim(),
        }))
        .filter((value) => value.name),
    }))
    .filter((spec) => spec.name && spec.values.length > 0);
}

function cartesianProduct<T>(groups: T[][]): T[][] {
  return groups.reduce<T[][]>(
    (acc, group) => acc.flatMap((item) => group.map((value) => [...item, value])),
    [[]],
  );
}

function rebuildSkuRows() {
  if (form.specMode === 'SINGLE') {
    const current = form.skus[0] ? { ...form.skus[0] } : createDefaultSku();
    form.skus = [
      {
        ...createDefaultSku(),
        ...current,
        specKey: 'DEFAULT',
        specName: 'Default',
        weightUnitGram: form.weighedEnabled ? current.weightUnitGram || 100 : undefined,
        sortOrder: 1,
      },
    ];
    return;
  }

  const groups = normalizedSpecGroups();
  if (groups.length === 0) {
    form.skus = [];
    return;
  }

  const existingMap = new Map(form.skus.map((item) => [item.specKey, item]));
  const combinations = cartesianProduct(groups.map((group) => group.values.map((value) => ({ groupName: group.name, value }))));

  form.skus = combinations.map((combination, index) => {
    const specKey = combination.map((item) => `${item.groupName}:${item.value.name}`).join('|');
    const specName = combination.map((item) => item.value.name).join(' / ');
    const previous = existingMap.get(specKey);
    return {
      ...createDefaultSku(),
      ...previous,
      specKey,
      specName,
      weightUnitGram: form.weighedEnabled ? previous?.weightUnitGram || 100 : undefined,
      sortOrder: index + 1,
    };
  });
}

function formatCurrency(valueInCent: number): string {
  return `$${(valueInCent / 100).toFixed(2)}`;
}

function formatPriceRange(minPriceInCent: number, maxPriceInCent: number): string {
  if (maxPriceInCent > minPriceInCent) {
    return `${formatCurrency(minPriceInCent)} ~ ${formatCurrency(maxPriceInCent)}`;
  }
  return formatCurrency(minPriceInCent);
}

function resetFilters() {
  filters.storeId = undefined;
  filters.categoryCode = '';
  filters.productType = undefined;
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
  form.productType = 'NORMAL';
  form.specMode = 'SINGLE';
  form.description = '';
  form.attrEnabled = false;
  form.materialEnabled = false;
  form.weighedEnabled = false;
  form.active = true;
  form.specs = [];
  form.skus = [createDefaultSku()];
  form.attrs = [];
  formRef.value?.clearValidate();
}

function addSpecGroup() {
  form.specs.push(createEmptySpec());
}

function removeSpecGroup(index: number) {
  form.specs.splice(index, 1);
  rebuildSkuRows();
}

function addSpecValue(specIndex: number) {
  form.specs[specIndex]?.values.push({
    ...createEmptySpecValue(),
    sortOrder: form.specs[specIndex].values.length + 1,
  });
}

function removeSpecValue(specIndex: number, valueIndex: number) {
  form.specs[specIndex]?.values.splice(valueIndex, 1);
  rebuildSkuRows();
}

function addAttrGroup() {
  form.attrs.push(createEmptyAttr());
}

function removeAttrGroup(index: number) {
  form.attrs.splice(index, 1);
}

function addAttrValue(attrIndex: number) {
  form.attrs[attrIndex]?.values.push({
    ...createEmptyAttrValue(),
    sortOrder: form.attrs[attrIndex].values.length + 1,
  });
}

function removeAttrValue(attrIndex: number, valueIndex: number) {
  form.attrs[attrIndex]?.values.splice(valueIndex, 1);
}

function markDefaultAttrValue(attrIndex: number, valueIndex: number) {
  const values = form.attrs[attrIndex]?.values || [];
  values.forEach((item, index) => {
    item.isDefault = index === valueIndex;
  });
}

function normalizedAttrs(): ProductAttr[] {
  return form.attrs
    .map((attr, attrIndex) => ({
      ...attr,
      sortOrder: attrIndex + 1,
      name: attr.name.trim(),
      values: attr.values
        .map((value, valueIndex) => ({
          ...value,
          sortOrder: valueIndex + 1,
          name: value.name.trim(),
        }))
        .filter((value) => value.name),
    }))
    .filter((attr) => attr.name && attr.values.length > 0);
}

function buildPayload(): ProductPayload {
  const specs = form.specMode === 'MULTI' ? normalizedSpecGroups() : [];
  const attrs = form.attrEnabled ? normalizedAttrs() : [];
  const skus = form.skus.map((item, index) => ({
    ...item,
    skuCode: item.skuCode.trim(),
    barcode: item.barcode.trim(),
    specKey: item.specKey,
    specName: item.specName,
    weightUnitGram: form.weighedEnabled ? item.weightUnitGram || 100 : undefined,
    sortOrder: index + 1,
  }));

  return {
    storeId: form.storeId!,
    name: form.name.trim(),
    code: form.code.trim(),
    categoryCode: form.categoryCode.trim(),
    productType: form.productType,
    specMode: form.specMode,
    description: form.description.trim() || undefined,
    attrEnabled: form.attrEnabled,
    materialEnabled: form.materialEnabled,
    weighedEnabled: form.weighedEnabled,
    active: form.active,
    specs,
    skus,
    attrs,
  };
}

function validateBeforeSubmit(payload: ProductPayload): boolean {
  if (payload.skus.length === 0) {
    ElMessage.error(t('product.validation.skuRequired'));
    return false;
  }
  if (payload.specMode === 'MULTI' && payload.specs.length === 0) {
    ElMessage.error(t('product.validation.specRequired'));
    return false;
  }
  if (payload.attrEnabled && payload.attrs.some((item) => item.values.filter((value) => value.isDefault).length > 1)) {
    ElMessage.error(t('product.validation.attrDefault'));
    return false;
  }
  return true;
}

function openCreateDialog() {
  if (!canCreateProduct.value) {
    return;
  }
  dialogMode.value = 'create';
  resetForm();
  dialogVisible.value = true;
}

async function openEditDialog(row: ProductSummary) {
  if (!canUpdateProduct.value) {
    return;
  }
  dialogMode.value = 'edit';
  dialogLoading.value = true;
  try {
    const detail = await fetchProductDetail(row.id);
    form.id = detail.id;
    form.storeId = detail.storeId;
    form.name = detail.name;
    form.code = detail.code;
    form.categoryCode = detail.categoryCode;
    form.productType = detail.productType;
    form.specMode = detail.specMode;
    form.description = detail.description;
    form.attrEnabled = detail.attrEnabled;
    form.materialEnabled = detail.materialEnabled;
    form.weighedEnabled = detail.weighedEnabled;
    form.active = detail.active;
    form.specs = detail.specs.map((item) => ({
      id: item.id,
      name: item.name,
      sortOrder: item.sortOrder,
      values: item.values.map((value) => ({
        id: value.id,
        name: value.name,
        sortOrder: value.sortOrder,
      })),
    }));
    form.skus = detail.skus.map((item) => ({ ...item }));
    form.attrs = detail.attrs.map((item) => ({
      id: item.id,
      name: item.name,
      sortOrder: item.sortOrder,
      values: item.values.map((value) => ({
        id: value.id,
        name: value.name,
        isDefault: value.isDefault,
        sortOrder: value.sortOrder,
      })),
    }));
    dialogVisible.value = true;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  } finally {
    dialogLoading.value = false;
  }
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

  const payload = buildPayload();
  if (!validateBeforeSubmit(payload)) {
    return;
  }

  submitting.value = true;
  try {
    if (dialogMode.value === 'create') {
      await createProduct(payload);
    } else if (form.id) {
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

async function handleDelete(row: ProductSummary) {
  if (!canDeleteProduct.value) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      t('product.delete.confirm', { name: row.name }),
      t('product.delete.title'),
      {
        type: 'warning',
        confirmButtonText: t('common.yes'),
        cancelButtonText: t('common.cancel'),
      },
    );
    await deleteProduct(row.id);
    ElMessage.success(t('product.delete.success'));
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

<style scoped>
.dialog-section + .dialog-section {
  margin-top: 16px;
}

.section-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.section-grid__full {
  grid-column: 1 / -1;
}

.switch-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.switch-card {
  padding: 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  background: var(--el-fill-color-extra-light);
}

.switch-card__label {
  margin-bottom: 8px;
  font-weight: 600;
}

.section-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-toolbar__title {
  font-weight: 600;
}

.group-editor,
.group-card {
  margin-bottom: 16px;
}

.group-card {
  padding: 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
}

.group-card__head {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}

.group-values,
.group-values__row,
.attr-row {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.helper-text {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

@media (max-width: 960px) {
  .section-grid,
  .switch-grid {
    grid-template-columns: 1fr;
  }
}
</style>
