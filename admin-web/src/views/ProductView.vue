<template>
  <PageShell :title="t('page.products.title')" :description="t('page.products.description')">
    <el-empty description="Product module skeleton ready." />
  </PageShell>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import PageShell from '@/components/PageShell.vue';

const { t } = useI18n();
</script>
