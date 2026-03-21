<template>
  <PageShell :title="t('page.tables.title')" :description="t('page.tables.description')">
    <template #actions>
      <el-button :icon="RefreshRight" @click="loadRows">{{ t('common.refresh') }}</el-button>
      <el-button v-if="canCreateTable" type="primary" :icon="Plus" @click="openCreateDialog">
        {{ t('table.toolbar.add') }}
      </el-button>
    </template>

    <template #meta>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-card__label">{{ t('table.summary.total') }}</div>
          <div class="stat-card__value">{{ rows.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('table.summary.activeOrders') }}</div>
          <div class="stat-card__value">{{ activeCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('table.summary.waitingCheckout') }}</div>
          <div class="stat-card__value">{{ waitingCheckoutCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('table.summary.disabled') }}</div>
          <div class="stat-card__value">{{ disabledCount }}</div>
        </div>
      </div>
    </template>

    <el-card shadow="never" class="page-card filter-card">
      <el-form :inline="true">
        <el-form-item :label="t('table.filter.store')">
          <el-select v-model="filters.storeId" clearable :placeholder="t('table.filter.storePlaceholder')">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('table.filter.status')">
          <el-select v-model="filters.status" clearable :placeholder="t('table.filter.statusPlaceholder')">
            <el-option v-for="item in statusOptions" :key="item" :label="t(`dict.tableStatus.${item}`)" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('table.filter.keyword')">
          <el-input
            v-model="filters.keyword"
            clearable
            :placeholder="t('table.filter.keywordPlaceholder')"
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
        <div class="action-bar__hint">{{ t('page.tables.description') }}</div>
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
        <el-table-column prop="storeName" :label="t('table.columns.storeName')" min-width="180" />
        <el-table-column prop="areaName" :label="t('table.columns.areaName')" min-width="140" />
        <el-table-column prop="tableName" :label="t('table.columns.tableName')" min-width="160" />
        <el-table-column prop="tableCode" :label="t('table.columns.tableCode')" min-width="140" />
        <el-table-column prop="capacity" :label="t('table.columns.capacity')" min-width="110" />
        <el-table-column :label="t('table.columns.status')" min-width="150">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ t(`dict.tableStatus.${row.status}`) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentOrderNo" :label="t('table.columns.currentOrderNo')" min-width="180">
          <template #default="{ row }">
            <span :class="{ 'table-muted': !row.currentOrderNo }">{{ row.currentOrderNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('table.columns.currentAmount')" min-width="130">
          <template #default="{ row }">
            {{ formatCurrency(row.currentAmountInCent) }}
          </template>
        </el-table-column>
        <el-table-column v-if="canOperateTable" :label="t('common.actions')" min-width="220" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="canUpdateTable" link type="primary" @click="openEditDialog(row)">{{ t('common.edit') }}</el-button>
              <el-button
                v-if="canUpdateTableStatus && row.status !== 'IDLE' && row.status !== 'DISABLED'"
                link
                type="success"
                @click="changeStatus(row, 'IDLE')"
              >
                {{ t('table.actions.markIdle') }}
              </el-button>
              <el-button
                v-if="canUpdateTableStatus && row.status === 'DISABLED'"
                link
                type="success"
                @click="changeStatus(row, 'IDLE')"
              >
                {{ t('table.actions.enable') }}
              </el-button>
              <el-button
                v-if="canUpdateTableStatus && row.status !== 'DISABLED'"
                link
                type="danger"
                @click="changeStatus(row, 'DISABLED')"
              >
                {{ t('table.actions.disable') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? t('table.toolbar.add') : t('common.edit')"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <el-form-item :label="t('table.form.store')" prop="storeId">
          <el-select v-model="form.storeId" :placeholder="t('table.form.storePlaceholder')">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('table.form.areaName')" prop="areaName">
          <el-select v-model="form.areaName" :placeholder="t('table.form.areaNamePlaceholder')">
            <el-option v-for="item in availableAreas" :key="item.id" :label="item.areaName" :value="item.areaName" />
          </el-select>
          <div v-if="form.storeId && availableAreas.length === 0" class="helper-text">
            {{ t('table.helper.emptyAreas') }}
          </div>
        </el-form-item>
        <el-form-item :label="t('table.form.tableName')" prop="tableName">
          <el-input v-model="form.tableName" :placeholder="t('table.form.tableNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('table.form.capacity')" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('table.form.status')" prop="status">
          <el-select v-model="form.status">
            <el-option v-for="item in statusOptions" :key="item" :label="t(`dict.tableStatus.${item}`)" :value="item" />
          </el-select>
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
import { fetchStores, type StoreSummary } from '@/api/stores';
import { fetchTableAreas, type TableAreaSummary } from '@/api/table-areas';
import {
  createTable,
  fetchTables,
  updateTable,
  updateTableStatus,
  type TablePayload,
  type TableSummary,
} from '@/api/tables';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';

interface Filters {
  storeId?: number;
  status: string;
  keyword: string;
}

interface TableFormModel {
  id?: number;
  storeId?: number;
  areaName: string;
  tableName: string;
  capacity: number;
  status: string;
}

const { t } = useI18n();
const authStore = useAuthStore();

const loading = ref(false);
const submitting = ref(false);
const errorMessage = ref('');
const rows = ref<TableSummary[]>([]);
const storeOptions = ref<StoreSummary[]>([]);
const availableAreas = ref<TableAreaSummary[]>([]);
const dialogVisible = ref(false);
const dialogMode = ref<'create' | 'edit'>('create');
const formRef = ref<FormInstance>();

const filters = reactive<Filters>({
  status: '',
  keyword: '',
});
const form = reactive<TableFormModel>({
  areaName: '',
  tableName: '',
  capacity: 4,
  status: 'IDLE',
});

const statusOptions = ['IDLE', 'IN_USE', 'WAITING_CHECKOUT', 'WAITING_CLEAN', 'DISABLED'];
const activeCount = computed(() => rows.value.filter((item) => item.status === 'IN_USE').length);
const waitingCheckoutCount = computed(() => rows.value.filter((item) => item.status === 'WAITING_CHECKOUT').length);
const disabledCount = computed(() => rows.value.filter((item) => item.status === 'DISABLED').length);
const canCreateTable = computed(() => authStore.hasPermission('table:create'));
const canUpdateTable = computed(() => authStore.hasPermission('table:update'));
const canUpdateTableStatus = computed(() => authStore.hasPermission('table:status'));
const canOperateTable = computed(() => canUpdateTable.value || canUpdateTableStatus.value);

const rules: FormRules<TableFormModel> = {
  storeId: [{ required: true, message: t('table.form.storePlaceholder'), trigger: 'change' }],
  areaName: [{ required: true, message: t('table.form.areaNamePlaceholder'), trigger: 'change' }],
  tableName: [{ required: true, message: t('table.form.tableNamePlaceholder'), trigger: 'blur' }],
  capacity: [{ required: true, message: t('table.form.capacity'), trigger: 'change' }],
  status: [{ required: true, message: t('table.form.status'), trigger: 'change' }],
};

watch(
  () => form.storeId,
  async (value) => {
    if (!value) {
      availableAreas.value = [];
      form.areaName = '';
      return;
    }
    const currentArea = form.areaName;
    await loadAreas(value);
    if (!availableAreas.value.some((item) => item.areaName === currentArea)) {
      form.areaName = availableAreas.value[0]?.areaName ?? '';
    }
  },
);

function statusTagType(status: string): 'success' | 'warning' | 'danger' | 'info' {
  if (status === 'IDLE') {
    return 'success';
  }
  if (status === 'IN_USE' || status === 'WAITING_CHECKOUT') {
    return 'warning';
  }
  if (status === 'DISABLED') {
    return 'danger';
  }
  return 'info';
}

function formatCurrency(valueInCent: number): string {
  return valueInCent > 0 ? `¥ ${(valueInCent / 100).toFixed(2)}` : '-';
}

function resetFilters() {
  filters.storeId = undefined;
  filters.status = '';
  filters.keyword = '';
  loadRows();
}

function resetForm() {
  form.id = undefined;
  form.storeId = storeOptions.value.length === 1 ? storeOptions.value[0].id : filters.storeId;
  form.areaName = '';
  form.tableName = '';
  form.capacity = 4;
  form.status = 'IDLE';
  formRef.value?.clearValidate();
}

async function openCreateDialog() {
  if (!canCreateTable.value) {
    return;
  }
  dialogMode.value = 'create';
  resetForm();
  if (form.storeId) {
    await loadAreas(form.storeId);
    form.areaName = availableAreas.value[0]?.areaName ?? '';
  }
  dialogVisible.value = true;
}

async function openEditDialog(row: TableSummary) {
  if (!canUpdateTable.value) {
    return;
  }
  dialogMode.value = 'edit';
  form.id = row.id;
  form.storeId = row.storeId;
  form.tableName = row.tableName;
  form.capacity = row.capacity;
  form.status = row.status;
  await loadAreas(row.storeId);
  form.areaName = row.areaName;
  dialogVisible.value = true;
}

async function loadStores() {
  try {
    storeOptions.value = await fetchStores();
  } catch {
    storeOptions.value = [];
  }
}

async function loadAreas(storeId: number) {
  availableAreas.value = await fetchTableAreas({
    storeId,
    enabled: true,
  });
}

async function loadRows() {
  loading.value = true;
  errorMessage.value = '';
  try {
    rows.value = await fetchTables({
      storeId: filters.storeId,
      status: filters.status || undefined,
      keyword: filters.keyword || undefined,
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
    const payload: TablePayload = {
      storeId: form.storeId,
      areaName: form.areaName,
      tableName: form.tableName.trim(),
      capacity: form.capacity,
      status: form.status,
    };
    if (dialogMode.value === 'create') {
      if (!canCreateTable.value) {
        return;
      }
      await createTable(payload);
    } else if (form.id) {
      if (!canUpdateTable.value) {
        return;
      }
      await updateTable(form.id, payload);
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

async function changeStatus(row: TableSummary, nextStatus: string) {
  if (!canUpdateTableStatus.value) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      `${row.tableName} · ${t(`dict.tableStatus.${nextStatus}`)}`,
      t('common.actions'),
      { type: 'warning' },
    );
    await updateTableStatus(row.id, nextStatus);
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
