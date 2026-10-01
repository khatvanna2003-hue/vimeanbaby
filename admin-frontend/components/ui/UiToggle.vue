<script setup lang="ts">
defineProps<{ label: string, description?: string }>()
const model = defineModel<boolean>({ default: false })
const id = useId()
</script>

<template>
  <div class="flex items-start justify-between gap-4">
    <label :for="id" class="cursor-pointer">
      <span class="block text-sm font-semibold text-ink">{{ label }}</span>
      <span v-if="description" class="block text-xs text-ink-muted">{{ description }}</span>
    </label>
    <button
      :id="id"
      type="button"
      role="switch"
      :aria-checked="model"
      class="relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition"
      :class="model ? 'bg-brand' : 'bg-line'"
      @click="model = !model"
    >
      <span class="inline-block h-5 w-5 rounded-full bg-white shadow transition" :class="model ? 'translate-x-5' : 'translate-x-0.5'" />
    </button>
  </div>
</template>
