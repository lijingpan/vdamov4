<template>
  <PageShell :title="t('page.menus.title')" :description="t('page.menus.description')">
    <template #actions>
      <el-button :icon="RefreshRight" @click="loadMenus">{{ t('common.refresh') }}</el-button>
      <el-button v-if="canCreateMenu" type="primary" :icon="Plus" @click="openCreateDialog">
        {{ t('menuManage.toolbar.add') }}
      </el-button>
    </template>

    <template #meta>
      <div class="stats-grid stats-grid--three">
        <div class="stat-card">
          <div class="stat-card__label">{{ t('menuManage.summary.total') }}</div>
          <div class="stat-card__value">{{ rows.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('menuManage.summary.topLevel') }}</div>
          <div class="stat-card__value">{{ topLevelCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('menuManage.summary.permissionCount') }}</div>
          <div class="stat-card__value">{{ permissionCount }}</div>
        </div>
      </div>
    </template>

    <el-card shadow="never" class="page-card filter-card">
      <el-form :inline="true">
        <el-form-item :label="t('menuManage.filter.keyword')">
          <el-input
            v-model="filters.keyword"
            clearable
            :placeholder="t('menuManage.filter.keywordPlaceholder')"
            @keyup.enter="loadMenus"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadMenus">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page-card data-card">
      <div class="action-bar">
        <div class="action-bar__hint">{{ t('page.menus.description') }}</div>
      </div>

      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="false"
        style="margin: 16px 0"
      />

      <el-table
        v-loading="loading"
        :data="treeRows"
        stripe
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="name" :label="t('menuManage.columns.name')" min-width="220" />
        <el-table-column :label="t('menuManage.columns.parentName')" min-width="180">
          <template #default="{ row }">
            {{ resolveParentName(row.parentId, row.parentName) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('menuManage.columns.route')" min-width="180">
          <template #default="{ row }">
            <span v-if="row.route">{{ row.route }}</span>
            <span v-else class="table-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('menuManage.columns.menuType')" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.menuType === 'BUTTON' ? 'warning' : 'success'" effect="light">
              {{ row.menuType === 'BUTTON' ? t('menuManage.form.menuTypeButton') : t('menuManage.form.menuTypeMenu') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="permissionCode" :label="t('menuManage.columns.permissionCode')" min-width="220" />
        <el-table-column prop="sortOrder" :label="t('menuManage.columns.sortOrder')" min-width="100" />
        <el-table-column v-if="canUpdateMenu || canDeleteMenu" :label="t('common.actions')" min-width="180" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="canUpdateMenu" link type="primary" @click="openEditDialog(row)">{{ t('common.edit') }}</el-button>
              <el-button v-if="canDeleteMenu" link type="danger" @click="handleDelete(row)">{{ t('common.delete') }}</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? t('menuManage.toolbar.add') : t('common.edit')"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <el-form-item :label="t('menuManage.form.parent')" prop="parentId">
          <el-select v-model="form.parentId" clearable :placeholder="t('menuManage.form.parentPlaceholder')">
            <el-option :label="t('menuManage.form.parentRoot')" :value="null" />
            <el-option
              v-for="item in parentOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
              :disabled="item.id === form.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('menuManage.form.name')" prop="name">
          <el-input v-model="form.name" :placeholder="t('menuManage.form.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('menuManage.form.menuType')" prop="menuType">
          <el-select v-model="form.menuType" :placeholder="t('menuManage.form.menuTypePlaceholder')" @change="handleMenuTypeChange">
            <el-option :label="t('menuManage.form.menuTypeMenu')" value="MENU" />
            <el-option :label="t('menuManage.form.menuTypeButton')" value="BUTTON" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('menuManage.form.route')" prop="route">
          <el-input
            v-model="form.route"
            :placeholder="form.menuType === 'MENU' ? t('menuManage.form.routePlaceholder') : t('menuManage.form.routeDisabledHint')"
            :disabled="form.menuType === 'BUTTON'"
          />
        </el-form-item>
        <el-form-item :label="t('menuManage.form.permissionCode')" prop="permissionCode">
          <el-input v-model="form.permissionCode" :placeholder="t('menuManage.form.permissionCodePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('menuManage.form.sortOrder')" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :step="1" />
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
import { createMenu, deleteMenu, fetchMenus, updateMenu, type MenuPayload, type MenuSummary, type MenuType } from '@/api/menus';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';

interface MenuFilters {
  keyword: string;
}

interface MenuFormModel {
  id?: number;
  parentId: number | null;
  name: string;
  menuType: MenuType;
  route: string;
  permissionCode: string;
  sortOrder: number;
}

interface MenuTreeRow extends MenuSummary {
  children?: MenuTreeRow[];
}

const { t } = useI18n();
const authStore = useAuthStore();

const loading = ref(false);
const submitting = ref(false);
const errorMessage = ref('');
const rows = ref<MenuSummary[]>([]);
const filters = reactive<MenuFilters>({
  keyword: '',
});
const dialogVisible = ref(false);
const dialogMode = ref<'create' | 'edit'>('create');
const formRef = ref<FormInstance>();
const form = reactive<MenuFormModel>({
  parentId: null,
  name: '',
  menuType: 'MENU',
  route: '',
  permissionCode: '',
  sortOrder: 0,
});

const canCreateMenu = computed(() => authStore.hasPermission('menu:create'));
const canUpdateMenu = computed(() => authStore.hasPermission('menu:update'));
const canDeleteMenu = computed(() => authStore.hasPermission('menu:delete'));
const parentNameMap = computed(() =>
  rows.value.reduce<Record<number, string>>((result, item) => {
    result[item.id] = item.name;
    return result;
  }, {}),
);
const parentOptions = computed(() => rows.value.filter((item) => item.menuType === 'MENU'));
const topLevelCount = computed(() => rows.value.filter((item) => !item.parentId).length);
const permissionCount = computed(() => new Set(rows.value.map((item) => item.permissionCode).filter(Boolean)).size);
const treeRows = computed<MenuTreeRow[]>(() => buildTreeRows(rows.value));

const routeValidator = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (form.menuType === 'MENU' && !value.trim()) {
    callback(new Error(t('menuManage.form.routeRequired')));
    return;
  }
  callback();
};

const rules: FormRules<MenuFormModel> = {
  name: [{ required: true, message: t('menuManage.form.namePlaceholder'), trigger: 'blur' }],
  menuType: [{ required: true, message: t('menuManage.form.menuTypePlaceholder'), trigger: 'change' }],
  route: [
    { validator: routeValidator, trigger: 'blur' },
    { validator: routeValidator, trigger: 'change' },
  ],
  permissionCode: [{ required: true, message: t('menuManage.form.permissionCodePlaceholder'), trigger: 'blur' }],
  sortOrder: [{ required: true, message: t('menuManage.form.sortOrderPlaceholder'), trigger: 'change' }],
};

function buildTreeRows(items: MenuSummary[]): MenuTreeRow[] {
  const nodeMap = new Map<number, MenuTreeRow>();
  items.forEach((item) => {
    nodeMap.set(item.id, { ...item, children: [] });
  });

  const roots: MenuTreeRow[] = [];
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

  const sortNodes = (nodes: MenuTreeRow[]) => {
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

function resolveParentName(parentId: number | null, parentName: string): string {
  if (!parentId) {
    return '-';
  }
  return parentName || parentNameMap.value[parentId] || '-';
}

function resetFilters() {
  filters.keyword = '';
  loadMenus();
}

function resetForm() {
  form.id = undefined;
  form.parentId = null;
  form.name = '';
  form.menuType = 'MENU';
  form.route = '';
  form.permissionCode = '';
  form.sortOrder = 0;
  formRef.value?.clearValidate();
}

function openCreateDialog() {
  dialogMode.value = 'create';
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(row: MenuSummary) {
  dialogMode.value = 'edit';
  form.id = row.id;
  form.parentId = row.parentId;
  form.name = row.name;
  form.menuType = row.menuType;
  form.route = row.route;
  form.permissionCode = row.permissionCode;
  form.sortOrder = row.sortOrder;
  dialogVisible.value = true;
}

function handleMenuTypeChange() {
  if (form.menuType === 'BUTTON') {
    form.route = '';
  }
  formRef.value?.clearValidate(['route']);
}

async function loadMenus() {
  loading.value = true;
  errorMessage.value = '';
  try {
    rows.value = await fetchMenus({
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
  if (!valid) {
    return;
  }

  const payload: MenuPayload = {
    parentId: form.parentId,
    name: form.name.trim(),
    menuType: form.menuType,
    route: form.menuType === 'MENU' ? form.route.trim() : '',
    permissionCode: form.permissionCode.trim(),
    sortOrder: form.sortOrder,
  };

  submitting.value = true;
  try {
    if (dialogMode.value === 'create') {
      await createMenu(payload);
    } else if (form.id) {
      await updateMenu(form.id, payload);
    }
    dialogVisible.value = false;
    ElMessage.success(t('common.save'));
    await loadMenus();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(row: MenuSummary) {
  try {
    await ElMessageBox.confirm(
      t('menuManage.delete.confirm', { name: row.name }),
      t('menuManage.delete.title'),
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
    await deleteMenu(row.id);
    ElMessage.success(t('menuManage.delete.success'));
    await loadMenus();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('common.requestFailed'));
  }
}

onMounted(() => {
  loadMenus();
});
</script>
