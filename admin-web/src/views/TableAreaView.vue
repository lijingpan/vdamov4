<template>
  <PageShell :title="t('page.tableAreas.title')" :description="t('page.tableAreas.description')">
    <template #actions>
      <el-button :icon="RefreshRight" @click="loadRows">{{ t('common.refresh') }}</el-button>
      <el-button v-if="canCreateTableArea" type="primary" :icon="Plus" @click="openCreateDialog">
        {{ t('tableArea.toolbar.add') }}
      </el-button>
    </template>

    <template #meta>
      <div class="stats-grid stats-grid--three">
        <div class="stat-card">
          <div class="stat-card__label">{{ t('tableArea.summary.total') }}</div>
          <div class="stat-card__value">{{ rows.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('tableArea.summary.enabled') }}</div>
          <div class="stat-card__value">{{ enabledCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('tableArea.summary.tableCount') }}</div>
          <div class="stat-card__value">{{ totalTableCount }}</div>
        </div>
      </div>
    </template>

    <el-card shadow="never" class="page-card filter-card">
      <el-form :inline="true">
        <el-form-item :label="t('tableArea.filter.store')">
          <el-select v-model="filters.storeId" clearable :placeholder="t('tableArea.filter.storePlaceholder')">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('tableArea.filter.enabled')">
          <el-select v-model="filters.enabled" clearable :placeholder="t('tableArea.filter.enabledPlaceholder')">
            <el-option :label="t('dict.enableStatus.ENABLED')" :value="true" />
            <el-option :label="t('dict.enableStatus.DISABLED')" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('tableArea.filter.keyword')">
          <el-input
            v-model="filters.keyword"
            clearable
            :placeholder="t('tableArea.filter.keywordPlaceholder')"
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
        <div class="action-bar__hint">{{ t('page.tableAreas.description') }}</div>
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
        <el-table-column prop="storeName" :label="t('tableArea.columns.storeName')" min-width="180" />
        <el-table-column prop="areaName" :label="t('tableArea.columns.areaName')" min-width="180" />
        <el-table-column prop="areaCode" :label="t('tableArea.columns.areaCode')" min-width="160" />
        <el-table-column prop="sortOrder" :label="t('tableArea.columns.sortOrder')" min-width="100" />
        <el-table-column :label="t('tableArea.columns.enabled')" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? t('dict.enableStatus.ENABLED') : t('dict.enableStatus.DISABLED') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tableCount" :label="t('tableArea.columns.tableCount')" min-width="120" />
        <el-table-column v-if="canOperateTableArea" :label="t('common.actions')" min-width="180" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="canUpdateTableArea" link type="primary" @click="openEditDialog(row)">{{ t('common.edit') }}</el-button>
              <el-button
                v-if="canEnableTableArea && !row.enabled"
                link
                type="success"
                @click="toggleEnabled(row, true)"
              >
                {{ t('tableArea.actions.enable') }}
              </el-button>
              <el-button
                v-if="canEnableTableArea && row.enabled"
                link
                type="danger"
                @click="toggleEnabled(row, false)"
              >
                {{ t('tableArea.actions.disable') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? t('tableArea.toolbar.add') : t('common.edit')"
      width="540px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <el-form-item :label="t('tableArea.form.store')" prop="storeId">
          <el-select v-model="form.storeId" :placeholder="t('tableArea.form.storePlaceholder')">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('tableArea.form.areaName')" prop="areaName">
          <el-input v-model="form.areaName" :placeholder="t('tableArea.form.areaNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('tableArea.form.areaCode')" prop="areaCode">
          <el-input v-model="form.areaCode" :placeholder="t('tableArea.form.areaCodePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('tableArea.form.sortOrder')" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('tableArea.form.enabled')" prop="enabled">
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
import { fetchStores, type StoreSummary } from '@/api/stores';
import {
  createTableArea,
  fetchTableAreas,
  updateTableArea,
  updateTableAreaEnabled,
  type TableAreaPayload,
  type TableAreaSummary,
} from '@/api/table-areas';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';

interface Filters {
  storeId?: number;
  keyword: string;
  enabled?: boolean;
}

interface AreaFormModel {
  id?: number;
  storeId?: number;
  areaName: string;
  areaCode: string;
  sortOrder: number;
  enabled: boolean;
}

const { t } = useI18n();
const authStore = useAuthStore();

const loading = ref(false);
const submitting = ref(false);
const errorMessage = ref('');
const rows = ref<TableAreaSummary[]>([]);
const storeOptions = ref<StoreSummary[]>([]);
const dialogVisible = ref(false);
const dialogMode = ref<'create' | 'edit'>('create');
const formRef = ref<FormInstance>();

const filters = reactive<Filters>({
  keyword: '',
});
const form = reactive<AreaFormModel>({
  areaName: '',
  areaCode: '',
  sortOrder: 10,
  enabled: true,
});

const enabledCount = computed(() => rows.value.filter((item) => item.enabled).length);
const totalTableCount = computed(() => rows.value.reduce((sum, item) => sum + item.tableCount, 0));
const canCreateTableArea = computed(() => authStore.hasPermission('table.area:create'));
const canUpdateTableArea = computed(() => authStore.hasPermission('table.area:update'));
const canEnableTableArea = computed(() => authStore.hasPermission('table.area:enable'));
const canOperateTableArea = computed(() => canUpdateTableArea.value || canEnableTableArea.value);

const rules: FormRules<AreaFormModel> = {
  storeId: [{ required: true, message: t('tableArea.form.storePlaceholder'), trigger: 'change' }],
  areaName: [{ required: true, message: t('tableArea.form.areaNamePlaceholder'), trigger: 'blur' }],
  areaCode: [{ required: true, message: t('tableArea.form.areaCodePlaceholder'), trigger: 'blur' }],
  sortOrder: [{ required: true, message: t('tableArea.form.sortOrder'), trigger: 'change' }],
};

function resetFilters() {
  filters.storeId = undefined;
  filters.keyword = '';
  filters.enabled = undefined;
  loadRows();
}

function resetForm() {
  form.id = undefined;
  form.storeId = storeOptions.value.length === 1 ? storeOptions.value[0].id : undefined;
  form.areaName = '';
  form.areaCode = '';
  form.sortOrder = 10;
  form.enabled = true;
  formRef.value?.clearValidate();
}

function openCreateDialog() {
  if (!canCreateTableArea.value) {
    return;
  }
  dialogMode.value = 'create';
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(row: TableAreaSummary) {
  if (!canUpdateTableArea.value) {
    return;
  }
  dialogMode.value = 'edit';
  form.id = row.id;
  form.storeId = row.storeId;
  form.areaName = row.areaName;
  form.areaCode = row.areaCode;
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
    rows.value = await fetchTableAreas({
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

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid || !form.storeId) {
    return;
  }

  submitting.value = true;
  try {
    const payload: TableAreaPayload = {
      storeId: form.storeId,
      areaName: form.areaName.trim(),
      areaCode: form.areaCode.trim().toUpperCase(),
      sortOrder: form.sortOrder,
      enabled: form.enabled,
    };
    if (dialogMode.value === 'create') {
      if (!canCreateTableArea.value) {
        return;
      }
      await createTableArea(payload);
    } else if (form.id) {
      if (!canUpdateTableArea.value) {
        return;
      }
      await updateTableArea(form.id, payload);
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

async function toggleEnabled(row: TableAreaSummary, enabled: boolean) {
  if (!canEnableTableArea.value) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      `${row.areaName} · ${enabled ? t('common.enable') : t('common.disable')}`,
      t('common.actions'),
      { type: 'warning' },
    );
    await updateTableAreaEnabled(row.id, enabled);
    ElMessage.success(t('common.save'));
    await loadRows();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  }
}

onMounted(async () => {
  await loadStores();
  resetForm();
  await loadRows();
});
</script>
