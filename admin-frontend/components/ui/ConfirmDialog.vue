<script setup lang="ts">
const { state, settle } = useConfirm()
const { t } = useI18n()
</script>

<template>
  <UiModal :open="state.open" :title="state.title" size="sm" @close="settle(false)">
    <div class="flex gap-4">
      <span
        class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full"
        :class="state.danger ? 'bg-danger/10 text-danger' : 'bg-brand-tint text-brand'"
      >
        <AppIcon name="alert" />
      </span>
      <p class="pt-2 text-sm text-ink-muted">{{ state.message }}</p>
    </div>
    <template #footer>
      <button type="button" class="btn-secondary" @click="settle(false)">{{ t('common.cancel') }}</button>
      <button type="button" :class="state.danger ? 'btn-danger' : 'btn-primary'" @click="settle(true)">
        {{ state.confirmLabel || t('common.confirm') }}
      </button>
    </template>
  </UiModal>
</template>
