<template>
  <section class="page-shell">
    <header class="page-shell__header">
      <h2>{{ title }}</h2>
      <p>{{ description }}</p>
    </header>
    <el-card shadow="never" class="page-shell__card">
      <slot />
    </el-card>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title: string;
  description: string;
}>();
</script>
