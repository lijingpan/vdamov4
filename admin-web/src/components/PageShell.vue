<template>
  <section class="page-shell">
    <header class="page-shell__hero">
      <div class="page-shell__intro">
        <p class="page-shell__eyebrow">{{ description }}</p>
        <h2>{{ title }}</h2>
      </div>
      <div v-if="$slots.actions" class="page-shell__actions">
        <slot name="actions" />
      </div>
    </header>
    <div v-if="$slots.meta" class="page-shell__meta">
      <div class="page-shell__meta-inner">
        <slot name="meta" />
      </div>
    </div>
    <div class="page-shell__content">
      <slot />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title: string;
  description: string;
}>();
</script>
