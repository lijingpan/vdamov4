<template>
  <PageShell :title="t('page.dashboard.title')" :description="t('page.dashboard.description')">
    <template #actions>
      <el-button :icon="RefreshRight" @click="loadSummary">{{ t('common.refresh') }}</el-button>
    </template>

    <template #meta>
      <div class="dashboard-metrics">
        <article
          v-for="item in metricCards"
          :key="item.key"
          class="dashboard-metric-card"
          :class="`dashboard-metric-card--${item.tone}`"
        >
          <div class="dashboard-metric-card__icon">
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
          </div>
          <div class="dashboard-metric-card__body">
            <span class="dashboard-metric-card__label">{{ t(item.labelKey) }}</span>
            <strong class="dashboard-metric-card__value">{{ item.value }}</strong>
          </div>
        </article>
      </div>
    </template>

    <div v-loading="loading" class="dashboard-page">
      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="false"
        class="dashboard-alert"
      />

      <div class="dashboard-panels">
        <el-card shadow="never" class="page-card dashboard-overview-card">
          <div class="action-bar">
            <div class="dashboard-section-copy">
              <div class="dashboard-section-copy__title">{{ t('page.dashboard.title') }}</div>
              <div class="action-bar__hint">{{ t('page.dashboard.description') }}</div>
            </div>
          </div>

          <div class="dashboard-overview">
            <div class="dashboard-overview__hero">
              <span class="dashboard-overview__label">{{ t('dashboard.todayOrderCount') }}</span>
              <strong class="dashboard-overview__value">{{ summary.todayOrderCount }}</strong>
              <div class="dashboard-overview__chips">
                <div class="dashboard-overview__chip">
                  <span>{{ t('dashboard.activeOrderCount') }}</span>
                  <strong>{{ summary.activeOrderCount }}</strong>
                </div>
                <div class="dashboard-overview__chip">
                  <span>{{ t('dashboard.tableCount') }}</span>
                  <strong>{{ summary.tableCount }}</strong>
                </div>
              </div>
            </div>

            <div class="dashboard-overview__aside">
              <div v-for="item in overviewCards" :key="item.key" class="dashboard-overview__aside-card">
                <span>{{ t(item.labelKey) }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="page-card table-status-card">
          <div class="table-status-header">
            <div class="dashboard-section-copy">
              <div class="dashboard-section-copy__title">{{ t('dashboard.tableStatusTitle') }}</div>
              <div class="action-bar__hint">{{ t('page.dashboard.description') }}</div>
            </div>
            <el-button text type="primary" @click="loadSummary">
              {{ t('common.refresh') }}
            </el-button>
          </div>

          <div class="table-status-list">
            <div
              v-for="item in tableStatusList"
              :key="item.key"
              class="table-status-item"
              :class="`table-status-item--${item.tone}`"
            >
              <div class="table-status-item__head">
                <span>{{ t(`dict.tableStatus.${item.key}`) }}</span>
                <strong>{{ item.count }}</strong>
              </div>
              <div class="table-status-item__bar">
                <span :style="{ width: `${item.percent}%` }"></span>
              </div>
              <small>{{ item.percent }}%</small>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import {
  Calendar,
  Goods,
  Grid,
  Monitor,
  OfficeBuilding,
  RefreshRight,
  ShoppingCart,
  User,
} from '@element-plus/icons-vue';
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

const metricCards = computed(() => [
  { key: 'storeCount', labelKey: 'dashboard.storeCount', value: summary.storeCount, icon: OfficeBuilding, tone: 'blue' },
  { key: 'productCount', labelKey: 'dashboard.productCount', value: summary.productCount, icon: Goods, tone: 'cyan' },
  { key: 'memberCount', labelKey: 'dashboard.memberCount', value: summary.memberCount, icon: User, tone: 'green' },
  { key: 'deviceCount', labelKey: 'dashboard.deviceCount', value: summary.deviceCount, icon: Monitor, tone: 'violet' },
  { key: 'tableCount', labelKey: 'dashboard.tableCount', value: summary.tableCount, icon: Grid, tone: 'slate' },
  {
    key: 'activeOrderCount',
    labelKey: 'dashboard.activeOrderCount',
    value: summary.activeOrderCount,
    icon: ShoppingCart,
    tone: 'amber',
  },
  {
    key: 'todayOrderCount',
    labelKey: 'dashboard.todayOrderCount',
    value: summary.todayOrderCount,
    icon: Calendar,
    tone: 'navy',
  },
]);

const overviewCards = computed(() => [
  { key: 'storeCount', labelKey: 'dashboard.storeCount', value: summary.storeCount },
  { key: 'productCount', labelKey: 'dashboard.productCount', value: summary.productCount },
  { key: 'memberCount', labelKey: 'dashboard.memberCount', value: summary.memberCount },
  { key: 'deviceCount', labelKey: 'dashboard.deviceCount', value: summary.deviceCount },
]);

const tableStatusList = computed(() => {
  const fallbackKeys = ['IDLE', 'IN_USE', 'WAITING_CHECKOUT', 'WAITING_CLEAN', 'DISABLED'];
  const totalFromStats = fallbackKeys.reduce((sum, key) => sum + (summary.tableStatusStats[key] ?? 0), 0);
  const total = Math.max(summary.tableCount, totalFromStats);
  const toneMap: Record<string, string> = {
    IDLE: 'green',
    IN_USE: 'blue',
    WAITING_CHECKOUT: 'amber',
    WAITING_CLEAN: 'cyan',
    DISABLED: 'slate',
  };
  return fallbackKeys.map((key) => ({
    key,
    count: summary.tableStatusStats[key] ?? 0,
    tone: toneMap[key] ?? 'slate',
    percent: total > 0 ? Math.round(((summary.tableStatusStats[key] ?? 0) / total) * 100) : 0,
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
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dashboard-alert {
  margin-bottom: 0;
}

.dashboard-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.dashboard-metric-card {
  --dashboard-card-glow: rgba(31, 95, 209, 0.12);
  --dashboard-card-accent: #1f5fd1;
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 122px;
  padding: 18px 20px;
  position: relative;
  overflow: hidden;
  border-radius: var(--app-radius-md);
  border: 1px solid var(--app-border);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(246, 249, 253, 0.98));
  box-shadow: 0 12px 28px rgba(17, 41, 86, 0.08);
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.dashboard-metric-card::before {
  content: '';
  position: absolute;
  inset: auto -12px -36px auto;
  width: 128px;
  height: 128px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--dashboard-card-glow), transparent 72%);
}

.dashboard-metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 32px rgba(17, 41, 86, 0.11);
}

.dashboard-metric-card__icon {
  position: relative;
  z-index: 1;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--dashboard-card-accent);
  background: color-mix(in srgb, var(--dashboard-card-accent) 12%, white);
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--dashboard-card-accent) 18%, white);
}

.dashboard-metric-card__icon :deep(svg) {
  width: 22px;
  height: 22px;
}

.dashboard-metric-card__body {
  position: relative;
  z-index: 1;
  min-width: 0;
}

.dashboard-metric-card__label {
  display: block;
  color: var(--app-text-soft);
  font-size: 13px;
  line-height: 1.5;
}

.dashboard-metric-card__value {
  display: block;
  margin-top: 8px;
  color: #193152;
  font-size: 30px;
  line-height: 1;
  font-weight: 700;
}

.dashboard-metric-card--blue {
  --dashboard-card-glow: rgba(61, 121, 242, 0.2);
  --dashboard-card-accent: #2f71dd;
}

.dashboard-metric-card--cyan {
  --dashboard-card-glow: rgba(28, 162, 180, 0.18);
  --dashboard-card-accent: #1a9ab0;
}

.dashboard-metric-card--green {
  --dashboard-card-glow: rgba(48, 155, 111, 0.18);
  --dashboard-card-accent: #2f976f;
}

.dashboard-metric-card--violet {
  --dashboard-card-glow: rgba(97, 104, 202, 0.16);
  --dashboard-card-accent: #5f69bd;
}

.dashboard-metric-card--slate {
  --dashboard-card-glow: rgba(95, 117, 145, 0.16);
  --dashboard-card-accent: #5f7591;
}

.dashboard-metric-card--amber {
  --dashboard-card-glow: rgba(214, 145, 34, 0.18);
  --dashboard-card-accent: #d18a1c;
}

.dashboard-metric-card--navy {
  --dashboard-card-glow: rgba(29, 78, 166, 0.2);
  --dashboard-card-accent: #1f5fd1;
}

.dashboard-panels {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.95fr);
  gap: 16px;
}

.dashboard-overview-card,
.table-status-card {
  border: 1px solid var(--app-border);
}

.dashboard-section-copy {
  min-width: 0;
}

.dashboard-section-copy__title {
  color: var(--app-text-strong);
  font-size: 18px;
  font-weight: 700;
}

.dashboard-overview {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(240px, 0.8fr);
  gap: 16px;
  margin-top: 18px;
}

.dashboard-overview__hero {
  position: relative;
  overflow: hidden;
  padding: 24px;
  border-radius: 20px;
  min-height: 248px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background:
    radial-gradient(circle at 88% 12%, rgba(175, 209, 255, 0.28), transparent 32%),
    linear-gradient(135deg, #123775 0%, #1b56b7 56%, #2f7adf 100%);
  box-shadow: 0 18px 34px rgba(20, 56, 119, 0.24);
}

.dashboard-overview__hero::after {
  content: '';
  position: absolute;
  right: -34px;
  bottom: -40px;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.2), transparent 70%);
}

.dashboard-overview__label,
.dashboard-overview__chip span {
  position: relative;
  z-index: 1;
  color: rgba(236, 243, 255, 0.84);
}

.dashboard-overview__value {
  position: relative;
  z-index: 1;
  display: block;
  margin-top: 10px;
  color: #fff;
  font-size: 56px;
  line-height: 1;
  font-weight: 700;
}

.dashboard-overview__chips {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.dashboard-overview__chip {
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
}

.dashboard-overview__chip strong {
  display: block;
  margin-top: 8px;
  color: #fff;
  font-size: 24px;
  line-height: 1;
}

.dashboard-overview__aside {
  display: grid;
  gap: 12px;
}

.dashboard-overview__aside-card {
  min-height: 86px;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid #dbe5f1;
  background: linear-gradient(180deg, #f9fbff, #f2f6fc);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.dashboard-overview__aside-card span {
  display: block;
  color: var(--app-text-soft);
  font-size: 13px;
}

.dashboard-overview__aside-card strong {
  display: block;
  margin-top: 12px;
  color: #1b3153;
  font-size: 28px;
  line-height: 1;
}

.table-status-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.table-status-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.table-status-item {
  --status-accent: #5f7591;
  padding: 16px;
  border-radius: 18px;
  border: 1px solid color-mix(in srgb, var(--status-accent) 20%, white);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(245, 248, 252, 0.98));
  box-shadow: 0 10px 22px rgba(17, 41, 86, 0.06);
}

.table-status-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.table-status-item__head span {
  color: #37506f;
  font-weight: 600;
}

.table-status-item__head strong {
  color: var(--status-accent);
  font-size: 24px;
  line-height: 1;
}

.table-status-item__bar {
  height: 8px;
  margin-top: 14px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--status-accent) 12%, white);
  overflow: hidden;
}

.table-status-item__bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--status-accent), color-mix(in srgb, var(--status-accent) 72%, white));
}

.table-status-item small {
  display: block;
  margin-top: 10px;
  color: var(--app-text-soft);
  font-size: 12px;
}

.table-status-item--green {
  --status-accent: #2d9b6f;
}

.table-status-item--blue {
  --status-accent: #2e74df;
}

.table-status-item--amber {
  --status-accent: #d18a1c;
}

.table-status-item--cyan {
  --status-accent: #1a9ab0;
}

.table-status-item--slate {
  --status-accent: #647b99;
}

@media (max-width: 1280px) {
  .dashboard-metrics {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .dashboard-panels {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .dashboard-metrics,
  .dashboard-overview,
  .table-status-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-overview__hero,
  .dashboard-overview__aside {
    grid-column: 1 / -1;
  }
}

@media (max-width: 640px) {
  .dashboard-metrics,
  .dashboard-overview__chips,
  .table-status-list {
    grid-template-columns: 1fr;
  }

  .table-status-header {
    flex-direction: column;
    align-items: stretch;
  }

  .dashboard-metric-card,
  .dashboard-overview__hero,
  .dashboard-overview__aside-card,
  .table-status-item {
    min-height: auto;
  }

  .dashboard-overview__value {
    font-size: 44px;
  }
}
</style>
