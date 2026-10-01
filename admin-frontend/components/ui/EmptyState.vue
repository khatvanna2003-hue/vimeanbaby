<script setup lang="ts">
withDefaults(defineProps<{
  icon?: string
  title: string
  description?: string
}>(), { icon: 'search', description: '' })
</script>

<template>
  <div class="flex flex-col items-center px-6 py-14 text-center">
    <span class="flex h-14 w-14 items-center justify-center rounded-full bg-brand-tint text-brand">
      <AppIcon :name="icon" :size="26" />
    </span>
    <h3 class="mt-4 text-base font-bold text-ink">{{ title }}</h3>
    <p v-if="description" class="mt-1 max-w-sm text-sm text-ink-muted">{{ description }}</p>
    <div v-if="$slots.default" class="mt-5">
      <slot />
    </div>
  </div>
</template>
