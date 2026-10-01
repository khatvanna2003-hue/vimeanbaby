<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <section class="rounded-[1.5rem] border border-line bg-white p-5 shadow-soft sm:p-8">
    <header class="flex flex-wrap items-start justify-between gap-4 border-b border-line pb-5">
      <div>
        <h2 class="text-lg font-bold text-ink sm:text-xl">{{ title }}</h2>
        <p v-if="description" class="mt-1 text-sm text-muted">{{ description }}</p>
      </div>
      <slot name="actions" />
    </header>
    <div class="pt-6">
      <slot />
    </div>
  </section>
</template>
