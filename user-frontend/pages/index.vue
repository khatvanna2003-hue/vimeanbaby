<script setup lang="ts">
import type { Category, PageResponse, ProductSummary } from '~/types/product'

const { t } = useI18n()
const localePath = useLocalePath()
const { apiFetch } = useApi()

const { data: categories } = await useAsyncData('home-categories', () =>
  apiFetch<Category[]>('/public/categories'),
)

const { data: featuredPage } = await useAsyncData('home-featured', () =>
  apiFetch<PageResponse<ProductSummary>>('/public/products', {
    query: { page: 0, size: 10, sort: 'featured' },
  }),
)

const featured = computed(() => featuredPage.value?.content ?? [])

useSeoMeta({
  title: () => `${t('app.name')} | ${t('app.tagline')}`,
  description: () => t('home.heroSubtitle'),
  ogImage: '/catalog/fallback.svg',
})
</script>

<template>
  <div>
    <HeroBanner />
    <TrustStrip />
    <CategoryStrip :categories="categories || []" />

    <section class="container-store section-pad">
      <div class="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h2 class="text-2xl font-bold text-ink sm:text-3xl">{{ t('home.topOffers') }}</h2>
          <p class="mt-1 text-sm text-muted">{{ t('home.topOffersSub') }}</p>
        </div>
        <NuxtLink :to="localePath('/products')" class="text-sm font-semibold text-brand">
          {{ t('home.viewAll') }} →
        </NuxtLink>
      </div>
      <ProductGrid v-if="featured.length" :products="featured" />
      <p v-else class="rounded-2xl bg-white px-4 py-10 text-center text-sm text-muted">
        {{ t('shop.empty') }}
      </p>
    </section>

    <section class="container-store px-4 pb-14 sm:px-6 lg:px-8">
      <div class="grid overflow-hidden rounded-[1.75rem] bg-ink text-white shadow-soft lg:grid-cols-2">
        <div class="flex flex-col justify-center p-8 sm:p-12">
          <h2 class="text-3xl font-bold">{{ t('home.storyTitle') }}</h2>
          <p class="mt-4 text-sm leading-relaxed text-white/75">{{ t('home.storyBody') }}</p>
          <div class="mt-8">
            <NuxtLink :to="localePath('/contact')" class="btn-primary bg-white text-brand hover:bg-cream">
              {{ t('home.storyCta') }}
            </NuxtLink>
          </div>
        </div>
        <div class="min-h-[240px] bg-gradient-to-br from-mint-tint via-brand-tint to-blush-tint p-8">
          <div class="grid h-full grid-cols-2 gap-3">
            <div class="rounded-2xl bg-white/80 p-4 text-ink shadow-soft">
              <p class="text-2xl font-bold text-brand">COD</p>
              <p class="mt-2 text-xs text-ink-muted">{{ t('home.trustCod') }}</p>
            </div>
            <div class="rounded-2xl bg-white/80 p-4 text-ink shadow-soft">
              <p class="text-2xl font-bold text-brand">100%</p>
              <p class="mt-2 text-xs text-ink-muted">{{ t('home.trustAuth') }}</p>
            </div>
            <div class="col-span-2 rounded-2xl bg-white/90 p-4 text-ink shadow-soft">
              <p class="text-sm font-semibold">{{ t('home.trustShip') }}</p>
              <p class="mt-1 text-xs text-ink-muted">Phnom Penh · Siem Reap · Provinces</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
