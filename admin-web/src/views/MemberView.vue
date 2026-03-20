<template>
  <PageShell :title="t('page.members.title')" :description="t('page.members.description')">
    <el-empty description="Member module skeleton ready." />
  </PageShell>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import PageShell from '@/components/PageShell.vue';

const { t } = useI18n();
</script>
