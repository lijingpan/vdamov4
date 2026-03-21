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
        <el-table-column :label="t('role.columns.permissionCodes')" min-width="320">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="item in row.permissionCodes" :key="item" type="info">{{ item }}</el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column v-if="canUpdateRole || canDeleteRole" :label="t('common.actions')" min-width="180" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="canUpdateRole" link type="primary" @click="openEditDialog(row)">{{ t('common.edit') }}</el-button>
              <el-button v-if="canDeleteRole" link type="danger" @click="handleDelete(row)">{{ t('common.delete') }}</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? t('role.toolbar.add') : t('common.edit')"
      width="760px"
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
          <div class="role-menu-field">
            <div class="role-menu-field__toolbar">
              <span class="table-muted">{{ t('role.form.menuTreeHint') }}</span>
              <div class="table-actions">
                <el-button link type="primary" @click="expandAll">{{ t('role.form.expandAll') }}</el-button>
                <el-button link type="primary" @click="collapseAll">{{ t('role.form.collapseAll') }}</el-button>
                <el-button link type="primary" @click="checkAll">{{ t('role.form.checkAll') }}</el-button>
                <el-button link @click="clearAll">{{ t('role.form.clearAll') }}</el-button>
              </div>
            </div>
            <div class="role-menu-tree">
              <el-tree
                ref="menuTreeRef"
                node-key="id"
                show-checkbox
                check-strictly
                default-expand-all
                :data="menuTreeData"
                :props="{ label: 'name', children: 'children' }"
                @check="handleMenuCheck"
              >
                <template #default="{ data }">
                  <div class="role-menu-tree__node">
                    <span>{{ data.name }}</span>
                    <el-tag :type="data.menuType === 'BUTTON' ? 'warning' : 'success'" effect="light" size="small">
                      {{ data.menuType === 'BUTTON' ? t('menuManage.form.menuTypeButton') : t('menuManage.form.menuTypeMenu') }}
                    </el-tag>
                    <span v-if="data.permissionCode" class="role-menu-tree__permission">{{ data.permissionCode }}</span>
                  </div>
                </template>
              </el-tree>
            </div>
          </div>
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
import { computed, nextTick, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type TreeInstance } from 'element-plus';
import { Plus, RefreshRight } from '@element-plus/icons-vue';
import { useI18n } from 'vue-i18n';
import { fetchMenus, type MenuSummary } from '@/api/menus';
import { createRole, deleteRole, fetchRoles, updateRole, type RolePayload, type RoleSummary } from '@/api/roles';
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

interface MenuTreeNode extends MenuSummary {
  children?: MenuTreeNode[];
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
const menuTreeRef = ref<TreeInstance>();
const form = reactive<RoleFormModel>({
  code: '',
  name: '',
  menuIds: [],
});

const canCreateRole = computed(() => authStore.hasPermission('role:create'));
const canUpdateRole = computed(() => authStore.hasPermission('role:update'));
const canDeleteRole = computed(() => authStore.hasPermission('role:delete'));
const totalUsers = computed(() => rows.value.reduce((result, item) => result + item.userCount, 0));
const totalPermissionCodes = computed(() => new Set(rows.value.flatMap((item) => item.permissionCodes)).size);
const allMenuIds = computed(() => menuOptions.value.map((item) => item.id));
const menuTreeData = computed<MenuTreeNode[]>(() => buildMenuTree(menuOptions.value));

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
  menuTreeRef.value?.setCheckedKeys([]);
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

function buildMenuTree(items: MenuSummary[]): MenuTreeNode[] {
  const nodeMap = new Map<number, MenuTreeNode>();
  items.forEach((item) => {
    nodeMap.set(item.id, { ...item, children: [] });
  });

  const roots: MenuTreeNode[] = [];
  items.forEach((item) => {
    const node = nodeMap.get(item.id);
    if (!node) {
      return;
    }
    if (item.parentId && nodeMap.has(item.parentId)) {
      nodeMap.get(item.parentId)?.children?.push(node);
      return;
    }
    roots.push(node);
  });

  const sortNodes = (nodes: MenuTreeNode[]) => {
    nodes.sort((left, right) => {
      if (left.sortOrder !== right.sortOrder) {
        return left.sortOrder - right.sortOrder;
      }
      return left.id - right.id;
    });
    nodes.forEach((node) => sortNodes(node.children || []));
  };

  sortNodes(roots);
  return roots;
}

function syncCheckedMenuIds() {
  const checkedKeys = menuTreeRef.value?.getCheckedKeys(false) ?? [];
  form.menuIds = checkedKeys.filter((item): item is number => typeof item === 'number');
}

function handleMenuCheck() {
  syncCheckedMenuIds();
}

function expandAll() {
  Object.values(menuTreeRef.value?.store.nodesMap || {}).forEach((node) => node.expand());
}

function collapseAll() {
  Object.values(menuTreeRef.value?.store.nodesMap || {}).forEach((node) => node.collapse());
}

function checkAll() {
  menuTreeRef.value?.setCheckedKeys(allMenuIds.value);
  syncCheckedMenuIds();
}

function clearAll() {
  menuTreeRef.value?.setCheckedKeys([]);
  syncCheckedMenuIds();
}

function openCreateDialog() {
  dialogMode.value = 'create';
  resetForm();
  dialogVisible.value = true;
  nextTick(() => {
    menuTreeRef.value?.setCheckedKeys([]);
  });
}

function openEditDialog(row: RoleSummary) {
  dialogMode.value = 'edit';
  form.id = row.id;
  form.code = row.code;
  form.name = row.name;
  form.menuIds = inferMenuIds(row);
  dialogVisible.value = true;
  nextTick(() => {
    menuTreeRef.value?.setCheckedKeys(form.menuIds);
  });
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

async function handleDelete(row: RoleSummary) {
  try {
    await ElMessageBox.confirm(
      t('role.delete.confirm', { name: row.name }),
      t('role.delete.title'),
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
    await deleteRole(row.id);
    ElMessage.success(t('role.delete.success'));
    await loadData();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  }
}

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.role-menu-field {
  display: grid;
  gap: 10px;
}

.role-menu-field__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.role-menu-tree {
  max-height: 320px;
  overflow: auto;
  padding: 12px;
  border: 1px solid var(--app-border);
  border-radius: 14px;
  background: rgba(245, 248, 252, 0.72);
}

.role-menu-tree__node {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.role-menu-tree__permission {
  color: var(--app-text-soft);
  font-size: 12px;
}
</style>
