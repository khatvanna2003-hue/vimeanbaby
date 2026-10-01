<script setup lang="ts">
import type { CustomerSummary, PageResponse } from '~/types/admin'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const { apiFetch } = useApi()
const errorMessage = useApiErrorMessage()

useHead({ title: () => `${t('nav.customers')} | Vimean Baby Admin` })

const query = computed(() => ({
  q: typeof route.query.q === 'string' ? route.query.q : '',
  status: typeof route.query.status === 'string' ? route.query.status : '',
  page: Number(route.query.page ?? 0) || 0,
}))

function updateQuery(patch: Partial<{ q: string, status: string, page: number }>) {
  const merged = { ...query.value, page: 0, ...patch }
  const next: Record<string, string> = {}
  if (merged.q) next.q = merged.q
  if (merged.status) next.status = merged.status
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
  'admin-customers',
  () => apiFetch<PageResponse<CustomerSummary>>('/admin/customers', {
    query: { q: query.value.q || undefined, status: query.value.status || undefined, page: query.value.page, size: 20 },
  }),
  { watch: [query] },
)
</script>

<template>
  <div>
    <PageHeader :title="t('nav.customers')" :description="t('customers.subtitle', { count: formatNumber(data?.totalElements ?? 0) })" />

    <div class="card overflow-hidden">
      <div class="flex flex-col gap-3 border-b border-line/70 p-4 sm:flex-row">
        <label class="relative flex-1">
          <span class="sr-only">{{ t('common.search') }}</span>
          <AppIcon name="search" :size="18" class="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-ink-muted" />
          <input v-model="search" type="search" class="field-input pl-10" :placeholder="t('customers.searchPlaceholder')">
        </label>
        <select class="field-input sm:w-44" :value="query.status" :aria-label="t('common.status')" @change="updateQuery({ status: ($event.target as HTMLSelectElement).value })">
          <option value="">{{ t('common.allStatuses') }}</option>
          <option value="active">{{ t('common.active') }}</option>
          <option value="disabled">{{ t('customers.disabled') }}</option>
        </select>
      </div>

      <div v-if="error" class="p-6">
        <EmptyState icon="alert" :title="t('errors.loadTitle')" :description="errorMessage(error)">
          <button type="button" class="btn-primary" @click="refresh()">{{ t('common.retry') }}</button>
        </EmptyState>
      </div>
      <div v-else class="overflow-x-auto">
        <table class="w-full min-w-[760px]">
          <thead class="table-head">
            <tr>
              <th class="px-4 py-3">{{ t('customers.customer') }}</th>
              <th class="px-4 py-3">{{ t('customers.phone') }}</th>
              <th class="px-4 py-3">{{ t('customers.joined') }}</th>
              <th class="px-4 py-3">{{ t('customers.lastLogin') }}</th>
              <th class="px-4 py-3">{{ t('common.status') }}</th>
            </tr>
          </thead>
          <tbody v-if="status === 'pending' && !data" aria-busy="true">
            <tr v-for="i in 6" :key="i"><td colspan="5" class="table-cell"><div class="h-10 animate-pulse rounded-lg bg-surface" /></td></tr>
          </tbody>
          <tbody v-else-if="data?.content.length" :class="status === 'pending' ? 'opacity-60' : ''">
            <tr v-for="customer in data.content" :key="customer.id" class="cursor-pointer hover:bg-surface/60" @click="router.push(`/customers/${customer.id}`)">
              <td class="table-cell">
                <div class="flex items-center gap-3">
                  <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-brand-tint text-xs font-bold text-ink">{{ initials(customer.fullName) }}</span>
                  <div class="min-w-0">
                    <NuxtLink :to="`/customers/${customer.id}`" class="block truncate font-semibold text-ink hover:text-brand" @click.stop>{{ customer.fullName }}</NuxtLink>
                    <p class="truncate text-xs text-ink-muted">{{ customer.email }}</p>
                  </div>
                </div>
              </td>
              <td class="table-cell">{{ customer.phone }}</td>
              <td class="table-cell text-ink-muted">{{ formatDate(customer.createdAt, locale) }}</td>
              <td class="table-cell text-ink-muted">{{ formatDateTime(customer.lastLoginAt, locale) }}</td>
              <td class="table-cell">
                <span :class="customer.active ? 'badge-success' : 'badge-danger'">{{ customer.active ? t('common.active') : t('customers.disabled') }}</span>
              </td>
            </tr>
          </tbody>
          <tbody v-else>
            <tr>
              <td colspan="5">
                <EmptyState icon="customers" :title="query.q || query.status ? t('common.noResults') : t('customers.empty')" :description="query.q || query.status ? t('common.tryOtherFilters') : t('customers.emptyHint')" />
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
