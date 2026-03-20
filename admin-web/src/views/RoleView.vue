<template>
  <PageShell :title="t('page.roles.title')" :description="t('page.roles.description')">
    <div v-loading="loading">
      <el-form :inline="true" class="filter-form">
        <el-form-item :label="t('role.filter.keyword')">
          <el-input v-model="keyword" clearable :placeholder="t('role.filter.keywordPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRoles">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

      <el-table :data="filteredRows" stripe border>
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
      </el-table>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import type { RoleSummary } from '@/api/roles';
import { fetchRoles } from '@/api/roles';
import PageShell from '@/components/PageShell.vue';

const { t } = useI18n();

const loading = ref(false);
const errorMessage = ref('');
const keyword = ref('');
const rows = ref<RoleSummary[]>([]);

const filteredRows = computed(() =>
  rows.value.filter((item) => {
    const keywordValue = keyword.value.trim().toLowerCase();
    if (!keywordValue) {
      return true;
    }
    return (
      item.code.toLowerCase().includes(keywordValue) ||
      item.name.toLowerCase().includes(keywordValue) ||
      item.permissionCodes.some((code) => code.toLowerCase().includes(keywordValue))
    );
  }),
);

function resetFilters() {
  keyword.value = '';
}

async function loadRoles() {
  loading.value = true;
  errorMessage.value = '';
  try {
    rows.value = await fetchRoles({
      keyword: keyword.value || undefined,
    });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadRoles();
});
</script>

<style scoped>
.filter-form {
  margin-bottom: 12px;
}
</style>
