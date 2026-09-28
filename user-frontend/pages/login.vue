<script setup lang="ts">
const { t } = useI18n()
const localePath = useLocalePath()

useSeoMeta({
  title: () => `${t('nav.account')} | ${t('app.name')}`,
})
</script>

<template>
  <div class="container-store section-pad">
    <div class="mx-auto max-w-md rounded-[1.5rem] border border-line bg-white p-8 shadow-soft">
      <h1 class="mb-2 text-2xl font-bold">{{ t('nav.account') }}</h1>
      <p class="mb-6 text-sm text-muted">{{ t('account.comingSoon') }}</p>
      <div class="flex flex-wrap gap-3">
        <NuxtLink :to="localePath('/')" class="btn-primary inline-flex">{{ t('nav.home') }}</NuxtLink>
        <NuxtLink :to="localePath('/products')" class="btn-ghost inline-flex">{{ t('nav.shop') }}</NuxtLink>
      </div>
    </div>
  </div>
</template>
