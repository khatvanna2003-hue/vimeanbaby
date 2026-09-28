<script setup lang="ts">
import type { Category, PageResponse, ProductSummary } from '~/types/product'

const { t, locale } = useI18n()
const route = useRoute()
const { apiFetch } = useApi()

const slug = computed(() => String(route.params.slug))

const { data: categories } = await useAsyncData('all-categories', () =>
  apiFetch<Category[]>('/public/categories'),
)

const category = computed(() => categories.value?.find(c => c.slug === slug.value))

const { data, pending, error } = await useAsyncData(
  () => `category-products-${slug.value}`,
  () => apiFetch<PageResponse<ProductSummary>>('/public/products', {
    query: { category: slug.value, page: 0, size: 24, sort: 'featured' },
  }),
  { watch: [slug] },
)

const title = computed(() => {
  if (!category.value) return t('nav.categories')
  return locale.value === 'km' ? category.value.nameKm : category.value.nameEn
})

useSeoMeta({
  title: () => `${title.value} | ${t('app.name')}`,
  description: () => t('home.topOffersSub'),
  ogImage: () => category.value?.imageUrl || undefined,
})
</script>

<template>
  <div class="container-store section-pad">
    <h1 class="mb-2 text-3xl font-bold">{{ title }}</h1>
    <p class="mb-8 text-sm text-muted">{{ t('shop.count', { count: data?.totalElements ?? 0 }) }}</p>

    <p v-if="pending" class="py-16 text-center text-sm text-muted">{{ t('shop.loading') }}</p>
    <p v-else-if="error" class="py-16 text-center text-sm text-muted">{{ t('shop.error') }}</p>
    <ProductGrid v-else-if="data?.content?.length" :products="data.content" />
    <p v-else class="rounded-2xl bg-white px-4 py-16 text-center text-sm text-muted">
      {{ t('shop.empty') }}
    </p>
  </div>
</template>
