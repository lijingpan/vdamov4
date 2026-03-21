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

      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column prop="name" :label="t('menuManage.columns.name')" min-width="180" />
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
        <el-table-column prop="permissionCode" :label="t('menuManage.columns.permissionCode')" min-width="220" />
        <el-table-column prop="sortOrder" :label="t('menuManage.columns.sortOrder')" min-width="100" />
        <el-table-column v-if="canUpdateMenu" :label="t('common.actions')" min-width="120" fixed="right">
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
        <el-form-item :label="t('menuManage.form.route')" prop="route">
          <el-input v-model="form.route" :placeholder="t('menuManage.form.routePlaceholder')" />
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
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { Plus, RefreshRight } from '@element-plus/icons-vue';
import { useI18n } from 'vue-i18n';
import { createMenu, fetchMenus, updateMenu, type MenuPayload, type MenuSummary } from '@/api/menus';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';

interface MenuFilters {
  keyword: string;
}

interface MenuFormModel {
  id?: number;
  parentId: number | null;
  name: string;
  route: string;
  permissionCode: string;
  sortOrder: number;
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
  route: '',
  permissionCode: '',
  sortOrder: 0,
});

const canCreateMenu = computed(() => authStore.hasPermission('menu:create'));
const canUpdateMenu = computed(() => authStore.hasPermission('menu:update'));
const parentNameMap = computed(() =>
  rows.value.reduce<Record<number, string>>((result, item) => {
    result[item.id] = item.name;
    return result;
  }, {}),
);
const parentOptions = computed(() => rows.value.filter((item) => !item.parentId));
const topLevelCount = computed(() => rows.value.filter((item) => !item.parentId).length);
const permissionCount = computed(() =>
  new Set(rows.value.map((item) => item.permissionCode).filter((item) => Boolean(item))).size,
);

const rules: FormRules<MenuFormModel> = {
  name: [{ required: true, message: t('menuManage.form.namePlaceholder'), trigger: 'blur' }],
  permissionCode: [{ required: true, message: t('menuManage.form.permissionCodePlaceholder'), trigger: 'blur' }],
  sortOrder: [{ required: true, message: t('menuManage.form.sortOrderPlaceholder'), trigger: 'change' }],
};

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
  form.route = row.route;
  form.permissionCode = row.permissionCode;
  form.sortOrder = row.sortOrder;
  dialogVisible.value = true;
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
    route: form.route.trim(),
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

onMounted(() => {
  loadMenus();
});
</script>
