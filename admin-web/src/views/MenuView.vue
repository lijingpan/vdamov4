<template>
  <PageShell :title="t('page.menus.title')" :description="t('page.menus.description')">
    <div v-loading="loading">
      <el-form :inline="true" class="filter-form">
        <el-form-item :label="t('menuManage.filter.keyword')">
          <el-input v-model="keyword" clearable :placeholder="t('menuManage.filter.keywordPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadMenus">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

      <el-table :data="filteredRows" stripe border>
        <el-table-column prop="name" :label="t('menuManage.columns.name')" min-width="180" />
        <el-table-column prop="parentName" :label="t('menuManage.columns.parentName')" min-width="180" />
        <el-table-column prop="route" :label="t('menuManage.columns.route')" min-width="180" />
        <el-table-column prop="permissionCode" :label="t('menuManage.columns.permissionCode')" min-width="220" />
        <el-table-column prop="sortOrder" :label="t('menuManage.columns.sortOrder')" min-width="100" />
      </el-table>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import type { MenuSummary } from '@/api/menus';
import { fetchMenus } from '@/api/menus';
import PageShell from '@/components/PageShell.vue';

const { t } = useI18n();

const loading = ref(false);
const errorMessage = ref('');
const keyword = ref('');
const rows = ref<MenuSummary[]>([]);

const parentNameMap = computed(() =>
  rows.value.reduce<Record<number, string>>((result, item) => {
    result[item.id] = item.name;
    return result;
  }, {}),
);

const filteredRows = computed(() =>
  rows.value
    .map((item) => ({
      ...item,
      parentName: item.parentName || (item.parentId ? parentNameMap.value[item.parentId] || '-' : '-'),
    }))
    .filter((item) => {
      const keywordValue = keyword.value.trim().toLowerCase();
      if (!keywordValue) {
        return true;
      }
      return (
        item.name.toLowerCase().includes(keywordValue) ||
        item.parentName.toLowerCase().includes(keywordValue) ||
        item.route.toLowerCase().includes(keywordValue) ||
        item.permissionCode.toLowerCase().includes(keywordValue)
      );
    }),
);

function resetFilters() {
  keyword.value = '';
}

async function loadMenus() {
  loading.value = true;
  errorMessage.value = '';
  try {
    rows.value = await fetchMenus({
      keyword: keyword.value || undefined,
    });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadMenus();
});
</script>

<style scoped>
.filter-form {
  margin-bottom: 12px;
}
</style>
