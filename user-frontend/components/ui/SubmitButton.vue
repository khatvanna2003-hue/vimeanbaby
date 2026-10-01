<script setup lang="ts">
withDefaults(defineProps<{
  loading?: boolean
  disabled?: boolean
  block?: boolean
  variant?: 'primary' | 'ghost'
}>(), {
  loading: false,
  disabled: false,
  block: false,
  variant: 'primary',
})
</script>

<template>
  <button
    type="submit"
    :class="[variant === 'primary' ? 'btn-primary' : 'btn-ghost', block ? 'w-full' : '', 'gap-2 disabled:cursor-not-allowed disabled:opacity-60']"
    :disabled="disabled || loading"
    :aria-busy="loading"
  >
    <svg v-if="loading" class="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" class="opacity-25" />
      <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor" stroke-width="3" stroke-linecap="round" />
    </svg>
    <slot />
  </button>
</template>
