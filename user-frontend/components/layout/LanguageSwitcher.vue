<script setup lang="ts">
const { locale, locales, setLocale } = useI18n()

const available = computed(() =>
  (locales.value as Array<{ code: string, name: string }>).map(l => ({
    code: l.code,
    name: l.name,
  })),
)

function switchLocale(code: string) {
  setLocale(code)
}
</script>

<template>
  <div class="inline-flex overflow-hidden rounded-full border border-line bg-white text-xs">
    <button
      v-for="item in available"
      :key="item.code"
      type="button"
      class="min-h-9 px-3 font-semibold transition"
      :class="locale === item.code ? 'bg-brand text-white' : 'text-ink-muted hover:bg-cream'"
      @click="switchLocale(item.code)"
    >
      {{ item.code.toUpperCase() }}
    </button>
  </div>
</template>
