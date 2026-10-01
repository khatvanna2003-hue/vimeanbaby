<script setup lang="ts">
const { t } = useI18n()

useHead({ title: () => `${t('nav.orders')} | Vimean Baby Admin` })

const steps = ['pending', 'confirmed', 'shipping', 'delivered'] as const
</script>

<template>
  <div>
    <PageHeader :title="t('nav.orders')" :description="t('orders.subtitle')" />

    <div class="card p-6 sm:p-10">
      <EmptyState icon="orders" :title="t('orders.comingTitle')" :description="t('orders.comingBody')">
        <NuxtLink to="/customers" class="btn-secondary">{{ t('nav.customers') }}</NuxtLink>
      </EmptyState>

      <ol class="mx-auto mt-8 grid max-w-2xl gap-3 sm:grid-cols-4">
        <li v-for="(step, index) in steps" :key="step" class="rounded-xl border border-line/80 bg-surface/60 p-4 text-center">
          <span class="mx-auto flex h-8 w-8 items-center justify-center rounded-full bg-white text-sm font-bold text-ink-muted shadow-sm">{{ index + 1 }}</span>
          <p class="mt-2 text-sm font-semibold text-ink">{{ t(`orders.steps.${step}`) }}</p>
        </li>
      </ol>
    </div>
  </div>
</template>
