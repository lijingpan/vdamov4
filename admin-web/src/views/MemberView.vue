<template>
  <PageShell :title="t('page.members.title')" :description="t('page.members.description')">
    <template #actions>
      <el-button :icon="RefreshRight" @click="loadData">{{ t('common.refresh') }}</el-button>
      <el-button v-if="canCreateMember" type="primary" :icon="Plus" @click="openCreateDialog">
        {{ t('member.toolbar.add') }}
      </el-button>
    </template>

    <template #meta>
      <div class="stats-grid stats-grid--three">
        <div class="stat-card">
          <div class="stat-card__label">{{ t('member.summary.total') }}</div>
          <div class="stat-card__value">{{ rows.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('member.summary.cnMembers') }}</div>
          <div class="stat-card__value">{{ cnCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">{{ t('member.summary.thMembers') }}</div>
          <div class="stat-card__value">{{ thCount }}</div>
        </div>
      </div>
    </template>

    <el-card shadow="never" class="page-card filter-card">
      <el-form :inline="true">
        <el-form-item :label="t('member.filter.store')">
          <el-select v-model="filters.storeId" clearable :placeholder="t('member.filter.storePlaceholder')">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('member.filter.countryCode')">
          <el-select v-model="filters.countryCode" clearable :placeholder="t('member.filter.countryCodePlaceholder')">
            <el-option v-for="item in countryCodes" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('member.filter.levelCode')">
          <el-select v-model="filters.levelCode" clearable :placeholder="t('member.filter.levelCodePlaceholder')">
            <el-option v-for="item in levelCodes" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('member.filter.keyword')">
          <el-input
            v-model="filters.keyword"
            clearable
            :placeholder="t('member.filter.keywordPlaceholder')"
            @keyup.enter="loadMembers"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadMembers">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page-card data-card">
      <div class="action-bar">
        <div class="action-bar__hint">{{ t('page.members.description') }}</div>
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
        <el-table-column prop="storeName" :label="t('member.columns.storeName')" min-width="180" />
        <el-table-column prop="displayName" :label="t('member.columns.displayName')" min-width="160" />
        <el-table-column prop="levelCode" :label="t('member.columns.levelCode')" min-width="120" />
        <el-table-column prop="countryCode" :label="t('member.columns.countryCode')" min-width="120" />
        <el-table-column prop="phoneNational" :label="t('member.columns.phoneNational')" min-width="150" />
        <el-table-column prop="phoneE164" :label="t('member.columns.phoneE164')" min-width="180" />
        <el-table-column v-if="canUpdateMember" :label="t('common.actions')" min-width="120" fixed="right">
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
      :title="dialogMode === 'create' ? t('member.toolbar.add') : t('common.edit')"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <el-form-item :label="t('member.form.store')" prop="storeId">
          <el-select v-model="form.storeId" :placeholder="t('member.form.storePlaceholder')">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('member.form.levelCode')" prop="levelCode">
          <el-input v-model="form.levelCode" :placeholder="t('member.form.levelCodePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('member.form.displayName')" prop="displayName">
          <el-input v-model="form.displayName" :placeholder="t('member.form.displayNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('member.form.countryCode')" prop="countryCode">
          <el-input v-model="form.countryCode" :placeholder="t('member.form.countryCodePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('member.form.phoneNational')" prop="phoneNational">
          <el-input v-model="form.phoneNational" :placeholder="t('member.form.phoneNationalPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('member.form.phoneE164')" prop="phoneE164">
          <el-input v-model="form.phoneE164" :placeholder="t('member.form.phoneE164Placeholder')" />
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
import {
  createMember,
  fetchMembers,
  updateMember,
  type MemberPayload,
  type MemberSummary,
} from '@/api/members';
import { fetchStores, type StoreSummary } from '@/api/stores';
import PageShell from '@/components/PageShell.vue';
import { useAuthStore } from '@/stores/auth';

interface MemberFilters {
  storeId?: number;
  countryCode: string;
  levelCode: string;
  keyword: string;
}

interface MemberFormModel {
  id?: number;
  storeId?: number;
  levelCode: string;
  displayName: string;
  countryCode: string;
  phoneNational: string;
  phoneE164: string;
}

const { t } = useI18n();
const authStore = useAuthStore();

const loading = ref(false);
const submitting = ref(false);
const errorMessage = ref('');
const rows = ref<MemberSummary[]>([]);
const storeOptions = ref<StoreSummary[]>([]);
const formRef = ref<FormInstance>();
const dialogVisible = ref(false);
const dialogMode = ref<'create' | 'edit'>('create');

const filters = reactive<MemberFilters>({
  countryCode: '',
  levelCode: '',
  keyword: '',
});
const form = reactive<MemberFormModel>({
  levelCode: '',
  displayName: '',
  countryCode: '',
  phoneNational: '',
  phoneE164: '',
});

const canCreateMember = computed(() => authStore.hasPermission('member:create'));
const canUpdateMember = computed(() => authStore.hasPermission('member:update'));
const storeNameMap = computed(() =>
  storeOptions.value.reduce<Record<number, string>>((result, item) => {
    result[item.id] = item.name;
    return result;
  }, {}),
);
const countryCodes = computed(() => {
  const set = new Set<string>(['+86', '+66']);
  rows.value.forEach((item) => {
    if (item.countryCode) {
      set.add(item.countryCode);
    }
  });
  return Array.from(set);
});
const levelCodes = computed(() => {
  const set = new Set<string>();
  rows.value.forEach((item) => {
    if (item.levelCode) {
      set.add(item.levelCode);
    }
  });
  return Array.from(set);
});
const tableRows = computed(() =>
  rows.value.map((item) => ({
    ...item,
    storeName: item.storeName || storeNameMap.value[item.storeId] || '',
  })),
);
const cnCount = computed(() => rows.value.filter((item) => item.countryCode.toUpperCase() === '+86').length);
const thCount = computed(() => rows.value.filter((item) => item.countryCode.toUpperCase() === '+66').length);

const rules: FormRules<MemberFormModel> = {
  storeId: [{ required: true, message: t('member.form.storePlaceholder'), trigger: 'change' }],
  levelCode: [{ required: true, message: t('member.form.levelCodePlaceholder'), trigger: 'blur' }],
  displayName: [{ required: true, message: t('member.form.displayNamePlaceholder'), trigger: 'blur' }],
  countryCode: [{ required: true, message: t('member.form.countryCodePlaceholder'), trigger: 'blur' }],
  phoneNational: [{ required: true, message: t('member.form.phoneNationalPlaceholder'), trigger: 'blur' }],
  phoneE164: [{ required: true, message: t('member.form.phoneE164Placeholder'), trigger: 'blur' }],
};

function resetFilters() {
  filters.storeId = undefined;
  filters.countryCode = '';
  filters.levelCode = '';
  filters.keyword = '';
  loadMembers();
}

function resetForm() {
  form.id = undefined;
  form.storeId = storeOptions.value.length === 1 ? storeOptions.value[0].id : undefined;
  form.levelCode = '';
  form.displayName = '';
  form.countryCode = '';
  form.phoneNational = '';
  form.phoneE164 = '';
  formRef.value?.clearValidate();
}

function openCreateDialog() {
  if (!canCreateMember.value) {
    return;
  }
  dialogMode.value = 'create';
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(row: MemberSummary) {
  if (!canUpdateMember.value) {
    return;
  }
  dialogMode.value = 'edit';
  form.id = row.id;
  form.storeId = row.storeId;
  form.levelCode = row.levelCode;
  form.displayName = row.displayName;
  form.countryCode = row.countryCode;
  form.phoneNational = row.phoneNational;
  form.phoneE164 = row.phoneE164;
  dialogVisible.value = true;
}

async function loadStores() {
  try {
    storeOptions.value = await fetchStores();
  } catch {
    storeOptions.value = [];
  }
}

async function loadMembers() {
  loading.value = true;
  errorMessage.value = '';
  try {
    rows.value = await fetchMembers({
      storeId: filters.storeId,
      countryCode: filters.countryCode || undefined,
      levelCode: filters.levelCode || undefined,
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
  await loadMembers();
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid || !form.storeId) {
    return;
  }

  const payload: MemberPayload = {
    storeId: form.storeId,
    levelCode: form.levelCode.trim(),
    displayName: form.displayName.trim(),
    countryCode: form.countryCode.trim(),
    phoneNational: form.phoneNational.trim(),
    phoneE164: form.phoneE164.trim(),
  };

  submitting.value = true;
  try {
    if (dialogMode.value === 'create') {
      if (!canCreateMember.value) {
        return;
      }
      await createMember(payload);
    } else if (form.id) {
      if (!canUpdateMember.value) {
        return;
      }
      await updateMember(form.id, payload);
    }
    dialogVisible.value = false;
    ElMessage.success(t('common.save'));
    await loadMembers();
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
