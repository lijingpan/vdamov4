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
        <el-table-column prop="storeName" :label="t('product.columns.storeName')" min-width="160" />
        <el-table-column prop="name" :label="t('product.columns.name')" min-width="180" />
        <el-table-column prop="code" :label="t('product.columns.code')" min-width="150" />
        <el-table-column prop="categoryCode" :label="t('product.columns.categoryCode')" min-width="140" />
        <el-table-column :label="t('product.columns.price')" min-width="150">
          <template #default="{ row }">
            {{ formatPriceRange(row.priceInCent, row.maxPriceInCent) }}
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
      width="980px"
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
              <el-form-item :label="t('product.form.basePriceInCent')" prop="basePriceInCent">
                <el-input-number v-model="form.basePriceInCent" :min="0" :step="100" controls-position="right" />
                <div class="helper-text">{{ t('product.form.basePriceHint') }}</div>
              </el-form-item>
              <el-form-item :label="t('product.form.active')">
                <el-switch v-model="form.active" />
              </el-form-item>
              <el-form-item :label="t('product.form.description')" class="section-grid__full">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="2"
                  :placeholder="t('product.form.descriptionPlaceholder')"
                />
              </el-form-item>
            </div>
          </el-card>

          <el-card shadow="never" class="dialog-section">
            <template #header>{{ t('product.sections.rules') }}</template>
            <div class="section-toolbar">
              <span class="section-toolbar__title">{{ t('product.rules.groups') }}</span>
              <el-button link type="primary" @click="addRuleGroup">{{ t('product.rules.addGroup') }}</el-button>
            </div>
            <div v-if="form.rules.length === 0" class="helper-text">{{ t('product.rules.empty') }}</div>
            <div v-for="(rule, ruleIndex) in form.rules" :key="`rule-${ruleIndex}`" class="group-card">
              <div class="group-card__head">
                <el-input v-model="rule.name" :placeholder="t('product.rules.groupNamePlaceholder')" />
                <el-button link type="danger" @click="removeRuleGroup(ruleIndex)">
                  {{ t('product.rules.removeGroup') }}
                </el-button>
              </div>
              <div class="group-values">
                <div
                  v-for="(value, valueIndex) in rule.values"
                  :key="`rule-${ruleIndex}-value-${valueIndex}`"
                  class="group-values__row"
                >
                  <el-input v-model="value.name" :placeholder="t('product.rules.valuePlaceholder')" />
                  <el-button link type="danger" @click="removeRuleValue(ruleIndex, valueIndex)">
                    {{ t('product.rules.removeValue') }}
                  </el-button>
                </div>
              </div>
              <el-button link type="primary" @click="addRuleValue(ruleIndex)">
                {{ t('product.rules.addValue') }}
              </el-button>
            </div>
          </el-card>

          <el-card shadow="never" class="dialog-section">
            <template #header>{{ t('product.sections.attrs') }}</template>
            <div class="section-toolbar">
              <span class="section-toolbar__title">{{ t('product.attrs.groups') }}</span>
              <el-button link type="primary" @click="addAttrGroup">{{ t('product.attrs.addGroup') }}</el-button>
            </div>
            <div v-if="form.attrs.length === 0" class="helper-text">{{ t('product.attrs.empty') }}</div>
            <div v-for="(attr, attrIndex) in form.attrs" :key="`attr-${attrIndex}`" class="group-card">
              <div class="group-card__head">
                <el-input v-model="attr.name" :placeholder="t('product.attrs.groupNamePlaceholder')" />
                <el-button link type="danger" @click="removeAttrGroup(attrIndex)">
                  {{ t('product.attrs.removeGroup') }}
                </el-button>
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
  type ProductSummary,
  type ProductType,
} from '@/api/products';
import { fetchProductCategories, type ProductCategorySummary } from '@/api/product-categories';
import { fetchStores, type StoreSummary } from '@/api/stores';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';
import { formatMoneyFromCent } from '@/utils/currency';

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
  basePriceInCent: number;
  description: string;
  active: boolean;
  productType: ProductType;
  materialEnabled: boolean;
  weighedEnabled: boolean;
  rules: ProductSpec[];
  attrs: ProductAttr[];
}

const { locale, t } = useI18n();
const authStore = useAuthStore();

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
  basePriceInCent: 0,
  description: '',
  active: true,
  productType: 'NORMAL',
  materialEnabled: false,
  weighedEnabled: false,
  rules: [],
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
  basePriceInCent: [{ required: true, message: t('product.form.basePricePlaceholder'), trigger: 'change' }],
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

function createEmptyRuleValue(): ProductSpecValue {
  return { name: '', sortOrder: 1 };
}

function createEmptyRule(): ProductSpec {
  return { name: '', sortOrder: form.rules.length + 1, values: [createEmptyRuleValue()] };
}

function createEmptyAttrValue(): ProductAttrValue {
  return { name: '', isDefault: false, sortOrder: 1 };
}

function createEmptyAttr(): ProductAttr {
  return { name: '', sortOrder: form.attrs.length + 1, values: [createEmptyAttrValue()] };
}

function formatCurrency(valueInCent: number): string {
  return formatMoneyFromCent(valueInCent, locale.value);
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
  form.basePriceInCent = 0;
  form.description = '';
  form.active = true;
  form.productType = 'NORMAL';
  form.materialEnabled = false;
  form.weighedEnabled = false;
  form.rules = [];
  form.attrs = [];
  formRef.value?.clearValidate();
}

function addRuleGroup() {
  form.rules.push(createEmptyRule());
}

function removeRuleGroup(index: number) {
  form.rules.splice(index, 1);
}

function addRuleValue(ruleIndex: number) {
  form.rules[ruleIndex]?.values.push({
    ...createEmptyRuleValue(),
    sortOrder: form.rules[ruleIndex].values.length + 1,
  });
}

function removeRuleValue(ruleIndex: number, valueIndex: number) {
  form.rules[ruleIndex]?.values.splice(valueIndex, 1);
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

function normalizedRuleGroups(): ProductSpec[] {
  return form.rules
    .map((rule, ruleIndex) => ({
      ...rule,
      sortOrder: ruleIndex + 1,
      name: rule.name.trim(),
      values: rule.values
        .map((value, valueIndex) => ({
          ...value,
          sortOrder: valueIndex + 1,
          name: value.name.trim(),
        }))
        .filter((value) => value.name),
    }))
    .filter((rule) => rule.name && rule.values.length > 0);
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

function cartesianProduct<T>(groups: T[][]): T[][] {
  return groups.reduce<T[][]>((acc, group) => acc.flatMap((left) => group.map((right) => [...left, right])), [[]]);
}

function buildSkus(basePriceInCent: number, rulesValue: ProductSpec[], weighedEnabled: boolean): ProductSku[] {
  if (rulesValue.length === 0) {
    return [
      {
        specKey: 'DEFAULT',
        specName: 'DEFAULT',
        skuCode: '',
        barcode: '',
        priceInCent: basePriceInCent,
        linePriceInCent: basePriceInCent,
        costPriceInCent: 0,
        boxFeeInCent: 0,
        stockQty: 0,
        autoReplenish: false,
        weightUnitGram: weighedEnabled ? 100 : undefined,
        sortOrder: 1,
        active: true,
      },
    ];
  }

  const combinations = cartesianProduct(
    rulesValue.map((rule) => rule.values.map((value) => ({ ruleName: rule.name, valueName: value.name }))),
  );

  return combinations.map((combination, index) => {
    const specKey = combination.map((item) => `${item.ruleName}:${item.valueName}`).join('|');
    const specName = combination.map((item) => item.valueName).join(' / ');
    return {
      specKey,
      specName,
      skuCode: '',
      barcode: '',
      priceInCent: basePriceInCent,
      linePriceInCent: basePriceInCent,
      costPriceInCent: 0,
      boxFeeInCent: 0,
      stockQty: 0,
      autoReplenish: false,
      weightUnitGram: weighedEnabled ? 100 : undefined,
      sortOrder: index + 1,
      active: true,
    };
  });
}

function validateRuleInput(): boolean {
  const ruleNames = new Set<string>();
  for (const rule of form.rules) {
    const name = rule.name.trim();
    const values = rule.values.map((item) => item.name.trim()).filter(Boolean);
    const touched = Boolean(name) || values.length > 0;

    if (!touched) {
      continue;
    }
    if (!name || values.length === 0) {
      ElMessage.error(t('product.validation.ruleGroupIncomplete'));
      return false;
    }

    const upperName = name.toUpperCase();
    if (ruleNames.has(upperName)) {
      ElMessage.error(t('product.validation.ruleGroupDuplicate'));
      return false;
    }
    ruleNames.add(upperName);

    const valueNames = new Set<string>();
    for (const value of values) {
      const upperValue = value.toUpperCase();
      if (valueNames.has(upperValue)) {
        ElMessage.error(t('product.validation.ruleValueDuplicate'));
        return false;
      }
      valueNames.add(upperValue);
    }
  }
  return true;
}

function buildPayload(): ProductPayload {
  const rulesValue = normalizedRuleGroups();
  const attrs = normalizedAttrs();
  const specMode = rulesValue.length > 0 ? 'MULTI' : 'SINGLE';
  return {
    storeId: form.storeId!,
    name: form.name.trim(),
    code: form.code.trim(),
    categoryCode: form.categoryCode.trim(),
    productType: form.productType,
    specMode,
    description: form.description.trim() || undefined,
    attrEnabled: attrs.length > 0,
    materialEnabled: form.materialEnabled,
    weighedEnabled: form.weighedEnabled,
    active: form.active,
    specs: specMode === 'MULTI' ? rulesValue : [],
    skus: buildSkus(form.basePriceInCent, rulesValue, form.weighedEnabled),
    attrs,
  };
}

function openCreateDialog() {
  if (!canCreateProduct.value) {
    return;
  }
  dialogMode.value = 'create';
  resetForm();
  dialogVisible.value = true;
}

function resolveBasePrice(skus: ProductSku[]): number {
  if (!skus.length) {
    return 0;
  }
  return skus
    .map((item) => item.priceInCent)
    .filter((item) => Number.isFinite(item))
    .reduce((acc, current) => Math.min(acc, current), skus[0].priceInCent);
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
    form.basePriceInCent = resolveBasePrice(detail.skus);
    form.description = detail.description;
    form.active = detail.active;
    form.productType = detail.productType;
    form.materialEnabled = detail.materialEnabled;
    form.weighedEnabled = detail.weighedEnabled;
    form.rules = detail.specs.map((item) => ({
      id: item.id,
      name: item.name,
      sortOrder: item.sortOrder,
      values: item.values.map((value) => ({
        id: value.id,
        name: value.name,
        sortOrder: value.sortOrder,
      })),
    }));
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
  if (!validateRuleInput()) {
    return;
  }

  const payload = buildPayload();
  if (payload.attrs.some((item) => item.values.filter((value) => value.isDefault).length > 1)) {
    ElMessage.error(t('product.validation.attrDefault'));
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

.section-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-toolbar__title {
  font-weight: 600;
}

.group-card {
  margin-bottom: 16px;
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
  .section-grid {
    grid-template-columns: 1fr;
  }
}
</style>
