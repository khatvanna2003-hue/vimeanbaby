<script setup lang="ts">
const props = defineProps<{ password: string }>()
const { t } = useI18n()

const score = computed(() => passwordScore(props.password))
const labels = computed(() => ['', t('auth.strength.weak'), t('auth.strength.fair'), t('auth.strength.good'), t('auth.strength.strong')])
const barClass = computed(() => ['', 'bg-danger', 'bg-warn', 'bg-mint', 'bg-mint'][score.value])

const rules = computed(() => [
  { ok: props.password.length >= 8, label: t('auth.rules.length') },
  { ok: /[A-Za-z]/.test(props.password), label: t('auth.rules.letter') },
  { ok: /\d/.test(props.password), label: t('auth.rules.number') },
])
</script>

<template>
  <div v-if="password" class="space-y-2" aria-live="polite">
    <div class="flex items-center gap-3">
      <div class="grid flex-1 grid-cols-4 gap-1.5">
        <span
          v-for="i in 4"
          :key="i"
          class="h-1.5 rounded-full transition-colors"
          :class="i <= score ? barClass : 'bg-line'"
        />
      </div>
      <span class="w-16 text-right text-xs font-semibold text-muted">{{ labels[score] }}</span>
    </div>
    <ul class="flex flex-wrap gap-x-4 gap-y-1 text-xs">
      <li v-for="rule in rules" :key="rule.label" class="inline-flex items-center gap-1.5" :class="rule.ok ? 'text-ink' : 'text-muted'">
        <span class="inline-flex h-4 w-4 items-center justify-center rounded-full text-[10px]" :class="rule.ok ? 'bg-mint text-ink' : 'bg-line text-muted'">
          {{ rule.ok ? '✓' : '•' }}
        </span>
        {{ rule.label }}
      </li>
    </ul>
  </div>
</template>
