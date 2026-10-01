<script setup lang="ts">
const { locale, locales, setLocale } = useI18n()
const codes = computed(() => (locales.value as Array<{ code: 'km' | 'en' }>).map(l => l.code))
</script>

<template>
  <div class="inline-flex overflow-hidden rounded-lg border border-line bg-white text-xs">
    <button
      v-for="code in codes"
      :key="code"
      type="button"
      class="min-h-8 px-2.5 font-semibold transition"
      :class="locale === code ? 'bg-brand text-white' : 'text-ink-muted hover:bg-surface'"
      @click="setLocale(code)"
    >
      {{ code.toUpperCase() }}
    </button>
  </div>
</template>
