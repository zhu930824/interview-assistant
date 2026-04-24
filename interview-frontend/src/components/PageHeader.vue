<script setup lang="ts">
defineProps<{
  title: string;
  subtitle: string;
}>();
</script>

<template>
  <div class="page-head">
    <p class="eyebrow">智能招聘工作台</p>
    <h2>{{ title }}</h2>
    <p class="muted">{{ subtitle }}</p>
  </div>
</template>
