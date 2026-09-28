<script setup lang="ts">
import type { Category, PageResponse, ProductSummary } from '~/types/product'

const { t } = useI18n()
const localePath = useLocalePath()
const { apiFetch } = useApi()

const { data: categories } = await useAsyncData('home-categories', () =>
  apiFetch<Category[]>('/public/categories'),
)

const { data: featuredPage, pending: featuredPending, error: featuredError } = await useAsyncData('home-featured', () =>
  apiFetch<PageResponse<ProductSummary>>('/public/products', {
    query: { page: 0, size: 5, sort: 'featured' },
  }),
)

const HOME_PRODUCTS_PAGE_SIZE = 25

const { data: productsPages, pending: productsPending, error: productsError } = await useAsyncData('home-products', () =>
  Promise.all([0, 1].map(page =>
    apiFetch<PageResponse<ProductSummary>>('/public/products', {
      query: { page, size: HOME_PRODUCTS_PAGE_SIZE, sort: 'newest' },
    }),
  )),
)

const featured = computed(() => featuredPage.value?.content ?? [])
const products = computed(() => productsPages.value?.flatMap(p => p.content) ?? [])
const totalProducts = computed(() => productsPages.value?.[0]?.totalElements ?? 0)

useSeoMeta({
  title: () => `${t('app.name')} | ${t('app.tagline')}`,
  description: () => t('home.heroSubtitle'),
  ogImage: '/catalog/fallback.svg',
})
</script>

<template>
  <div>
    <HeroBanner />
    <CategoryStrip :categories="categories || []" />

    <section class="container-store section-pad">
      <div class="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h2 class="text-2xl font-bold text-ink sm:text-3xl">{{ t('home.topOffers') }}</h2>
          <p class="mt-1 text-sm text-muted">{{ t('home.topOffersSub') }}</p>
        </div>
        <NuxtLink :to="localePath('/products')" class="text-sm font-semibold text-brand">
          {{ t('home.showMore') }} →
        </NuxtLink>
      </div>
      <p v-if="featuredPending" class="py-10 text-center text-sm text-muted">{{ t('shop.loading') }}</p>
      <p v-else-if="featuredError" class="rounded-2xl bg-white px-4 py-10 text-center text-sm text-muted">{{ t('shop.error') }}</p>
      <ProductGrid v-else-if="featured.length" :products="featured" />
      <p v-else class="rounded-2xl bg-white px-4 py-10 text-center text-sm text-muted">
        {{ t('shop.empty') }}
      </p>
    </section>

    <section class="container-store px-4 pb-14 sm:px-6 lg:px-8">
      <div class="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h2 class="text-2xl font-bold text-ink sm:text-3xl">{{ t('home.products') }}</h2>
          <p class="mt-1 text-sm text-muted">{{ t('home.productsSub', { count: totalProducts }) }}</p>
        </div>
        <p class="text-xs font-semibold uppercase tracking-[0.16em] text-brand">{{ t('home.topPicks') }}</p>
      </div>
      <p v-if="productsPending" class="py-10 text-center text-sm text-muted">{{ t('shop.loading') }}</p>
      <p v-else-if="productsError" class="rounded-2xl bg-white px-4 py-10 text-center text-sm text-muted">{{ t('shop.error') }}</p>
      <template v-else-if="products.length">
        <ProductGrid :products="products" />
        <div class="mt-8 text-center">
          <NuxtLink :to="localePath('/products')" class="btn-primary">
            {{ t('home.viewAll') }}
          </NuxtLink>
        </div>
      </template>
      <p v-else class="rounded-2xl bg-white px-4 py-10 text-center text-sm text-muted">
        {{ t('shop.empty') }}
      </p>
    </section>

    <section class="container-store px-4 pb-14 sm:px-6 lg:px-8">
      <div class="relative min-h-[360px] overflow-hidden rounded-[1.75rem] bg-ink text-white shadow-soft sm:min-h-[420px]">
        <SafeImage
          src="https://res.cloudinary.com/vimeanbaby/image/upload/vimeanbaby/packshots/p-0013"
          alt=""
          img-class="absolute inset-0 h-full w-full object-cover"
          :width="1400"
        />
        <div class="absolute inset-0 bg-gradient-to-r from-ink/90 via-ink/65 to-ink/25" />

        <div class="relative z-10 grid h-full min-h-[360px] gap-8 p-8 sm:min-h-[420px] sm:p-12 lg:grid-cols-2 lg:items-center">
          <div>
            <h2 class="text-3xl font-bold">{{ t('home.storyTitle') }}</h2>
            <p class="mt-4 text-sm leading-relaxed text-white/80">{{ t('home.storyBody') }}</p>
            <div class="mt-8">
              <NuxtLink :to="localePath('/contact')" class="btn-primary bg-white text-brand hover:bg-cream">
                {{ t('home.storyCta') }}
              </NuxtLink>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3 lg:max-w-md lg:justify-self-end">
            <div class="rounded-2xl bg-white/95 p-4 text-ink shadow-soft backdrop-blur-sm">
              <p class="text-2xl font-bold text-brand">COD</p>
              <p class="mt-2 text-xs text-ink-muted">{{ t('home.trustCod') }}</p>
            </div>
            <div class="rounded-2xl bg-white/95 p-4 text-ink shadow-soft backdrop-blur-sm">
              <p class="text-2xl font-bold text-brand">100%</p>
              <p class="mt-2 text-xs text-ink-muted">{{ t('home.trustAuth') }}</p>
            </div>
            <div class="col-span-2 rounded-2xl bg-white/95 p-4 text-ink shadow-soft backdrop-blur-sm">
              <p class="text-sm font-semibold">{{ t('home.trustShip') }}</p>
              <p class="mt-1 text-xs text-ink-muted">Phnom Penh · Siem Reap · Provinces</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
