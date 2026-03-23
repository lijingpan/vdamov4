<template>
  <PageShell :title="t('page.discounts.title')" :description="t('page.discounts.description')">
    <template #actions>
      <el-button :icon="RefreshRight" @click="loadData">{{ t('common.refresh') }}</el-button>
      <el-button v-if="canCreateDiscount" type="primary" :icon="Plus" @click="openCreateDialog">
        {{ t('discount.toolbar.add') }}
      </el-button>
    </template>

    <template #meta>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-card__label">{{ t('discount.summary.total') }}</div>
          <div class="stat-card__value">{{ rows.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('discount.summary.enabled') }}</div>
          <div class="stat-card__value">{{ enabledCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('discount.summary.stackable') }}</div>
          <div class="stat-card__value">{{ stackableCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('discount.summary.fullReduction') }}</div>
          <div class="stat-card__value">{{ fullReductionCount }}</div>
        </div>
      </div>
    </template>

    <el-card shadow="never" class="page-card filter-card">
      <el-form :inline="true">
        <el-form-item :label="t('discount.filter.store')">
          <el-select v-model="filters.storeId" clearable :placeholder="t('discount.filter.storePlaceholder')">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('discount.filter.discountType')">
          <el-select
            v-model="filters.discountType"
            clearable
            :placeholder="t('discount.filter.discountTypePlaceholder')"
          >
            <el-option
              v-for="item in discountTypeOptions"
              :key="item"
              :label="t(`dict.discountType.${item}`)"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('discount.filter.enabled')">
          <el-select v-model="filters.enabled" clearable :placeholder="t('discount.filter.enabledPlaceholder')">
            <el-option :label="t('dict.enableStatus.ENABLED')" :value="true" />
            <el-option :label="t('dict.enableStatus.DISABLED')" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('discount.filter.keyword')">
          <el-input
            v-model="filters.keyword"
            clearable
            :placeholder="t('discount.filter.keywordPlaceholder')"
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
        <div class="action-bar__hint">{{ t('page.discounts.description') }}</div>
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
        <el-table-column prop="storeName" :label="t('discount.columns.storeName')" min-width="180" />
        <el-table-column prop="name" :label="t('discount.columns.name')" min-width="180" />
        <el-table-column prop="code" :label="t('discount.columns.code')" min-width="160" />
        <el-table-column :label="t('discount.columns.discountType')" min-width="160">
          <template #default="{ row }">
            {{ t(`dict.discountType.${row.discountType}`) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('discount.columns.amount')" min-width="160">
          <template #default="{ row }">
            {{ formatAmount(row.amountType, row.amountValue) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('discount.columns.threshold')" min-width="160">
          <template #default="{ row }">
            {{ formatCurrency(row.thresholdAmountInCent) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('discount.columns.stackable')" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.stackable ? 'warning' : 'info'">
              {{ row.stackable ? t('common.yes') : t('common.no') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('discount.columns.enabled')" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? t('dict.enableStatus.ENABLED') : t('dict.enableStatus.DISABLED') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" :label="t('discount.columns.startTime')" min-width="180">
          <template #default="{ row }">
            {{ row.startTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" :label="t('discount.columns.endTime')" min-width="180">
          <template #default="{ row }">
            {{ row.endTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" :label="t('discount.columns.remark')" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="table-muted">{{ row.remark || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="canOperateDiscount" :label="t('common.actions')" min-width="260" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="canUpdateDiscount" link type="primary" @click="openEditDialog(row)">
                {{ t('common.edit') }}
              </el-button>
              <el-button v-if="canEnableDiscount && !row.enabled" link type="success" @click="toggleEnabled(row, true)">
                {{ t('discount.actions.enable') }}
              </el-button>
              <el-button v-if="canEnableDiscount && row.enabled" link type="danger" @click="toggleEnabled(row, false)">
                {{ t('discount.actions.disable') }}
              </el-button>
              <el-button v-if="canDeleteDiscount" link type="danger" @click="handleDelete(row)">
                {{ t('common.delete') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? t('discount.toolbar.add') : t('common.edit')"
      width="680px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item :label="t('discount.form.store')" prop="storeId">
              <el-select v-model="form.storeId" :placeholder="t('discount.form.storePlaceholder')">
                <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('discount.form.code')" prop="code">
              <el-input v-model="form.code" :placeholder="t('discount.form.codePlaceholder')" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item :label="t('discount.form.name')" prop="name">
              <el-input v-model="form.name" :placeholder="t('discount.form.namePlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('discount.form.discountType')" prop="discountType">
              <el-select v-model="form.discountType" :placeholder="t('discount.form.discountTypePlaceholder')">
                <el-option
                  v-for="item in discountTypeOptions"
                  :key="item"
                  :label="t(`dict.discountType.${item}`)"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item :label="t('discount.form.amountType')" prop="amountType">
              <el-select v-model="form.amountType" :placeholder="t('discount.form.amountTypePlaceholder')">
                <el-option
                  v-for="item in amountTypeOptions"
                  :key="item"
                  :label="t(`dict.amountType.${item}`)"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('discount.form.amountValue')" prop="amountValue">
              <el-input-number
                v-model="form.amountValue"
                :min="0"
                :max="form.amountType === 'PERCENT' ? 100 : 99999999"
                :precision="form.amountType === 'PERCENT' ? 2 : 0"
                :step="form.amountType === 'PERCENT' ? 0.1 : 100"
                controls-position="right"
                style="width: 100%"
              />
              <div class="helper-text">
                {{
                  form.amountType === 'PERCENT'
                    ? t('discount.form.amountValuePercentHint')
                    : t('discount.form.amountValueFixedHint')
                }}
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item :label="t('discount.form.thresholdAmountInCent')" prop="thresholdAmountInCent">
              <el-input-number
                v-model="form.thresholdAmountInCent"
                :min="0"
                :step="100"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('discount.form.stackable')" prop="stackable">
              <el-switch v-model="form.stackable" />
            </el-form-item>
            <el-form-item :label="t('discount.form.enabled')" prop="enabled">
              <el-switch v-model="form.enabled" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item :label="t('discount.form.startTime')" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                :placeholder="t('discount.form.startTimePlaceholder')"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('discount.form.endTime')" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                :placeholder="t('discount.form.endTimePlaceholder')"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="t('discount.form.remark')" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            :placeholder="t('discount.form.remarkPlaceholder')"
          />
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
  createDiscount,
  deleteDiscount,
  fetchDiscounts,
  updateDiscount,
  updateDiscountEnabled,
  type DiscountPayload,
  type DiscountSummary,
} from '@/api/discounts';
import { fetchStores, type StoreSummary } from '@/api/stores';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';
import { formatMoneyFromCent } from '@/utils/currency';

interface DiscountFilters {
  storeId?: number;
  enabled?: boolean;
  discountType: string;
  keyword: string;
}

interface DiscountFormModel {
  id?: number;
  storeId?: number;
  name: string;
  code: string;
  discountType: string;
  amountType: string;
  amountValue: number;
  thresholdAmountInCent: number;
  stackable: boolean;
  enabled: boolean;
  startTime: string;
  endTime: string;
  remark: string;
}

const { locale, t } = useI18n();
const authStore = useAuthStore();

const loading = ref(false);
const submitting = ref(false);
const errorMessage = ref('');
const rows = ref<DiscountSummary[]>([]);
const storeOptions = ref<StoreSummary[]>([]);
const formRef = ref<FormInstance>();
const dialogVisible = ref(false);
const dialogMode = ref<'create' | 'edit'>('create');

const filters = reactive<DiscountFilters>({
  discountType: '',
  keyword: '',
});
const form = reactive<DiscountFormModel>({
  name: '',
  code: '',
  discountType: 'ORDER_DISCOUNT',
  amountType: 'FIXED',
  amountValue: 0,
  thresholdAmountInCent: 0,
  stackable: false,
  enabled: true,
  startTime: '',
  endTime: '',
  remark: '',
});

const discountTypeOptions = ['MEMBER_PRICE', 'ORDER_DISCOUNT', 'FULL_REDUCTION', 'COUPON'];
const amountTypeOptions = ['FIXED', 'PERCENT'];

const canCreateDiscount = computed(() => authStore.hasPermission('discount:create'));
const canUpdateDiscount = computed(() => authStore.hasPermission('discount:update'));
const canEnableDiscount = computed(() => authStore.hasPermission('discount:enable'));
const canDeleteDiscount = computed(() => authStore.hasPermission('discount:delete'));
const canOperateDiscount = computed(() => canUpdateDiscount.value || canEnableDiscount.value || canDeleteDiscount.value);
const enabledCount = computed(() => rows.value.filter((item) => item.enabled).length);
const stackableCount = computed(() => rows.value.filter((item) => item.stackable).length);
const fullReductionCount = computed(() => rows.value.filter((item) => item.discountType === 'FULL_REDUCTION').length);
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

const rules: FormRules<DiscountFormModel> = {
  storeId: [{ required: true, message: t('discount.form.storePlaceholder'), trigger: 'change' }],
  name: [{ required: true, message: t('discount.form.namePlaceholder'), trigger: 'blur' }],
  code: [{ required: true, message: t('discount.form.codePlaceholder'), trigger: 'blur' }],
  discountType: [{ required: true, message: t('discount.form.discountTypePlaceholder'), trigger: 'change' }],
  amountType: [{ required: true, message: t('discount.form.amountTypePlaceholder'), trigger: 'change' }],
  amountValue: [{ required: true, message: t('discount.form.amountValuePlaceholder'), trigger: 'change' }],
  thresholdAmountInCent: [{ required: true, message: t('discount.form.thresholdAmountInCentPlaceholder'), trigger: 'change' }],
};

function formatCurrency(valueInCent: number): string {
  return formatMoneyFromCent(valueInCent, locale.value);
}

function formatAmount(amountType: string, amountValue: number): string {
  if (amountType === 'PERCENT') {
    return `${amountValue}%`;
  }
  return formatCurrency(amountValue);
}

function resetFilters() {
  filters.storeId = undefined;
  filters.enabled = undefined;
  filters.discountType = '';
  filters.keyword = '';
  loadRows();
}

function resetForm() {
  form.id = undefined;
  form.storeId = storeOptions.value.length === 1 ? storeOptions.value[0].id : undefined;
  form.name = '';
  form.code = '';
  form.discountType = 'ORDER_DISCOUNT';
  form.amountType = 'FIXED';
  form.amountValue = 0;
  form.thresholdAmountInCent = 0;
  form.stackable = false;
  form.enabled = true;
  form.startTime = '';
  form.endTime = '';
  form.remark = '';
  formRef.value?.clearValidate();
}

function openCreateDialog() {
  if (!canCreateDiscount.value) {
    return;
  }
  dialogMode.value = 'create';
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(row: DiscountSummary) {
  if (!canUpdateDiscount.value) {
    return;
  }
  dialogMode.value = 'edit';
  form.id = row.id;
  form.storeId = row.storeId;
  form.name = row.name;
  form.code = row.code;
  form.discountType = row.discountType;
  form.amountType = row.amountType || 'FIXED';
  form.amountValue = row.amountValue;
  form.thresholdAmountInCent = row.thresholdAmountInCent;
  form.stackable = row.stackable;
  form.enabled = row.enabled;
  form.startTime = row.startTime || '';
  form.endTime = row.endTime || '';
  form.remark = row.remark || '';
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
    rows.value = await fetchDiscounts({
      storeId: filters.storeId,
      enabled: filters.enabled,
      discountType: filters.discountType || undefined,
      keyword: filters.keyword || undefined,
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

  if (form.startTime && form.endTime && form.startTime > form.endTime) {
    ElMessage.warning(t('discount.form.timeRangeInvalid'));
    return;
  }

  const payload: DiscountPayload = {
    storeId: form.storeId,
    name: form.name.trim(),
    code: form.code.trim().toUpperCase(),
    discountType: form.discountType,
    amountType: form.amountType,
    amountValue: form.amountValue,
    thresholdAmountInCent: form.thresholdAmountInCent,
    stackable: form.stackable,
    enabled: form.enabled,
    startTime: form.startTime || undefined,
    endTime: form.endTime || undefined,
    remark: form.remark.trim() || undefined,
  };

  submitting.value = true;
  try {
    if (dialogMode.value === 'create') {
      if (!canCreateDiscount.value) {
        return;
      }
      await createDiscount(payload);
    } else if (form.id) {
      if (!canUpdateDiscount.value) {
        return;
      }
      await updateDiscount(form.id, payload);
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

async function toggleEnabled(row: DiscountSummary, enabled: boolean) {
  if (!canEnableDiscount.value) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      `${row.name} - ${enabled ? t('discount.actions.enable') : t('discount.actions.disable')}`,
      t('common.actions'),
      { type: 'warning' },
    );
    await updateDiscountEnabled(row.id, enabled);
    ElMessage.success(t('common.save'));
    await loadRows();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  }
}

async function handleDelete(row: DiscountSummary) {
  if (!canDeleteDiscount.value) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      t('discount.delete.confirm', { name: row.name }),
      t('discount.delete.title'),
      {
        type: 'warning',
        confirmButtonText: t('common.yes'),
        cancelButtonText: t('common.cancel'),
      },
    );
    await deleteDiscount(row.id);
    ElMessage.success(t('discount.delete.success'));
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
