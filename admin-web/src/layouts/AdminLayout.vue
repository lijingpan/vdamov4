<template>
  <el-container class="layout-root">
    <el-header class="layout-header">
      <div class="layout-header__brand">
        <div class="layout-header__badge">VD</div>
        <div class="layout-header__brand-copy">
          <h1>{{ t('app.title') }}</h1>
          <p>{{ t('app.subtitle') }}</p>
        </div>
      </div>
 
      <div class="header-action">
        <span class="header-user">{{ currentUserName }}</span>
        <div class="header-action__group">
          <span>{{ t('header.language') }}</span>
          <el-select v-model="selectedLocale" class="lang-select" @change="changeLocale">
            <el-option :label="t('locale.zhCN')" value="zh-CN" />
            <el-option :label="t('locale.enUS')" value="en-US" />
            <el-option :label="t('locale.thTH')" value="th-TH" />
          </el-select>
          <span class="header-divider" />
          <el-button link class="layout-logout" @click="handleLogout">{{ t('header.logout') }}</el-button>
        </div>
      </div>
    </el-header>
    <el-container class="layout-body">
      <el-aside width="248px" class="layout-sidebar">
        <div class="sidebar-panel">
          <p class="sidebar-panel__subtitle">{{ t('app.subtitle') }}</p>
          <p class="sidebar-panel__title">{{ t('app.title') }}</p>
        </div>
 
        <el-menu :default-active="activeMenu" class="side-menu" router>
          <el-menu-item v-for="item in visibleMenuItems" :key="item.key" :index="item.path" class="side-menu__item">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ t(item.i18nKey) }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="layout-main">
        <div class="layout-main__inner">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';
import { useRouter } from 'vue-router';
import { menuItems } from '@/router/menu';
import { useAuthStore } from '@/stores/auth';
import { useLocaleStore, type AppLocale } from '@/stores/locale';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const { locale, t } = useI18n();
const localeStore = useLocaleStore();

locale.value = localeStore.locale;

const selectedLocale = ref<AppLocale>(localeStore.locale);
const activeMenu = computed(() => route.path);
const visibleMenuItems = computed(() => {
  if (authStore.isSuperAdmin) {
    return menuItems;
  }
  const allowed = new Set(authStore.allowedRoutes);
  return menuItems.filter((item) => allowed.has(item.path));
});
const currentUserName = computed(() => authStore.user?.displayName ?? authStore.user?.username ?? '');

watch(
  () => localeStore.locale,
  (value) => {
    locale.value = value;
    selectedLocale.value = value;
  },
);

function changeLocale(value: AppLocale) {
  localeStore.setLocale(value);
}

async function handleLogout() {
  await authStore.logout();
  await router.replace('/login');
}
</script>
