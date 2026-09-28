<script setup lang="ts">
import type { Category } from '~/types/product'

const { t, locale } = useI18n()
const localePath = useLocalePath()
const { apiFetch } = useApi()

const { data: categories, pending, error } = await useAsyncData('categories-index', () =>
  apiFetch<Category[]>('/public/categories'),
)

useSeoMeta({
  title: () => `${t('nav.categories')} | ${t('app.name')}`,
  description: () => t('home.topOffersSub'),
})
</script>

<template>
  <div class="container-store section-pad">
    <p class="text-xs text-muted">{{ t('nav.home') }} / {{ t('nav.categories') }}</p>
    <h1 class="mt-2 text-3xl font-bold">{{ t('nav.categories') }}</h1>
    <p class="mt-2 text-sm text-muted">{{ t('home.categoriesSub') }}</p>

    <p v-if="pending" class="py-16 text-center text-sm text-muted">{{ t('shop.loading') }}</p>
    <p v-else-if="error" class="py-16 text-center text-sm text-muted">{{ t('shop.error') }}</p>
    <div v-else class="mt-8 grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-6">
      <NuxtLink
        v-for="category in categories || []"
        :key="category.id"
        :to="localePath(`/categories/${category.slug}`)"
        class="group rounded-[1.5rem] border border-line bg-white p-4 text-center shadow-soft transition hover:-translate-y-1"
      >
        <SafeImage
          :src="category.imageUrl"
          :alt="locale === 'km' ? category.nameKm : category.nameEn"
          img-class="aspect-square w-full rounded-2xl object-cover"
        />
        <p class="mt-3 text-sm font-semibold">
          {{ locale === 'km' ? category.nameKm : category.nameEn }}
        </p>
        <p class="mt-1 text-xs text-brand">{{ t('home.shopNow') }} →</p>
      </NuxtLink>
    </div>
  </div>
</template>
