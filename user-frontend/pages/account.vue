<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const { t } = useI18n()

useSeoMeta({
  robots: 'noindex, nofollow',
  description: () => t('account.metaDescription'),
})
</script>

<template>
  <div class="container-store px-4 py-8 sm:px-6 lg:px-8 lg:py-12">
    <div class="grid gap-6 lg:grid-cols-[280px_1fr] lg:gap-8">
      <aside class="lg:sticky lg:top-44 lg:self-start">
        <AccountNav />
      </aside>
      <div class="min-w-0">
        <NuxtPage />
      </div>
    </div>
  </div>
</template>
