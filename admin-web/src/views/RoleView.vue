<template>
  <PageShell :title="t('page.roles.title')" :description="t('page.roles.description')">
    <template #actions>
      <el-button :icon="RefreshRight" @click="loadData">{{ t('common.refresh') }}</el-button>
      <el-button v-if="canCreateRole" type="primary" :icon="Plus" @click="openCreateDialog">
        {{ t('role.toolbar.add') }}
      </el-button>
    </template>

    <template #meta>
      <div class="stats-grid stats-grid--three">
        <div class="stat-card">
          <div class="stat-card__label">{{ t('role.summary.total') }}</div>
          <div class="stat-card__value">{{ rows.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('role.summary.totalUsers') }}</div>
          <div class="stat-card__value">{{ totalUsers }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('role.summary.totalPermissions') }}</div>
          <div class="stat-card__value">{{ totalPermissionCodes }}</div>
        </div>
      </div>
    </template>

    <el-card shadow="never" class="page-card filter-card">
      <el-form :inline="true">
        <el-form-item :label="t('role.filter.keyword')">
          <el-input
            v-model="filters.keyword"
            clearable
            :placeholder="t('role.filter.keywordPlaceholder')"
            @keyup.enter="loadRoles"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRoles">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page-card data-card">
      <div class="action-bar">
        <div class="action-bar__hint">{{ t('page.roles.description') }}</div>
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
        <el-table-column prop="code" :label="t('role.columns.code')" min-width="180" />
        <el-table-column prop="name" :label="t('role.columns.name')" min-width="180" />
        <el-table-column prop="menuCount" :label="t('role.columns.menuCount')" min-width="120" />
        <el-table-column prop="userCount" :label="t('role.columns.userCount')" min-width="120" />
        <el-table-column :label="t('role.columns.permissionCodes')" min-width="280">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="item in row.permissionCodes" :key="item" type="info">{{ item }}</el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column v-if="canUpdateRole" :label="t('common.actions')" min-width="120" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="openEditDialog(row)">{{ t('common.edit') }}</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? t('role.toolbar.add') : t('common.edit')"
      width="620px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <el-form-item :label="t('role.form.code')" prop="code">
          <el-input v-model="form.code" maxlength="64" :placeholder="t('role.form.codePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('role.form.name')" prop="name">
          <el-input v-model="form.name" maxlength="100" :placeholder="t('role.form.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('role.form.menuIds')" prop="menuIds">
          <el-select
            v-model="form.menuIds"
            multiple
            filterable
            clearable
            :placeholder="t('role.form.menuIdsPlaceholder')"
          >
            <el-option
              v-for="item in menuOptions"
              :key="item.id"
              :label="`${item.name} · ${item.permissionCode}`"
              :value="item.id"
            />
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
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { Plus, RefreshRight } from '@element-plus/icons-vue';
import { useI18n } from 'vue-i18n';
import { fetchMenus, type MenuSummary } from '@/api/menus';
import { createRole, fetchRoles, updateRole, type RolePayload, type RoleSummary } from '@/api/roles';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';

interface RoleFilters {
  keyword: string;
}

interface RoleFormModel {
  id?: number;
  code: string;
  name: string;
  menuIds: number[];
}

const { t } = useI18n();
const authStore = useAuthStore();

const loading = ref(false);
const submitting = ref(false);
const errorMessage = ref('');
const rows = ref<RoleSummary[]>([]);
const menuOptions = ref<MenuSummary[]>([]);
const filters = reactive<RoleFilters>({
  keyword: '',
});
const dialogVisible = ref(false);
const dialogMode = ref<'create' | 'edit'>('create');
const formRef = ref<FormInstance>();
const form = reactive<RoleFormModel>({
  code: '',
  name: '',
  menuIds: [],
});

const canCreateRole = computed(() => authStore.hasPermission('role:create'));
const canUpdateRole = computed(() => authStore.hasPermission('role:update'));
const totalUsers = computed(() => rows.value.reduce((result, item) => result + item.userCount, 0));
const totalPermissionCodes = computed(() =>
  new Set(rows.value.flatMap((item) => item.permissionCodes)).size,
);

const rules: FormRules<RoleFormModel> = {
  code: [{ required: true, message: t('role.form.codePlaceholder'), trigger: 'blur' }],
  name: [{ required: true, message: t('role.form.namePlaceholder'), trigger: 'blur' }],
  menuIds: [{ type: 'array', required: true, min: 1, message: t('role.form.menuIdsPlaceholder'), trigger: 'change' }],
};

function resetFilters() {
  filters.keyword = '';
  loadRoles();
}

function resetForm() {
  form.id = undefined;
  form.code = '';
  form.name = '';
  form.menuIds = [];
  formRef.value?.clearValidate();
}

function inferMenuIds(role: RoleSummary): number[] {
  if (role.menuIds.length > 0) {
    return [...role.menuIds];
  }
  const permissionSet = new Set(role.permissionCodes);
  return menuOptions.value
    .filter((item) => permissionSet.has(item.permissionCode))
    .map((item) => item.id);
}

function openCreateDialog() {
  dialogMode.value = 'create';
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(row: RoleSummary) {
  dialogMode.value = 'edit';
  form.id = row.id;
  form.code = row.code;
  form.name = row.name;
  form.menuIds = inferMenuIds(row);
  dialogVisible.value = true;
}

async function loadRoles() {
  loading.value = true;
  errorMessage.value = '';
  try {
    rows.value = await fetchRoles({
      keyword: filters.keyword || undefined,
    });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

async function loadMenusForRole() {
  try {
    menuOptions.value = await fetchMenus();
  } catch {
    menuOptions.value = [];
  }
}

async function loadData() {
  await Promise.all([loadMenusForRole(), loadRoles()]);
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  const payload: RolePayload = {
    code: form.code.trim().toUpperCase(),
    name: form.name.trim(),
    menuIds: [...new Set(form.menuIds)],
  };

  submitting.value = true;
  try {
    if (dialogMode.value === 'create') {
      await createRole(payload);
    } else if (form.id) {
      await updateRole(form.id, payload);
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

onMounted(() => {
  loadData();
});
</script>
