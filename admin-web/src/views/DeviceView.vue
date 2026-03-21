<template>
  <PageShell :title="t('page.devices.title')" :description="t('page.devices.description')">
    <template #actions>
      <el-button :icon="RefreshRight" @click="loadData">{{ t('common.refresh') }}</el-button>
      <el-button v-if="canCreateDevice" type="primary" :icon="Plus" @click="openCreateDialog">
        {{ t('device.toolbar.add') }}
      </el-button>
    </template>

    <template #meta>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-card__label">{{ t('device.summary.total') }}</div>
          <div class="stat-card__value">{{ rows.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('device.summary.enabled') }}</div>
          <div class="stat-card__value">{{ enabledCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('device.summary.disabled') }}</div>
          <div class="stat-card__value">{{ disabledCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('device.summary.printers') }}</div>
          <div class="stat-card__value">{{ printerCount }}</div>
        </div>
      </div>
    </template>

    <el-card shadow="never" class="page-card filter-card">
      <el-form :inline="true">
        <el-form-item :label="t('device.filter.store')">
          <el-select v-model="filters.storeId" clearable :placeholder="t('device.filter.storePlaceholder')">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('device.filter.type')">
          <el-select v-model="filters.type" clearable :placeholder="t('device.filter.typePlaceholder')">
            <el-option v-for="item in deviceTypes" :key="item" :label="t(`dict.deviceType.${item}`)" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('device.filter.status')">
          <el-select v-model="filters.enabled" clearable :placeholder="t('device.filter.statusPlaceholder')">
            <el-option :label="t('dict.enableStatus.ENABLED')" :value="true" />
            <el-option :label="t('dict.enableStatus.DISABLED')" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('device.filter.keyword')">
          <el-input
            v-model="filters.keyword"
            clearable
            :placeholder="t('device.filter.keywordPlaceholder')"
            @keyup.enter="loadDevices"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadDevices">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page-card data-card">
      <div class="action-bar">
        <div class="action-bar__hint">{{ t('page.devices.description') }}</div>
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
        <el-table-column prop="storeName" :label="t('device.columns.storeName')" min-width="180" />
        <el-table-column prop="name" :label="t('device.columns.name')" min-width="180" />
        <el-table-column :label="t('device.columns.type')" min-width="150">
          <template #default="{ row }">
            {{ t(`dict.deviceType.${row.type}`) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('device.columns.purpose')" min-width="160">
          <template #default="{ row }">
            {{ t(`dict.devicePurpose.${row.purpose}`) }}
          </template>
        </el-table-column>
        <el-table-column prop="brand" :label="t('device.columns.brand')" min-width="140" />
        <el-table-column prop="sn" :label="t('device.columns.sn')" min-width="180" />
        <el-table-column prop="size" :label="t('device.columns.size')" min-width="120" />
        <el-table-column :label="t('device.columns.onlineStatus')" min-width="140">
          <template #default="{ row }">
            <el-tag :type="row.onlineStatus === 'ONLINE' ? 'success' : 'warning'">
              {{ t(`dict.onlineStatus.${row.onlineStatus}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('device.columns.status')" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? t('dict.enableStatus.ENABLED') : t('dict.enableStatus.DISABLED') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canOperateDevice" :label="t('common.actions')" min-width="200" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="canUpdateDevice" link type="primary" @click="openEditDialog(row)">
                {{ t('common.edit') }}
              </el-button>
              <el-button
                v-if="canEnableDevice && !row.enabled"
                link
                type="success"
                @click="changeEnabled(row, true)"
              >
                {{ t('device.actions.enable') }}
              </el-button>
              <el-button
                v-if="canEnableDevice && row.enabled"
                link
                type="danger"
                @click="changeEnabled(row, false)"
              >
                {{ t('device.actions.disable') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? t('device.toolbar.add') : t('common.edit')"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <el-form-item :label="t('device.form.store')" prop="storeId">
          <el-select v-model="form.storeId" :placeholder="t('device.form.storePlaceholder')">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('device.form.name')" prop="name">
          <el-input v-model="form.name" :placeholder="t('device.form.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('device.form.type')" prop="type">
          <el-select v-model="form.type" :placeholder="t('device.form.typePlaceholder')">
            <el-option v-for="item in deviceTypes" :key="item" :label="t(`dict.deviceType.${item}`)" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('device.form.purpose')" prop="purpose">
          <el-select v-model="form.purpose" :placeholder="t('device.form.purposePlaceholder')">
            <el-option
              v-for="item in devicePurposeOptions"
              :key="item"
              :label="t(`dict.devicePurpose.${item}`)"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('device.form.brand')" prop="brand">
          <el-input v-model="form.brand" :placeholder="t('device.form.brandPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('device.form.sn')" prop="sn">
          <el-input v-model="form.sn" :placeholder="t('device.form.snPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('device.form.size')" prop="size">
          <el-input v-model="form.size" :placeholder="t('device.form.sizePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('device.form.onlineStatus')" prop="onlineStatus">
          <el-select v-model="form.onlineStatus" :placeholder="t('device.form.onlineStatusPlaceholder')">
            <el-option v-for="item in onlineStatusOptions" :key="item" :label="t(`dict.onlineStatus.${item}`)" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('device.form.enabled')" prop="enabled">
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
  createDevice,
  fetchDevices,
  updateDevice,
  updateDeviceEnabled,
  type DevicePayload,
  type DeviceSummary,
} from '@/api/devices';
import { fetchStores, type StoreSummary } from '@/api/stores';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';

interface DeviceFilters {
  storeId?: number;
  type: string;
  enabled?: boolean;
  keyword: string;
}

interface DeviceFormModel {
  id?: number;
  storeId?: number;
  name: string;
  type: string;
  purpose: string;
  brand: string;
  sn: string;
  size: string;
  onlineStatus: string;
  enabled: boolean;
}

const { t } = useI18n();
const authStore = useAuthStore();

const loading = ref(false);
const submitting = ref(false);
const errorMessage = ref('');
const storeOptions = ref<StoreSummary[]>([]);
const rows = ref<DeviceSummary[]>([]);
const formRef = ref<FormInstance>();
const dialogVisible = ref(false);
const dialogMode = ref<'create' | 'edit'>('create');

const filters = reactive<DeviceFilters>({
  type: '',
  keyword: '',
});
const form = reactive<DeviceFormModel>({
  name: '',
  type: 'PRINTER',
  purpose: 'KITCHEN_PRINT',
  brand: '',
  sn: '',
  size: '',
  onlineStatus: 'ONLINE',
  enabled: true,
});

const deviceTypes = ['PRINTER', 'SPEAKER', 'KITCHEN_PRINTER', 'LABEL_PRINTER'];
const devicePurposeOptions = ['KITCHEN_PRINT', 'CHECKOUT_PRINT', 'ORDER_BROADCAST'];
const onlineStatusOptions = ['ONLINE', 'OFFLINE'];

const canCreateDevice = computed(() => authStore.hasPermission('device:create'));
const canUpdateDevice = computed(() => authStore.hasPermission('device:update'));
const canEnableDevice = computed(() => authStore.hasPermission('device:enable'));
const canOperateDevice = computed(() => canUpdateDevice.value || canEnableDevice.value);

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

const enabledCount = computed(() => rows.value.filter((item) => item.enabled).length);
const disabledCount = computed(() => rows.value.filter((item) => !item.enabled).length);
const printerCount = computed(() => rows.value.filter((item) => item.type.includes('PRINTER')).length);

const rules: FormRules<DeviceFormModel> = {
  storeId: [{ required: true, message: t('device.form.storePlaceholder'), trigger: 'change' }],
  name: [{ required: true, message: t('device.form.namePlaceholder'), trigger: 'blur' }],
  type: [{ required: true, message: t('device.form.typePlaceholder'), trigger: 'change' }],
  purpose: [{ required: true, message: t('device.form.purposePlaceholder'), trigger: 'change' }],
  onlineStatus: [{ required: true, message: t('device.form.onlineStatusPlaceholder'), trigger: 'change' }],
};

function resetFilters() {
  filters.storeId = undefined;
  filters.type = '';
  filters.enabled = undefined;
  filters.keyword = '';
  loadDevices();
}

function resetForm() {
  form.id = undefined;
  form.storeId = storeOptions.value.length === 1 ? storeOptions.value[0].id : undefined;
  form.name = '';
  form.type = 'PRINTER';
  form.purpose = 'KITCHEN_PRINT';
  form.brand = '';
  form.sn = '';
  form.size = '';
  form.onlineStatus = 'ONLINE';
  form.enabled = true;
  formRef.value?.clearValidate();
}

function openCreateDialog() {
  if (!canCreateDevice.value) {
    return;
  }
  dialogMode.value = 'create';
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(row: DeviceSummary) {
  if (!canUpdateDevice.value) {
    return;
  }
  dialogMode.value = 'edit';
  form.id = row.id;
  form.storeId = row.storeId;
  form.name = row.name;
  form.type = row.type;
  form.purpose = row.purpose;
  form.brand = row.brand;
  form.sn = row.sn;
  form.size = row.size;
  form.onlineStatus = row.onlineStatus || 'ONLINE';
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

async function loadDevices() {
  loading.value = true;
  errorMessage.value = '';
  try {
    rows.value = await fetchDevices({
      storeId: filters.storeId,
      type: filters.type || undefined,
      enabled: filters.enabled,
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
  await loadDevices();
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid || !form.storeId) {
    return;
  }

  const payload: DevicePayload = {
    storeId: form.storeId,
    name: form.name.trim(),
    type: form.type.trim(),
    purpose: form.purpose.trim(),
    brand: form.brand.trim(),
    sn: form.sn.trim(),
    size: form.size.trim(),
    onlineStatus: form.onlineStatus.trim(),
    enabled: form.enabled,
  };

  submitting.value = true;
  try {
    if (dialogMode.value === 'create') {
      if (!canCreateDevice.value) {
        return;
      }
      await createDevice(payload);
    } else if (form.id) {
      if (!canUpdateDevice.value) {
        return;
      }
      await updateDevice(form.id, payload);
    }
    dialogVisible.value = false;
    ElMessage.success(t('common.save'));
    await loadDevices();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  } finally {
    submitting.value = false;
  }
}

async function changeEnabled(row: DeviceSummary, enabled: boolean) {
  if (!canEnableDevice.value) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      `${row.name} - ${enabled ? t('device.actions.enable') : t('device.actions.disable')}`,
      t('common.actions'),
      { type: 'warning' },
    );
    await updateDeviceEnabled(row.id, enabled);
    ElMessage.success(t('common.save'));
    await loadDevices();
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
