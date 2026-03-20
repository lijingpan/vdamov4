<template>
  <PageShell :title="t('page.stores.title')" :description="t('page.stores.description')">
    <template #actions>
      <el-button :icon="RefreshRight" @click="loadStores">{{ t('common.refresh') }}</el-button>
      <el-button
        v-if="canCreateStore"
        type="primary"
        :icon="Plus"
        @click="openCreateDialog"
      >
        {{ t('store.toolbar.add') }}
      </el-button>
    </template>

    <template #meta>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-card__label">{{ t('store.summary.total') }}</div>
          <div class="stat-card__value">{{ rows.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('store.summary.open') }}</div>
          <div class="stat-card__value">{{ summary.open }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('store.summary.rest') }}</div>
          <div class="stat-card__value">{{ summary.rest }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('store.summary.disabled') }}</div>
          <div class="stat-card__value">{{ summary.disabled }}</div>
        </div>
      </div>
    </template>

    <el-card shadow="never" class="page-card filter-card">
      <el-form :inline="true">
        <el-form-item :label="t('store.filter.keyword')">
          <el-input
            v-model="filters.keyword"
            :placeholder="t('store.filter.keywordPlaceholder')"
            clearable
            @keyup.enter="loadStores"
          />
        </el-form-item>
        <el-form-item :label="t('store.filter.status')">
          <el-select v-model="filters.status" clearable :placeholder="t('store.filter.statusPlaceholder')">
            <el-option
              v-for="item in storeStatuses"
              :key="item"
              :label="t(`dict.storeStatus.${item}`)"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStores">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page-card data-card">
      <div class="action-bar">
        <div class="action-bar__hint">{{ t('page.stores.description') }}</div>
      </div>

      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="false"
        style="margin: 16px 0"
      />

      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column prop="id" :label="t('store.columns.id')" min-width="120" />
        <el-table-column prop="name" :label="t('store.columns.name')" min-width="220" />
        <el-table-column prop="countryCode" :label="t('store.columns.countryCode')" min-width="130" />
        <el-table-column :label="t('store.columns.businessTypes')" min-width="220">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="item in row.businessTypes" :key="item" type="info" effect="plain">
                {{ item }}
              </el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column :label="t('store.columns.status')" min-width="150">
          <template #default="{ row }">
            <el-tag :type="statusType(row.businessStatus)">
              {{ t(`dict.storeStatus.${row.businessStatus}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" min-width="220" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="openEditDialog(row)">{{ t('common.edit') }}</el-button>
              <el-button
                v-if="row.businessStatus !== 'OPEN'"
                link
                type="success"
                @click="changeStatus(row, 'OPEN')"
              >
                {{ t('store.actions.open') }}
              </el-button>
              <el-button
                v-if="row.businessStatus === 'OPEN'"
                link
                type="warning"
                @click="changeStatus(row, 'REST')"
              >
                {{ t('store.actions.rest') }}
              </el-button>
              <el-button
                v-if="row.businessStatus !== 'DISABLED'"
                link
                type="danger"
                @click="changeStatus(row, 'DISABLED')"
              >
                {{ t('store.actions.disable') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? t('store.toolbar.add') : t('common.edit')"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <el-form-item :label="t('store.form.name')" prop="name">
          <el-input v-model="form.name" :placeholder="t('store.form.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('store.form.countryCode')" prop="countryCode">
          <el-input v-model="form.countryCode" maxlength="8" :placeholder="t('store.form.countryCodePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('store.form.businessStatus')" prop="businessStatus">
          <el-select v-model="form.businessStatus">
            <el-option
              v-for="item in storeStatuses"
              :key="item"
              :label="t(`dict.storeStatus.${item}`)"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('store.form.businessTypes')" prop="businessTypes">
          <el-checkbox-group v-model="form.businessTypes">
            <el-checkbox v-for="item in businessTypeOptions" :key="item" :label="item">
              {{ item }}
            </el-checkbox>
          </el-checkbox-group>
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
import { createStore, fetchStores, updateStore, updateStoreStatus, type StorePayload, type StoreSummary } from '@/api/stores';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';

interface StoreFilters {
  keyword: string;
  status: string;
}

interface StoreFormModel {
  id?: number;
  name: string;
  countryCode: string;
  businessStatus: string;
  businessTypes: string[];
}

const { t } = useI18n();
const authStore = useAuthStore();

const loading = ref(false);
const submitting = ref(false);
const errorMessage = ref('');
const rows = ref<StoreSummary[]>([]);
const formRef = ref<FormInstance>();
const dialogVisible = ref(false);
const dialogMode = ref<'create' | 'edit'>('create');
const filters = reactive<StoreFilters>({
  keyword: '',
  status: '',
});
const form = reactive<StoreFormModel>({
  name: '',
  countryCode: '',
  businessStatus: 'OPEN',
  businessTypes: ['DINE_IN'],
});

const storeStatuses = ['OPEN', 'REST', 'DISABLED'];
const businessTypeOptions = ['DINE_IN', 'TAKEAWAY', 'DELIVERY'];

const canCreateStore = computed(() => authStore.user?.roleCodes.includes('SUPER_ADMIN'));
const summary = computed(() => ({
  open: rows.value.filter((item) => item.businessStatus === 'OPEN').length,
  rest: rows.value.filter((item) => item.businessStatus === 'REST').length,
  disabled: rows.value.filter((item) => item.businessStatus === 'DISABLED').length,
}));

const rules: FormRules<StoreFormModel> = {
  name: [{ required: true, message: t('store.form.namePlaceholder'), trigger: 'blur' }],
  countryCode: [{ required: true, message: t('store.form.countryCodePlaceholder'), trigger: 'blur' }],
  businessStatus: [{ required: true, message: t('store.form.businessStatus'), trigger: 'change' }],
  businessTypes: [{ type: 'array', required: true, min: 1, message: t('store.form.businessTypes'), trigger: 'change' }],
};

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
  filters.keyword = '';
  filters.status = '';
  loadStores();
}

function resetForm() {
  form.id = undefined;
  form.name = '';
  form.countryCode = '';
  form.businessStatus = 'OPEN';
  form.businessTypes = ['DINE_IN'];
  formRef.value?.clearValidate();
}

function openCreateDialog() {
  dialogMode.value = 'create';
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(row: StoreSummary) {
  dialogMode.value = 'edit';
  form.id = row.id;
  form.name = row.name;
  form.countryCode = row.countryCode;
  form.businessStatus = row.businessStatus;
  form.businessTypes = [...row.businessTypes];
  dialogVisible.value = true;
}

async function loadStores() {
  loading.value = true;
  errorMessage.value = '';
  try {
    rows.value = await fetchStores({
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
    });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  submitting.value = true;
  try {
    const payload: StorePayload = {
      name: form.name.trim(),
      countryCode: form.countryCode.trim().toUpperCase(),
      businessStatus: form.businessStatus,
      businessTypes: [...form.businessTypes],
    };
    if (dialogMode.value === 'create') {
      await createStore(payload);
    } else if (form.id) {
      await updateStore(form.id, payload);
    }
    dialogVisible.value = false;
    ElMessage.success(t('common.save'));
    await loadStores();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  } finally {
    submitting.value = false;
  }
}

async function changeStatus(row: StoreSummary, nextStatus: string) {
  try {
    await ElMessageBox.confirm(
      `${row.name} · ${t(`dict.storeStatus.${nextStatus}`)}`,
      t('common.actions'),
      {
        type: 'warning',
      },
    );
    await updateStoreStatus(row.id, nextStatus);
    ElMessage.success(t('common.save'));
    await loadStores();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  }
}

onMounted(() => {
  loadStores();
});
</script>
