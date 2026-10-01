<script setup lang="ts">
import type { InventoryItem, PageResponse } from '~/types/admin'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const { apiFetch } = useApi()
const toast = useToast()
const errorMessage = useApiErrorMessage()

useHead({ title: () => `${t('nav.inventory')} | Vimean Baby Admin` })

const FILTERS = ['all', 'low', 'out', 'expiring'] as const
type Filter = typeof FILTERS[number]

const query = computed(() => {
  const filter = FILTERS.includes(route.query.filter as Filter) ? route.query.filter as Filter : 'all'
  return {
    filter,
    q: typeof route.query.q === 'string' ? route.query.q : '',
    page: Number(route.query.page ?? 0) || 0,
  }
})

function updateQuery(patch: Partial<{ filter: Filter, q: string, page: number }>) {
  const merged = { ...query.value, page: 0, ...patch }
  const next: Record<string, string> = {}
  if (merged.filter !== 'all') next.filter = merged.filter
  if (merged.q) next.q = merged.q
  if (merged.page) next.page = String(merged.page)
  router.replace({ query: next })
}

const search = ref(query.value.q)
let timer: ReturnType<typeof setTimeout> | undefined
watch(search, (value) => {
  clearTimeout(timer)
  timer = setTimeout(() => updateQuery({ q: value.trim() }), 350)
})

const { data, status, error, refresh } = await useAsyncData(
  'admin-inventory',
  () => apiFetch<PageResponse<InventoryItem>>('/admin/inventory', {
    query: { filter: query.value.filter, q: query.value.q || undefined, page: query.value.page, size: 25 },
  }),
  { watch: [query] },
)

const drafts = reactive<Record<number, number | null>>({})
const savingId = ref<number | null>(null)

watch(data, () => {
  for (const key of Object.keys(drafts)) delete drafts[Number(key)]
})

function draftValue(item: InventoryItem) {
  return drafts[item.variantId] ?? item.stockQty
}

function isDirty(item: InventoryItem) {
  const value = drafts[item.variantId]
  return value !== undefined && value !== null && value !== item.stockQty
}

function setDraft(item: InventoryItem, raw: string) {
  drafts[item.variantId] = raw === '' ? null : Math.max(0, Math.floor(Number(raw)))
}

async function saveStock(item: InventoryItem) {
  const value = drafts[item.variantId]
  if (value === undefined || value === null || Number.isNaN(value)) return
  savingId.value = item.variantId
  try {
    const updated = await apiFetch<InventoryItem>(`/admin/inventory/variants/${item.variantId}/stock`, {
      method: 'PATCH',
      body: { stockQty: value },
    })
    item.stockQty = updated.stockQty
    delete drafts[item.variantId]
    toast.success(t('inventory.stockSaved', { sku: item.sku }))
  }
  catch (err) {
    toast.error(errorMessage(err))
  }
  finally {
    savingId.value = null
  }
}

function stockBadge(item: InventoryItem) {
  if (item.stockQty <= 0) return 'badge-danger'
  if (item.stockQty <= 5) return 'badge-pending'
  return 'badge-success'
}

function expiryBadge(item: InventoryItem) {
  const days = daysUntil(item.expiryDate)
  if (days === null) return ''
  if (days < 0) return 'badge-danger'
  if (days <= 60) return 'badge-pending'
  return 'badge-neutral'
}
</script>

<template>
  <div>
    <PageHeader :title="t('nav.inventory')" :description="t('inventory.subtitle')" />

    <div class="card overflow-hidden">
      <div class="flex flex-col gap-3 border-b border-line/70 p-4 lg:flex-row lg:items-center lg:justify-between">
        <div class="flex gap-1 overflow-x-auto rounded-xl bg-surface p-1" role="tablist">
          <button
            v-for="filter in FILTERS"
            :key="filter"
            type="button"
            role="tab"
            :aria-selected="query.filter === filter"
            class="whitespace-nowrap rounded-lg px-3 py-1.5 text-sm font-semibold transition"
            :class="query.filter === filter ? 'bg-white text-ink shadow-sm' : 'text-ink-muted hover:text-ink'"
            @click="updateQuery({ filter })"
          >
            {{ t(`inventory.filters.${filter}`) }}
          </button>
        </div>
        <label class="relative lg:w-80">
          <span class="sr-only">{{ t('common.search') }}</span>
          <AppIcon name="search" :size="18" class="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-ink-muted" />
          <input v-model="search" type="search" class="field-input pl-10" :placeholder="t('inventory.searchPlaceholder')">
        </label>
      </div>

      <div v-if="error" class="p-6">
        <EmptyState icon="alert" :title="t('errors.loadTitle')" :description="errorMessage(error)">
          <button type="button" class="btn-primary" @click="refresh()">{{ t('common.retry') }}</button>
        </EmptyState>
      </div>
      <div v-else class="overflow-x-auto">
        <table class="w-full min-w-[880px]">
          <thead class="table-head">
            <tr>
              <th class="px-4 py-3">{{ t('products.product') }}</th>
              <th class="px-4 py-3">SKU</th>
              <th class="px-4 py-3 text-right">{{ t('products.price') }}</th>
              <th class="px-4 py-3">{{ t('products.expiryDate') }}</th>
              <th class="px-4 py-3">{{ t('inventory.onHand') }}</th>
              <th class="px-4 py-3">{{ t('inventory.adjust') }}</th>
            </tr>
          </thead>
          <tbody v-if="status === 'pending' && !data" aria-busy="true">
            <tr v-for="i in 8" :key="i"><td colspan="6" class="table-cell"><div class="h-10 animate-pulse rounded-lg bg-surface" /></td></tr>
          </tbody>
          <tbody v-else-if="data?.content.length" :class="status === 'pending' ? 'opacity-60' : ''">
            <tr v-for="item in data.content" :key="item.variantId" class="hover:bg-surface/60">
              <td class="table-cell">
                <div class="flex items-center gap-3">
                  <div class="flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-line bg-white">
                    <img v-if="item.imageUrl" :src="thumb(item.imageUrl, 80)" alt="" class="h-full w-full object-contain" loading="lazy">
                    <AppIcon v-else name="photo" :size="18" class="text-ink-muted" />
                  </div>
                  <div class="min-w-0">
                    <NuxtLink :to="`/products/${item.productId}`" class="block max-w-xs truncate font-semibold text-ink hover:text-brand">
                      {{ locale === 'km' ? item.productNameKm || item.productNameEn : item.productNameEn }}
                    </NuxtLink>
                    <p class="text-xs text-ink-muted">
                      {{ item.optionName }}
                      <span v-if="!item.active" class="badge-neutral ml-1">{{ t('common.inactive') }}</span>
                    </p>
                  </div>
                </div>
              </td>
              <td class="table-cell font-mono text-xs">{{ item.sku }}</td>
              <td class="table-cell text-right font-semibold">{{ formatUsd(item.price) }}</td>
              <td class="table-cell">
                <span v-if="item.expiryDate" :class="expiryBadge(item)">{{ formatDate(item.expiryDate, locale) }}</span>
                <span v-else class="text-ink-muted">—</span>
              </td>
              <td class="table-cell"><span :class="stockBadge(item)">{{ formatNumber(item.stockQty) }}</span></td>
              <td class="table-cell">
                <form class="flex items-center gap-2" @submit.prevent="saveStock(item)">
                  <input
                    :value="draftValue(item) ?? ''"
                    type="number"
                    min="0"
                    step="1"
                    inputmode="numeric"
                    class="field-input w-24"
                    :aria-label="t('inventory.newStockFor', { sku: item.sku })"
                    @input="setDraft(item, ($event.target as HTMLInputElement).value)"
                  >
                  <button type="submit" class="btn-primary min-h-9 px-3" :disabled="!isDirty(item) || savingId === item.variantId">
                    {{ t('common.save') }}
                  </button>
                </form>
              </td>
            </tr>
          </tbody>
          <tbody v-else>
            <tr>
              <td colspan="6">
                <EmptyState
                  :icon="query.filter === 'all' ? 'inventory' : 'check'"
                  :title="query.filter === 'all' ? t('common.noResults') : t(`inventory.empty.${query.filter}`)"
                />
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
        @change="updateQuery({ page: $event })"
      />
    </div>
  </div>
</template>
