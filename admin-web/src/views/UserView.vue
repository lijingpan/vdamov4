<template>
  <PageShell :title="t('page.users.title')" :description="t('page.users.description')">
    <div v-loading="loading">
      <el-form :inline="true" class="filter-form">
        <el-form-item :label="t('user.filter.store')">
          <el-select v-model="storeId" clearable :placeholder="t('user.filter.storePlaceholder')">
            <el-option
              v-for="item in storeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('user.filter.status')">
          <el-select v-model="enabledFilter" clearable :placeholder="t('user.filter.statusPlaceholder')">
            <el-option :label="t('dict.enableStatus.ENABLED')" :value="true" />
            <el-option :label="t('dict.enableStatus.DISABLED')" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('user.filter.keyword')">
          <el-input v-model="keyword" clearable :placeholder="t('user.filter.keywordPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadUsers">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

      <el-table :data="filteredRows" stripe border>
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
      </el-table>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import type { StoreSummary } from '@/api/stores';
import { fetchStores } from '@/api/stores';
import type { UserSummary } from '@/api/users';
import { fetchUsers } from '@/api/users';
import PageShell from '@/components/PageShell.vue';

const { t } = useI18n();

const loading = ref(false);
const errorMessage = ref('');
const keyword = ref('');
const enabledFilter = ref<boolean | undefined>();
const storeId = ref<number | undefined>();
const rows = ref<UserSummary[]>([]);
const storeOptions = ref<StoreSummary[]>([]);

const storeNameMap = computed(() =>
  storeOptions.value.reduce<Record<number, string>>((result, item) => {
    result[item.id] = item.name;
    return result;
  }, {}),
);

const filteredRows = computed(() =>
  rows.value
    .map((item) => {
      const fallbackNames = item.storeIds
        .map((id) => storeNameMap.value[id])
        .filter((name): name is string => Boolean(name));
      return {
        ...item,
        storeNames: item.storeNames.length ? item.storeNames : fallbackNames,
      };
    })
    .filter((item) => {
      const matchEnabled = enabledFilter.value === undefined || item.enabled === enabledFilter.value;
      const matchStore = !storeId.value || item.storeIds.includes(storeId.value);
      const keywordValue = keyword.value.trim().toLowerCase();
      const matchKeyword =
        !keywordValue ||
        item.username.toLowerCase().includes(keywordValue) ||
        item.displayName.toLowerCase().includes(keywordValue) ||
        item.roleCodes.some((role) => role.toLowerCase().includes(keywordValue)) ||
        item.storeNames.some((storeName) => storeName.toLowerCase().includes(keywordValue));
      return matchEnabled && matchStore && matchKeyword;
    }),
);

function resetFilters() {
  keyword.value = '';
  enabledFilter.value = undefined;
  storeId.value = undefined;
}

async function loadStores() {
  try {
    storeOptions.value = await fetchStores();
  } catch {
    storeOptions.value = [];
  }
}

async function loadUsers() {
  loading.value = true;
  errorMessage.value = '';
  try {
    rows.value = await fetchUsers({
      storeId: storeId.value,
      enabled: enabledFilter.value,
      keyword: keyword.value || undefined,
    });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  await loadStores();
  await loadUsers();
});
</script>

<style scoped>
.filter-form {
  margin-bottom: 12px;
}
</style>
