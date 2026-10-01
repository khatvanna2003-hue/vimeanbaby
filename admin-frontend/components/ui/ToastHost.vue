<script setup lang="ts">
const { toasts, dismiss } = useToast()
const { t } = useI18n()
</script>

<template>
  <div class="pointer-events-none fixed inset-x-0 bottom-4 z-[60] flex flex-col items-center gap-2 px-4 sm:inset-x-auto sm:right-4 sm:items-end" aria-live="polite">
    <TransitionGroup
      enter-active-class="transition duration-200"
      enter-from-class="translate-y-2 opacity-0"
      leave-active-class="transition duration-150"
      leave-to-class="opacity-0"
    >
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="pointer-events-auto flex w-full max-w-sm items-start gap-3 rounded-xl border bg-white px-4 py-3 text-sm text-ink shadow-lg"
        :class="{
          'border-mint': toast.variant === 'success',
          'border-danger/40': toast.variant === 'error',
          'border-line': toast.variant === 'info',
        }"
        :role="toast.variant === 'error' ? 'alert' : 'status'"
      >
        <span
          class="mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full"
          :class="{
            'bg-mint text-ink': toast.variant === 'success',
            'bg-danger text-white': toast.variant === 'error',
            'bg-surface text-ink-muted': toast.variant === 'info',
          }"
        >
          <AppIcon :name="toast.variant === 'success' ? 'check' : 'alert'" :size="12" />
        </span>
        <p class="flex-1 leading-relaxed">{{ toast.message }}</p>
        <button type="button" class="text-ink-muted hover:text-ink" :aria-label="t('common.close')" @click="dismiss(toast.id)">
          <AppIcon name="close" :size="16" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>
