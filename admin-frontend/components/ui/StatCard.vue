<script setup lang="ts">
withDefaults(defineProps<{
  label: string
  value: string
  hint?: string
  icon: string
  tone?: 'brand' | 'mint' | 'warn' | 'danger' | 'neutral'
  to?: string
}>(), { hint: '', tone: 'brand', to: undefined })

const NuxtLink = resolveComponent('NuxtLink')
</script>

<template>
  <component
    :is="to ? NuxtLink : 'div'"
    :to="to"
    class="card flex items-start gap-4 p-5 transition"
    :class="to ? 'hover:-translate-y-0.5 hover:shadow-md' : ''"
  >
    <span
      class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl"
      :class="{
        'bg-brand-tint text-brand': tone === 'brand',
        'bg-mint-tint text-ink': tone === 'mint',
        'bg-warn/15 text-warn': tone === 'warn',
        'bg-danger/10 text-danger': tone === 'danger',
        'bg-surface text-ink-muted': tone === 'neutral',
      }"
    >
      <AppIcon :name="icon" :size="22" />
    </span>
    <div class="min-w-0">
      <p class="text-xs font-semibold uppercase tracking-wide text-ink-muted">{{ label }}</p>
      <p class="mt-1 truncate text-2xl font-bold text-ink">{{ value }}</p>
      <p v-if="hint" class="mt-0.5 text-xs text-ink-muted">{{ hint }}</p>
    </div>
  </component>
</template>
