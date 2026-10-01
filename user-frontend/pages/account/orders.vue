<script setup lang="ts">
const { t } = useI18n()
const localePath = useLocalePath()

useSeoMeta({
  title: () => `${t('account.nav.orders')} | ${t('app.name')}`,
})
</script>

<template>
  <AccountSection :title="t('account.nav.orders')" :description="t('orders.subtitle')">
    <div class="flex flex-col items-center px-4 py-10 text-center">
      <span class="flex h-16 w-16 items-center justify-center rounded-full bg-brand-tint text-brand">
        <svg class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.6" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" d="M20.25 7.5l-.63 10.63a2.25 2.25 0 0 1-2.24 2.12H6.62a2.25 2.25 0 0 1-2.24-2.12L3.75 7.5M10 11.25h4M3.38 7.5h17.25c.62 0 1.12-.5 1.12-1.13v-1.5c0-.62-.5-1.12-1.12-1.12H3.38c-.63 0-1.13.5-1.13 1.12v1.5c0 .63.5 1.13 1.13 1.13Z" />
        </svg>
      </span>
      <h3 class="mt-5 text-lg font-bold text-ink">{{ t('orders.emptyTitle') }}</h3>
      <p class="mt-2 max-w-sm text-sm text-muted">{{ t('orders.emptyBody') }}</p>
      <NuxtLink :to="localePath('/products')" class="btn-primary mt-6">{{ t('home.shopNow') }}</NuxtLink>
    </div>
  </AccountSection>
</template>
