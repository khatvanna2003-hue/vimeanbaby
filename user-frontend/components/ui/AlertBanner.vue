<script setup lang="ts">
withDefaults(defineProps<{
  variant?: 'error' | 'success' | 'info'
  message: string
}>(), {
  variant: 'info',
})
</script>

<template>
  <div
    class="flex items-start gap-3 rounded-2xl border px-4 py-3 text-sm text-ink"
    :class="{
      'border-danger/30 bg-danger/5': variant === 'error',
      'border-mint bg-mint-tint': variant === 'success',
      'border-line bg-cream': variant === 'info',
    }"
    :role="variant === 'error' ? 'alert' : 'status'"
  >
    <svg v-if="variant === 'error'" class="mt-0.5 h-5 w-5 shrink-0 text-danger" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
      <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m9-.75a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9 3.75h.008v.008H12v-.008Z" />
    </svg>
    <svg v-else-if="variant === 'success'" class="mt-0.5 h-5 w-5 shrink-0 text-ink" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
      <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
    </svg>
    <svg v-else class="mt-0.5 h-5 w-5 shrink-0 text-muted" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
      <path stroke-linecap="round" stroke-linejoin="round" d="m11.25 11.25.041-.02a.75.75 0 0 1 1.063.852l-.708 2.836a.75.75 0 0 0 1.063.853l.041-.021M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9-3.75h.008v.008H12V8.25Z" />
    </svg>
    <p class="leading-relaxed">{{ message }}</p>
  </div>
</template>
