<template>
  <el-container class="layout-root">
    <el-aside width="240px" class="layout-sidebar">
      <div class="brand">
        <h1>{{ t('app.title') }}</h1>
        <p>{{ t('app.subtitle') }}</p>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="side-menu"
        router
        background-color="transparent"
        text-color="#cdd6e5"
        active-text-color="#67a0ff"
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.key"
          :index="item.path"
          class="side-menu__item"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ t(item.i18nKey) }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <div class="header-title">{{ t(`menu.${activeLabel}`) }}</div>
        <div class="header-action">
          <span>{{ t('header.language') }}</span>
          <el-select v-model="selectedLocale" class="lang-select" @change="changeLocale">
            <el-option :label="t('locale.zhCN')" value="zh-CN" />
            <el-option :label="t('locale.enUS')" value="en-US" />
            <el-option :label="t('locale.thTH')" value="th-TH" />
          </el-select>
        </div>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';
import { menuItems } from '@/router/menu';
import { useLocaleStore, type AppLocale } from '@/stores/locale';

const route = useRoute();
const { locale, t } = useI18n();
const localeStore = useLocaleStore();

locale.value = localeStore.locale;

const selectedLocale = ref<AppLocale>(localeStore.locale);
const activeMenu = computed(() => route.path);
const activeLabel = computed(() => {
  const target = menuItems.find((item) => item.path === route.path);
  return target ? target.key : 'dashboard';
});

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
</script>
