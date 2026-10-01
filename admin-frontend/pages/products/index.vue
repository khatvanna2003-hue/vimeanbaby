<script setup lang="ts">
import type { PageResponse, ProductSummary } from '~/types/admin'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const { apiFetch } = useApi()
const toast = useToast()
const { confirm } = useConfirm()
const errorMessage = useApiErrorMessage()
const { categories, brands } = useCatalogOptions()

useHead({ title: () => `${t('nav.products')} | Vimean Baby Admin` })

const PAGE_SIZE = 20
const query = computed(() => ({
  q: typeof route.query.q === 'string' ? route.query.q : '',
  categoryId: typeof route.query.categoryId === 'string' ? route.query.categoryId : '',
  brandId: typeof route.query.brandId === 'string' ? route.query.brandId : '',
  status: typeof route.query.status === 'string' ? route.query.status : '',
  page: Number(route.query.page ?? 0) || 0,
}))

const search = ref(query.value.q)
let searchTimer: ReturnType<typeof setTimeout> | undefined
watch(search, (value) => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => updateQuery({ q: value.trim() || undefined }), 350)
})

function updateQuery(patch: Record<string, string | number | undefined>, resetPage = true) {
  const next: Record<string, string> = {}
  const merged = { ...query.value, ...(resetPage ? { page: 0 } : {}), ...patch }
  for (const [key, value] of Object.entries(merged)) {
    if (value !== undefined && value !== '' && !(key === 'page' && value === 0)) next[key] = String(value)
  }
  router.replace({ query: next })
}

const { data, status, error, refresh } = await useAsyncData(
  'admin-products',
  () => apiFetch<PageResponse<ProductSummary>>('/admin/products', {
    query: {
      q: query.value.q || undefined,
      categoryId: query.value.categoryId || undefined,
      brandId: query.value.brandId || undefined,
      status: query.value.status || undefined,
      page: query.value.page,
      size: PAGE_SIZE,
    },
  }),
  { watch: [query] },
)

const hasFilters = computed(() => !!(query.value.q || query.value.categoryId || query.value.brandId || query.value.status))

function clearFilters() {
  search.value = ''
  router.replace({ query: {} })
}

function displayName(product: ProductSummary) {
  return locale.value === 'km' ? product.nameKm || product.nameEn : product.nameEn
}

async function deactivate(product: ProductSummary) {
  const ok = await confirm({
    title: t('products.deactivateTitle'),
    message: t('products.deactivateMessage', { name: product.nameEn }),
    confirmLabel: t('products.deactivate'),
    danger: true,
  })
  if (!ok) return
  try {
    await apiFetch(`/admin/products/${product.id}`, { method: 'DELETE' })
    toast.success(t('products.deactivated'))
    await refresh()
  }
  catch (err) {
    toast.error(errorMessage(err))
  }
}
</script>

<template>
  <div>
    <PageHeader :title="t('nav.products')" :description="t('products.subtitle', { count: formatNumber(data?.totalElements ?? 0) })">
      <template #actions>
        <NuxtLink to="/products/new" class="btn-primary">
          <AppIcon name="plus" :size="16" /> {{ t('products.add') }}
        </NuxtLink>
      </template>
    </PageHeader>

    <div class="card overflow-hidden">
      <div class="flex flex-col gap-3 border-b border-line/70 p-4 lg:flex-row lg:items-center">
        <label class="relative flex-1">
          <span class="sr-only">{{ t('common.search') }}</span>
          <AppIcon name="search" :size="18" class="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-ink-muted" />
          <input v-model="search" type="search" class="field-input pl-10" :placeholder="t('products.searchPlaceholder')">
        </label>
        <div class="grid grid-cols-1 gap-3 sm:grid-cols-3 lg:flex">
          <select class="field-input lg:w-44" :value="query.categoryId" :aria-label="t('products.category')" @change="updateQuery({ categoryId: ($event.target as HTMLSelectElement).value || undefined })">
            <option value="">{{ t('products.allCategories') }}</option>
            <option v-for="category in categories.data.value" :key="category.id" :value="String(category.id)">{{ category.nameEn }}</option>
          </select>
          <select class="field-input lg:w-40" :value="query.brandId" :aria-label="t('products.brand')" @change="updateQuery({ brandId: ($event.target as HTMLSelectElement).value || undefined })">
            <option value="">{{ t('products.allBrands') }}</option>
            <option v-for="brand in brands.data.value" :key="brand.id" :value="String(brand.id)">{{ brand.name }}</option>
          </select>
          <select class="field-input lg:w-36" :value="query.status" :aria-label="t('common.status')" @change="updateQuery({ status: ($event.target as HTMLSelectElement).value || undefined })">
            <option value="">{{ t('common.allStatuses') }}</option>
            <option value="active">{{ t('common.active') }}</option>
            <option value="inactive">{{ t('common.inactive') }}</option>
          </select>
        </div>
        <button v-if="hasFilters" type="button" class="btn-secondary shrink-0" @click="clearFilters">{{ t('common.clearFilters') }}</button>
      </div>

      <div v-if="error" class="p-6">
        <EmptyState icon="alert" :title="t('errors.loadTitle')" :description="errorMessage(error)">
          <button type="button" class="btn-primary" @click="refresh()">{{ t('common.retry') }}</button>
        </EmptyState>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full min-w-[860px]">
          <thead class="table-head">
            <tr>
              <th class="px-4 py-3">{{ t('products.product') }}</th>
              <th class="px-4 py-3">{{ t('products.category') }}</th>
              <th class="px-4 py-3 text-right">{{ t('products.price') }}</th>
              <th class="px-4 py-3 text-right">{{ t('products.stock') }}</th>
              <th class="px-4 py-3">{{ t('common.status') }}</th>
              <th class="px-4 py-3 text-right"><span class="sr-only">{{ t('common.actions') }}</span></th>
            </tr>
          </thead>
          <tbody v-if="status === 'pending' && !data" aria-busy="true">
            <tr v-for="i in 8" :key="i">
              <td colspan="6" class="table-cell"><div class="h-10 animate-pulse rounded-lg bg-surface" /></td>
            </tr>
          </tbody>
          <tbody v-else-if="data?.content.length" :class="status === 'pending' ? 'opacity-60' : ''">
            <tr v-for="product in data.content" :key="product.id" class="hover:bg-surface/60">
              <td class="table-cell">
                <div class="flex items-center gap-3">
                  <div class="flex h-12 w-12 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-line bg-white">
                    <img v-if="product.primaryImageUrl" :src="thumb(product.primaryImageUrl, 96)" alt="" class="h-full w-full object-contain" loading="lazy">
                    <AppIcon v-else name="photo" class="text-ink-muted" />
                  </div>
                  <div class="min-w-0">
                    <NuxtLink :to="`/products/${product.id}`" class="block max-w-xs truncate font-semibold text-ink hover:text-brand">{{ displayName(product) }}</NuxtLink>
                    <p class="truncate text-xs text-ink-muted">
                      {{ product.brandName || '—' }}
                      <span v-if="product.variantCount && product.variantCount > 1"> · {{ t('products.variantCount', { count: product.variantCount }) }}</span>
                    </p>
                  </div>
                  <AppIcon v-if="product.featured" name="star" :size="16" class="text-warn" :aria-label="t('products.featured')" />
                </div>
              </td>
              <td class="table-cell text-ink-muted">{{ product.categoryName || product.categorySlug || '—' }}</td>
              <td class="table-cell text-right">
                <span class="font-semibold">{{ formatUsd(product.price) }}</span>
                <span v-if="product.compareAtPrice" class="block text-xs text-ink-muted line-through">{{ formatUsd(product.compareAtPrice) }}</span>
              </td>
              <td class="table-cell text-right">
                <span :class="(product.totalStock ?? product.stockQty) <= 0 ? 'badge-danger' : (product.totalStock ?? product.stockQty) <= 5 ? 'badge-pending' : 'font-semibold'">
                  {{ formatNumber(product.totalStock ?? product.stockQty) }}
                </span>
              </td>
              <td class="table-cell">
                <span :class="product.active ? 'badge-success' : 'badge-neutral'">{{ product.active ? t('common.active') : t('common.inactive') }}</span>
              </td>
              <td class="table-cell">
                <div class="flex justify-end gap-1">
                  <NuxtLink :to="`/products/${product.id}`" class="btn-icon" :aria-label="t('common.edit')"><AppIcon name="edit" :size="18" /></NuxtLink>
                  <button v-if="product.active" type="button" class="btn-icon hover:text-danger" :aria-label="t('products.deactivate')" @click="deactivate(product)">
                    <AppIcon name="trash" :size="18" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
          <tbody v-else>
            <tr>
              <td colspan="6">
                <EmptyState icon="products" :title="hasFilters ? t('products.noResults') : t('products.empty')" :description="hasFilters ? t('common.tryOtherFilters') : t('products.emptyHint')">
                  <button v-if="hasFilters" type="button" class="btn-secondary" @click="clearFilters">{{ t('common.clearFilters') }}</button>
                  <NuxtLink v-else to="/products/new" class="btn-primary">{{ t('products.add') }}</NuxtLink>
                </EmptyState>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <UiPagination
        v-if="data"
        :page="data.page"
        :total-pages="data.totalPages"
        :total-elements="data.totalElements"
        :size="data.size"
        @change="updateQuery({ page: $event }, false)"
      />
    </div>
  </div>
</template>
