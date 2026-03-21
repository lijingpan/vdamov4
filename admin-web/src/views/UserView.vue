<template>
  <PageShell :title="t('page.users.title')" :description="t('page.users.description')">
    <template #actions>
      <el-button :icon="RefreshRight" @click="loadData">{{ t('common.refresh') }}</el-button>
      <el-button v-if="canCreateUser" type="primary" :icon="Plus" @click="openCreateDialog">
        {{ t('user.toolbar.add') }}
      </el-button>
    </template>

    <template #meta>
      <div class="stats-grid stats-grid--three">
        <div class="stat-card">
          <div class="stat-card__label">{{ t('user.summary.total') }}</div>
          <div class="stat-card__value">{{ rows.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('user.summary.enabled') }}</div>
          <div class="stat-card__value">{{ enabledCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('user.summary.disabled') }}</div>
          <div class="stat-card__value">{{ disabledCount }}</div>
        </div>
      </div>
    </template>

    <el-card shadow="never" class="page-card filter-card">
      <el-form :inline="true">
        <el-form-item :label="t('user.filter.store')">
          <el-select v-model="filters.storeId" clearable :placeholder="t('user.filter.storePlaceholder')">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('user.filter.status')">
          <el-select v-model="filters.enabled" clearable :placeholder="t('user.filter.statusPlaceholder')">
            <el-option :label="t('dict.enableStatus.ENABLED')" :value="true" />
            <el-option :label="t('dict.enableStatus.DISABLED')" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('user.filter.keyword')">
          <el-input
            v-model="filters.keyword"
            clearable
            :placeholder="t('user.filter.keywordPlaceholder')"
            @keyup.enter="loadUsers"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadUsers">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page-card data-card">
      <div class="action-bar">
        <div class="action-bar__hint">{{ t('page.users.description') }}</div>
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
        <el-table-column prop="username" :label="t('user.columns.username')" min-width="160" />
        <el-table-column prop="displayName" :label="t('user.columns.displayName')" min-width="160" />
        <el-table-column :label="t('user.columns.enabled')" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? t('dict.enableStatus.ENABLED') : t('dict.enableStatus.DISABLED') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('user.columns.roles')" min-width="220">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="item in row.roleCodes" :key="item" type="info">{{ item }}</el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column :label="t('user.columns.stores')" min-width="240">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="item in row.storeNames" :key="item" type="warning">{{ item }}</el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column v-if="canUpdateUser || canDeleteUser" :label="t('common.actions')" min-width="180" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="canUpdateUser" link type="primary" @click="openEditDialog(row)">{{ t('common.edit') }}</el-button>
              <el-button v-if="canDeleteUser" link type="danger" @click="handleDelete(row)">{{ t('common.delete') }}</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? t('user.toolbar.add') : t('common.edit')"
      width="620px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="dialog-form"
        v-loading="detailLoading"
      >
        <el-form-item :label="t('user.form.username')" prop="username">
          <el-input v-model="form.username" maxlength="64" :placeholder="t('user.form.usernamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('user.form.password')" prop="password">
          <el-input
            v-model="form.password"
            show-password
            maxlength="128"
            :placeholder="dialogMode === 'create' ? t('user.form.passwordPlaceholder') : t('user.form.passwordPlaceholderOptional')"
          />
        </el-form-item>
        <el-form-item :label="t('user.form.displayName')" prop="displayName">
          <el-input v-model="form.displayName" maxlength="100" :placeholder="t('user.form.displayNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('user.form.enabled')" prop="enabled">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item :label="t('user.form.roleIds')" prop="roleIds">
          <el-select v-model="form.roleIds" multiple filterable :placeholder="t('user.form.roleIdsPlaceholder')">
            <el-option v-for="item in roleOptions" :key="item.id" :label="`${item.code} / ${item.name}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('user.form.storeIds')" prop="storeIds">
          <el-select v-model="form.storeIds" multiple filterable :placeholder="t('user.form.storeIdsPlaceholder')">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
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
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { Plus, RefreshRight } from '@element-plus/icons-vue';
import { useI18n } from 'vue-i18n';
import { fetchRoles, type RoleSummary } from '@/api/roles';
import { fetchStores, type StoreSummary } from '@/api/stores';
import {
  createUser,
  deleteUser,
  fetchUserDetail,
  fetchUsers,
  updateUser,
  type UserPayload,
  type UserSummary,
} from '@/api/users';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';

interface UserFilters {
  keyword: string;
  enabled?: boolean;
  storeId?: number;
}

interface UserFormModel {
  id?: number;
  username: string;
  password: string;
  displayName: string;
  enabled: boolean;
  roleIds: number[];
  storeIds: number[];
}

const { t } = useI18n();
const authStore = useAuthStore();

const loading = ref(false);
const submitting = ref(false);
const detailLoading = ref(false);
const errorMessage = ref('');
const rows = ref<UserSummary[]>([]);
const roleOptions = ref<RoleSummary[]>([]);
const storeOptions = ref<StoreSummary[]>([]);
const formRef = ref<FormInstance>();
const dialogVisible = ref(false);
const dialogMode = ref<'create' | 'edit'>('create');
const filters = reactive<UserFilters>({
  keyword: '',
});
const form = reactive<UserFormModel>({
  username: '',
  password: '',
  displayName: '',
  enabled: true,
  roleIds: [],
  storeIds: [],
});

const canCreateUser = computed(() => authStore.hasPermission('user:create'));
const canUpdateUser = computed(() => authStore.hasPermission('user:update'));
const canDeleteUser = computed(() => authStore.hasPermission('user:delete'));
const enabledCount = computed(() => rows.value.filter((item) => item.enabled).length);
const disabledCount = computed(() => rows.value.filter((item) => !item.enabled).length);
const storeNameById = computed(() =>
  storeOptions.value.reduce<Record<number, string>>((result, item) => {
    result[item.id] = item.name;
    return result;
  }, {}),
);
const tableRows = computed(() =>
  rows.value.map((item) => {
    const fallbackStoreNames = item.storeIds
      .map((id) => storeNameById.value[id])
      .filter((name): name is string => Boolean(name));
    return {
      ...item,
      storeNames: item.storeNames.length ? item.storeNames : fallbackStoreNames,
    };
  }),
);

const rules = computed<FormRules<UserFormModel>>(() => ({
  username: [{ required: true, message: t('user.form.usernamePlaceholder'), trigger: 'blur' }],
  password:
    dialogMode.value === 'create'
      ? [{ required: true, message: t('user.form.passwordPlaceholder'), trigger: 'blur' }]
      : [],
  displayName: [{ required: true, message: t('user.form.displayNamePlaceholder'), trigger: 'blur' }],
  roleIds: [{ type: 'array', required: true, min: 1, message: t('user.form.roleIdsPlaceholder'), trigger: 'change' }],
  storeIds: [{ type: 'array', required: true, min: 1, message: t('user.form.storeIdsPlaceholder'), trigger: 'change' }],
}));

function resetFilters() {
  filters.keyword = '';
  filters.enabled = undefined;
  filters.storeId = undefined;
  loadUsers();
}

function resetForm() {
  form.id = undefined;
  form.username = '';
  form.password = '';
  form.displayName = '';
  form.enabled = true;
  form.roleIds = [];
  form.storeIds = [];
  formRef.value?.clearValidate();
}

function openCreateDialog() {
  dialogMode.value = 'create';
  resetForm();
  dialogVisible.value = true;
}

async function openEditDialog(row: UserSummary) {
  dialogMode.value = 'edit';
  resetForm();
  detailLoading.value = true;
  try {
    const detail = await fetchUserDetail(row.id);
    form.id = detail.id;
    form.username = detail.username;
    form.password = '';
    form.displayName = detail.displayName;
    form.enabled = detail.enabled;
    form.roleIds = [...new Set(detail.roleIds)];
    form.storeIds = [...new Set(detail.storeIds)];
    dialogVisible.value = true;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  } finally {
    detailLoading.value = false;
  }
}

async function loadUsers() {
  loading.value = true;
  errorMessage.value = '';
  try {
    rows.value = await fetchUsers({
      keyword: filters.keyword || undefined,
      enabled: filters.enabled,
      storeId: filters.storeId,
    });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

async function loadRoles() {
  try {
    roleOptions.value = await fetchRoles();
  } catch {
    roleOptions.value = [];
  }
}

async function loadStores() {
  try {
    storeOptions.value = await fetchStores();
  } catch {
    storeOptions.value = [];
  }
}

async function loadData() {
  await Promise.all([loadRoles(), loadStores()]);
  await loadUsers();
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  const payload: UserPayload = {
    username: form.username.trim(),
    displayName: form.displayName.trim(),
    enabled: form.enabled,
    roleIds: [...new Set(form.roleIds)],
    storeIds: [...new Set(form.storeIds)],
  };
  if (form.password.trim()) {
    payload.password = form.password.trim();
  }

  submitting.value = true;
  try {
    if (dialogMode.value === 'create') {
      await createUser(payload);
    } else if (form.id) {
      await updateUser(form.id, payload);
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

async function handleDelete(row: UserSummary) {
  try {
    await ElMessageBox.confirm(
      t('user.delete.confirm', { name: row.displayName || row.username }),
      t('user.delete.title'),
      {
        type: 'warning',
        confirmButtonText: t('common.yes'),
        cancelButtonText: t('common.cancel'),
      },
    );
  } catch {
    return;
  }

  try {
    await deleteUser(row.id);
    ElMessage.success(t('user.delete.success'));
    await loadData();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  }
}

onMounted(() => {
  loadData();
});
</script>
