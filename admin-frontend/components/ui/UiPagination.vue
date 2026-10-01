<script setup lang="ts">
const props = defineProps<{
  page: number
  totalPages: number
  totalElements: number
  size: number
}>()

const emit = defineEmits<{ change: [page: number] }>()
const { t } = useI18n()

const from = computed(() => (props.totalElements === 0 ? 0 : props.page * props.size + 1))
const to = computed(() => Math.min((props.page + 1) * props.size, props.totalElements))
</script>

<template>
  <div v-if="totalElements > 0" class="flex flex-wrap items-center justify-between gap-3 border-t border-line/70 px-4 py-3 text-sm text-ink-muted">
    <p>{{ t('common.showing', { from, to, total: totalElements }) }}</p>
    <div class="flex items-center gap-2">
      <button
        type="button"
        class="btn-secondary min-h-9 px-3"
        :disabled="page <= 0"
        :aria-label="t('common.previous')"
        @click="emit('change', page - 1)"
      >
        <AppIcon name="chevronLeft" :size="16" />
      </button>
      <span class="min-w-16 text-center font-semibold text-ink">{{ page + 1 }} / {{ Math.max(totalPages, 1) }}</span>
      <button
        type="button"
        class="btn-secondary min-h-9 px-3"
        :disabled="page + 1 >= totalPages"
        :aria-label="t('common.next')"
        @click="emit('change', page + 1)"
      >
        <AppIcon name="chevronRight" :size="16" />
      </button>
    </div>
  </div>
</template>
