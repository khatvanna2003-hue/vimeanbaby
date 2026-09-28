<script setup lang="ts">
defineProps<{
  open: boolean
  links: Array<{ to: string, label: string }>
}>()

defineEmits<{ close: [] }>()

const { t } = useI18n()
const localePath = useLocalePath()
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="fixed inset-0 z-50 lg:hidden">
      <button class="absolute inset-0 bg-ink/40" type="button" :aria-label="t('nav.home')" @click="$emit('close')" />
      <aside class="absolute inset-y-0 left-0 flex w-[86%] max-w-sm flex-col bg-white shadow-soft">
        <div class="flex items-center justify-between border-b border-line px-5 py-4">
          <img src="/logo.svg" :alt="t('app.name')" class="h-10 w-auto" width="166" height="40">
          <button type="button" class="h-10 w-10 rounded-full border border-line" @click="$emit('close')">
            ✕
          </button>
        </div>
        <nav class="flex-1 space-y-1 px-3 py-4">
          <NuxtLink
            v-for="link in links"
            :key="link.label"
            :to="link.to"
            class="block rounded-2xl px-4 py-3 text-sm font-semibold text-ink hover:bg-cream"
            @click="$emit('close')"
          >
            {{ link.label }}
          </NuxtLink>
          <NuxtLink
            :to="localePath('/login')"
            class="block rounded-2xl px-4 py-3 text-sm font-semibold text-ink hover:bg-cream"
            @click="$emit('close')"
          >
            {{ t('nav.account') }}
          </NuxtLink>
        </nav>
        <div class="border-t border-line px-5 py-4">
          <p class="rounded-full bg-cream px-3 py-2 text-center text-xs text-muted">
            {{ t('locale.cambodia') }}
          </p>
        </div>
      </aside>
    </div>
  </Teleport>
</template>
