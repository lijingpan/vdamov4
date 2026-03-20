<template>
  <PageShell :title="t('page.devices.title')" :description="t('page.devices.description')">
    <el-empty description="Device module skeleton ready." />
  </PageShell>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import PageShell from '@/components/PageShell.vue';

const { t } = useI18n();
</script>
