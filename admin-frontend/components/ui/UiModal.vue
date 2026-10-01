<script setup lang="ts">
const props = withDefaults(defineProps<{
  open: boolean
  title: string
  size?: 'sm' | 'md' | 'lg'
}>(), { size: 'md' })

const emit = defineEmits<{ close: [] }>()
const { t } = useI18n()

function onKey(event: KeyboardEvent) {
  if (event.key === 'Escape' && props.open) emit('close')
}
onMounted(() => window.addEventListener('keydown', onKey))
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition duration-150"
      enter-from-class="opacity-0"
      leave-active-class="transition duration-100"
      leave-to-class="opacity-0"
    >
      <div v-if="open" class="fixed inset-0 z-50 flex items-end justify-center p-0 sm:items-center sm:p-4">
        <button type="button" class="absolute inset-0 bg-ink/40" :aria-label="t('common.close')" @click="emit('close')" />
        <div
          role="dialog"
          aria-modal="true"
          :aria-label="title"
          class="relative flex max-h-[92vh] w-full flex-col rounded-t-2xl bg-white shadow-xl sm:rounded-2xl"
          :class="{ 'sm:max-w-md': size === 'sm', 'sm:max-w-xl': size === 'md', 'sm:max-w-3xl': size === 'lg' }"
        >
          <header class="flex items-center justify-between gap-4 border-b border-line/70 px-5 py-4">
            <h2 class="text-base font-bold text-ink">{{ title }}</h2>
            <button type="button" class="btn-icon" :aria-label="t('common.close')" @click="emit('close')">
              <AppIcon name="close" />
            </button>
          </header>
          <div class="overflow-y-auto px-5 py-5">
            <slot />
          </div>
          <footer v-if="$slots.footer" class="flex flex-wrap justify-end gap-2 border-t border-line/70 px-5 py-4">
            <slot name="footer" />
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
