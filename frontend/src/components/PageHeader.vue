<template>
  <div class="page-header">
    <div class="header-content">
      <div class="header-icon" v-if="icon">
        <el-icon :size="28"><component :is="icon" /></el-icon>
      </div>
      <div class="header-text">
        <h1 class="page-title">{{ title }}</h1>
        <p class="page-subtitle" v-if="subtitle">{{ subtitle }}</p>
      </div>
    </div>
    <div class="header-actions" v-if="$slots.default">
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({
  icon: { type: [Object, String], default: null },
  title: { type: String, required: true },
  subtitle: { type: String, default: '' }
})
</script>
