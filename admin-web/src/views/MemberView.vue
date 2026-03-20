<template>
  <PageShell :title="t('page.members.title')" :description="t('page.members.description')">
    <div v-loading="loading">
      <el-form :inline="true" class="filter-form">
        <el-form-item :label="t('member.filter.store')">
          <el-select v-model="storeId" clearable :placeholder="t('member.filter.storePlaceholder')">
            <el-option
              v-for="item in storeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('member.filter.countryCode')">
          <el-select v-model="countryCodeFilter" clearable :placeholder="t('member.filter.countryCodePlaceholder')">
            <el-option
              v-for="item in countryCodes"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('member.filter.levelCode')">
          <el-select v-model="levelCodeFilter" clearable :placeholder="t('member.filter.levelCodePlaceholder')">
            <el-option
              v-for="item in levelCodes"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('member.filter.keyword')">
          <el-input v-model="keyword" clearable :placeholder="t('member.filter.keywordPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadMembers">{{ t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

      <el-row :gutter="12" class="summary-row">
        <el-col :xs="12" :md="8">
          <el-card shadow="hover">
            <el-statistic :title="t('member.summary.total')" :value="filteredRows.length" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="8">
          <el-card shadow="hover">
            <el-statistic :title="t('member.summary.cnMembers')" :value="cnCount" />
          </el-card>
        </el-col>
        <el-col :xs="12" :md="8">
          <el-card shadow="hover">
            <el-statistic :title="t('member.summary.thMembers')" :value="thCount" />
          </el-card>
        </el-col>
      </el-row>

      <el-table :data="filteredRows" stripe border>
        <el-table-column prop="storeName" :label="t('member.columns.storeName')" min-width="180" />
        <el-table-column prop="displayName" :label="t('member.columns.displayName')" min-width="160" />
        <el-table-column prop="levelCode" :label="t('member.columns.levelCode')" min-width="120" />
        <el-table-column prop="countryCode" :label="t('member.columns.countryCode')" min-width="120" />
        <el-table-column prop="phoneNational" :label="t('member.columns.phoneNational')" min-width="150" />
        <el-table-column prop="phoneE164" :label="t('member.columns.phoneE164')" min-width="180" />
      </el-table>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import type { MemberSummary } from '@/api/members';
import { fetchMembers } from '@/api/members';
import type { StoreSummary } from '@/api/stores';
import { fetchStores } from '@/api/stores';
import PageShell from '@/components/PageShell.vue';

const { t } = useI18n();

const loading = ref(false);
const errorMessage = ref('');
const storeId = ref<number | undefined>();
const countryCodeFilter = ref('');
const levelCodeFilter = ref('');
const keyword = ref('');
const rows = ref<MemberSummary[]>([]);
const storeOptions = ref<StoreSummary[]>([]);

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

const filteredRows = computed(() =>
  rows.value
    .map((item) => ({
      ...item,
      storeName: item.storeName || storeNameMap.value[item.storeId] || '',
    }))
    .filter((item) => {
      const matchCountry = !countryCodeFilter.value || item.countryCode === countryCodeFilter.value;
      const matchLevel = !levelCodeFilter.value || item.levelCode === levelCodeFilter.value;
      const keywordValue = keyword.value.trim().toLowerCase();
      const matchKeyword =
        !keywordValue ||
        item.displayName.toLowerCase().includes(keywordValue) ||
        item.phoneNational.toLowerCase().includes(keywordValue) ||
        item.phoneE164.toLowerCase().includes(keywordValue) ||
        item.storeName.toLowerCase().includes(keywordValue);
      return matchCountry && matchLevel && matchKeyword;
    }),
);

const cnCount = computed(() =>
  filteredRows.value.filter((item) => item.countryCode.toUpperCase() === '+86').length,
);
const thCount = computed(() =>
  filteredRows.value.filter((item) => item.countryCode.toUpperCase() === '+66').length,
);

function resetFilters() {
  storeId.value = undefined;
  countryCodeFilter.value = '';
  levelCodeFilter.value = '';
  keyword.value = '';
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
      storeId: storeId.value,
      countryCode: countryCodeFilter.value || undefined,
      levelCode: levelCodeFilter.value || undefined,
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
  await loadMembers();
});
</script>

<style scoped>
.filter-form {
  margin-bottom: 12px;
}

.summary-row {
  margin-bottom: 12px;
}
</style>
