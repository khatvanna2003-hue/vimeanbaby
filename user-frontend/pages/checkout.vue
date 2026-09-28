<script setup lang="ts">
const { t } = useI18n()
const localePath = useLocalePath()

useSeoMeta({
  title: () => `${t('checkout.title')} | ${t('app.name')}`,
})
</script>

<template>
  <div class="container-store section-pad">
    <div class="mx-auto max-w-lg rounded-[1.5rem] border border-line bg-white p-8 text-center shadow-soft">
      <h1 class="text-2xl font-bold">{{ t('checkout.title') }}</h1>
      <p class="mt-3 text-sm text-muted">{{ t('checkout.comingSoon') }}</p>
      <NuxtLink :to="localePath('/products')" class="btn-primary mt-6 inline-flex">
        {{ t('cart.continue') }}
      </NuxtLink>
    </div>
  </div>
</template>
