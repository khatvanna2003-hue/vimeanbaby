<script setup lang="ts">
import type { Category, PageResponse, ProductSummary } from '~/types/product'

const { t } = useI18n()
const route = useRoute()
const localePath = useLocalePath()
const { apiFetch } = useApi()

const page = computed(() => Number(route.query.page || 0))
const q = computed(() => String(route.query.q || ''))
const category = computed(() => String(route.query.category || ''))
const sort = computed(() => String(route.query.sort || 'featured'))

const { data: categories } = await useAsyncData('shop-categories', () =>
  apiFetch<Category[]>('/public/categories'),
)

const { data, pending, error, refresh } = await useAsyncData(
  'shop-products',
  () => apiFetch<PageResponse<ProductSummary>>('/public/products', {
    query: {
      page: page.value,
      size: 20,
      q: q.value || undefined,
      category: category.value || undefined,
      sort: sort.value,
    },
  }),
  { watch: [page, q, category, sort] },
)

watch(() => route.fullPath, () => refresh())

function updateQuery(patch: Record<string, string | number | undefined>) {
  navigateTo({
    path: localePath('/products'),
    query: {
      ...route.query,
      ...Object.fromEntries(
        Object.entries(patch).filter(([, v]) => v !== undefined && v !== ''),
      ),
    },
  })
}

useSeoMeta({
  title: () => `${t('shop.title')} | ${t('app.name')}`,
  description: () => t('home.topOffersSub'),
})
</script>

<template>
  <div class="container-store section-pad">
    <div class="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="text-xs text-muted">{{ t('nav.home') }} / {{ t('shop.title') }}</p>
        <h1 class="text-3xl font-bold">{{ t('shop.allProducts') }}</h1>
        <p class="mt-1 text-sm text-muted">
          {{ t('shop.count', { count: data?.totalElements ?? 0 }) }}
        </p>
      </div>
      <div class="flex flex-wrap gap-2">
        <select
          class="min-h-11 rounded-2xl border border-line bg-white px-3 text-sm"
          :value="category"
          @change="updateQuery({ category: ($event.target as HTMLSelectElement).value, page: 0 })"
        >
          <option value="">{{ t('nav.categories') }}</option>
          <option v-for="c in categories || []" :key="c.id" :value="c.slug">
            {{ c.nameEn }}
          </option>
        </select>
        <select
          class="min-h-11 rounded-2xl border border-line bg-white px-3 text-sm"
          :value="sort"
          @change="updateQuery({ sort: ($event.target as HTMLSelectElement).value, page: 0 })"
        >
          <option value="featured">{{ t('shop.sortFeatured') }}</option>
          <option value="newest">{{ t('shop.sortNewest') }}</option>
          <option value="nameAsc">{{ t('shop.sortName') }}</option>
        </select>
      </div>
    </div>

    <p v-if="pending" class="py-16 text-center text-sm text-muted">{{ t('shop.loading') }}</p>
    <p v-else-if="error" class="py-16 text-center text-sm text-muted">{{ t('shop.error') }}</p>
    <template v-else>
      <ProductGrid v-if="data?.content?.length" :products="data.content" />
      <p v-else class="rounded-2xl bg-white px-4 py-16 text-center text-sm text-muted">
        {{ t('shop.empty') }}
      </p>

      <div v-if="(data?.totalPages || 0) > 1" class="mt-8 flex items-center justify-center gap-3">
        <button
          type="button"
          class="btn-ghost"
          :disabled="page <= 0"
          @click="updateQuery({ page: page - 1 })"
        >
          {{ t('common.previous') }}
        </button>
        <span class="text-sm text-muted">{{ page + 1 }} / {{ data?.totalPages }}</span>
        <button
          type="button"
          class="btn-ghost"
          :disabled="page + 1 >= (data?.totalPages || 1)"
          @click="updateQuery({ page: page + 1 })"
        >
          {{ t('common.next') }}
        </button>
      </div>
    </template>
  </div>
</template>
