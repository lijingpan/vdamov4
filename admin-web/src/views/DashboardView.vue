<template>
  <PageShell :title="t('page.dashboard.title')" :description="t('page.dashboard.description')">
    <div v-loading="loading">
      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />
      <el-row :gutter="16" class="dashboard-stats">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('dashboard.storeCount')" :value="summary.storeCount" />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('dashboard.productCount')" :value="summary.productCount" />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('dashboard.memberCount')" :value="summary.memberCount" />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('dashboard.deviceCount')" :value="summary.deviceCount" />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('dashboard.tableCount')" :value="summary.tableCount" />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('dashboard.activeOrderCount')" :value="summary.activeOrderCount" />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover">
            <el-statistic :title="t('dashboard.todayOrderCount')" :value="summary.todayOrderCount" />
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="table-status-card">
        <template #header>
          <div class="table-status-header">
            <span>{{ t('dashboard.tableStatusTitle') }}</span>
            <el-button text type="primary" @click="loadSummary">
              {{ t('common.refresh') }}
            </el-button>
          </div>
        </template>
        <div class="table-status-list">
          <div v-for="item in tableStatusList" :key="item.key" class="table-status-item">
            <span>{{ t(`dict.tableStatus.${item.key}`) }}</span>
            <strong>{{ item.count }}</strong>
          </div>
        </div>
      </el-card>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { fetchDashboardSummary } from '@/api/dashboard';
import PageShell from '@/components/PageShell.vue';

const { t } = useI18n();

const loading = ref(false);
const errorMessage = ref('');
const summary = reactive({
  storeCount: 0,
  productCount: 0,
  memberCount: 0,
  deviceCount: 0,
  tableCount: 0,
  activeOrderCount: 0,
  todayOrderCount: 0,
  tableStatusStats: {} as Record<string, number>,
});

const tableStatusList = computed(() => {
  const fallbackKeys = ['IDLE', 'IN_USE', 'WAITING_CHECKOUT', 'WAITING_CLEAN', 'DISABLED'];
  return fallbackKeys.map((key) => ({
    key,
    count: summary.tableStatusStats[key] ?? 0,
  }));
});

async function loadSummary() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const data = await fetchDashboardSummary();
    summary.storeCount = data.storeCount;
    summary.productCount = data.productCount;
    summary.memberCount = data.memberCount;
    summary.deviceCount = data.deviceCount;
    summary.tableCount = data.tableCount;
    summary.activeOrderCount = data.activeOrderCount;
    summary.todayOrderCount = data.todayOrderCount;
    summary.tableStatusStats = data.tableStatusStats;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadSummary();
});
</script>

<style scoped>
.dashboard-stats {
  margin-bottom: 16px;
}

.table-status-card {
  border: 1px solid #d8e2f0;
}

.table-status-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.table-status-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
}

.table-status-item {
  border: 1px solid #e4ebf5;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f9fbfe;
}

.table-status-item strong {
  color: #1f3f73;
  font-size: 18px;
}
</style>
