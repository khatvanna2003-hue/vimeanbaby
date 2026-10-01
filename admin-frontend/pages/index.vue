<script setup lang="ts">
import type { Dashboard } from '~/types/admin'

const { t, locale } = useI18n()
const auth = useAuthStore()
const { apiFetch } = useApi()

useHead({ title: () => `${t('nav.dashboard')} | Vimean Baby Admin` })

const { data, status, error, refresh } = await useAsyncData('dashboard', () => apiFetch<Dashboard>('/admin/dashboard'))

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return t('dashboard.greeting.morning')
  if (hour < 18) return t('dashboard.greeting.afternoon')
  return t('dashboard.greeting.evening')
})

const today = computed(() => new Intl.DateTimeFormat(locale.value === 'km' ? 'km-KH' : 'en-GB', {
  weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
}).format(new Date()))

const signupMax = computed(() => Math.max(1, ...(data.value?.signups.map(d => d.count) ?? [0])))
const signupTotal = computed(() => data.value?.signups.reduce((sum, d) => sum + d.count, 0) ?? 0)
const categoryMax = computed(() => Math.max(1, ...(data.value?.productsByCategory.map(c => c.count) ?? [0])))

function barHeight(count: number) {
  return Math.max(3, (count / signupMax.value) * 96)
}

function dayLabel(iso: string) {
  return new Intl.DateTimeFormat(locale.value === 'km' ? 'km-KH' : 'en-GB', { day: 'numeric', month: 'short' }).format(new Date(`${iso}T00:00:00`))
}
</script>

<template>
  <div>
    <PageHeader :title="`${greeting}, ${auth.user?.fullName?.split(' ')[0] ?? ''}`" :description="today">
      <template #actions>
        <button type="button" class="btn-secondary" :disabled="status === 'pending'" @click="refresh()">
          <AppIcon name="refresh" :size="16" :class="status === 'pending' ? 'animate-spin' : ''" />
          {{ t('common.refresh') }}
        </button>
        <NuxtLink to="/products/new" class="btn-primary">
          <AppIcon name="plus" :size="16" /> {{ t('products.add') }}
        </NuxtLink>
      </template>
    </PageHeader>

    <div v-if="error" class="card p-6">
      <EmptyState icon="alert" :title="t('errors.loadTitle')" :description="t('errors.GENERIC')">
        <button type="button" class="btn-primary" @click="refresh()">{{ t('common.retry') }}</button>
      </EmptyState>
    </div>

    <div v-else-if="!data" class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4" aria-busy="true">
      <div v-for="i in 8" :key="i" class="card h-28 animate-pulse bg-white" />
    </div>

    <div v-else class="space-y-6">
      <div class="flex items-start gap-3 rounded-2xl border border-line/70 bg-white px-4 py-3 text-sm text-ink-muted">
        <AppIcon name="orders" :size="18" class="mt-0.5 text-brand" />
        <p>{{ t('dashboard.ordersNotice') }}</p>
      </div>

      <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          :label="t('dashboard.stats.inventoryValue')"
          :value="formatUsd(data.inventory.inventoryValue)"
          :hint="t('dashboard.stats.units', { count: formatNumber(data.inventory.stockUnits) })"
          icon="money"
          tone="brand"
          to="/inventory"
        />
        <StatCard
          :label="t('dashboard.stats.products')"
          :value="formatNumber(data.catalog.products)"
          :hint="t('dashboard.stats.publishedDrafts', { published: formatNumber(data.catalog.published), drafts: formatNumber(data.catalog.drafts) })"
          icon="products"
          tone="mint"
          to="/products"
        />
        <StatCard
          :label="t('dashboard.stats.customers')"
          :value="formatNumber(data.customers.total)"
          :hint="t('dashboard.stats.newCustomers', { count: formatNumber(data.customers.newLast30Days) })"
          icon="customers"
          tone="neutral"
          to="/customers"
        />
        <StatCard
          :label="t('dashboard.stats.attention')"
          :value="formatNumber(data.inventory.lowStock + data.inventory.outOfStock + data.inventory.expiringSoon)"
          :hint="t('dashboard.stats.attentionHint', { low: data.inventory.lowStock, out: data.inventory.outOfStock, expiring: data.inventory.expiringSoon })"
          icon="alert"
          :tone="data.inventory.outOfStock > 0 ? 'danger' : 'warn'"
          to="/inventory?filter=low"
        />
      </div>

      <div class="grid gap-6 xl:grid-cols-3">
        <section class="card p-5 xl:col-span-2">
          <div class="flex flex-wrap items-baseline justify-between gap-2">
            <div>
              <h2 class="font-bold text-ink">{{ t('dashboard.signups.title') }}</h2>
              <p class="text-xs text-ink-muted">{{ t('dashboard.signups.subtitle') }}</p>
            </div>
            <p class="text-2xl font-bold text-ink">{{ formatNumber(signupTotal) }}</p>
          </div>
          <svg
            class="mt-6 h-44 w-full"
            :viewBox="`0 0 ${data.signups.length * 10} 100`"
            preserveAspectRatio="none"
            role="img"
            :aria-label="t('dashboard.signups.title')"
          >
            <rect
              v-for="(day, index) in data.signups"
              :key="day.date"
              :x="index * 10 + 1.5"
              :y="100 - barHeight(day.count)"
              width="7"
              :height="barHeight(day.count)"
              rx="1"
              :class="day.count ? 'fill-brand hover:fill-brand-light' : 'fill-line'"
            >
              <title>{{ dayLabel(day.date) }}: {{ day.count }}</title>
            </rect>
          </svg>
          <div class="mt-2 flex text-[10px] text-ink-muted">
            <span v-for="(day, index) in data.signups" :key="day.date" class="flex-1 truncate text-center">
              {{ index % 2 === 0 ? dayLabel(day.date) : '' }}
            </span>
          </div>
        </section>

        <section class="card p-5">
          <h2 class="font-bold text-ink">{{ t('dashboard.catalogHealth') }}</h2>
          <dl class="mt-4 space-y-3 text-sm">
            <div class="flex items-center justify-between">
              <dt class="text-ink-muted">{{ t('nav.categories') }}</dt>
              <dd class="font-semibold">{{ formatNumber(data.catalog.categories) }}</dd>
            </div>
            <div class="flex items-center justify-between">
              <dt class="text-ink-muted">{{ t('nav.brands') }}</dt>
              <dd class="font-semibold">{{ formatNumber(data.catalog.brands) }}</dd>
            </div>
            <div class="flex items-center justify-between">
              <dt class="text-ink-muted">{{ t('dashboard.sellableVariants') }}</dt>
              <dd class="font-semibold">{{ formatNumber(data.inventory.sellableVariants) }}</dd>
            </div>
            <div class="flex items-center justify-between">
              <dt class="text-ink-muted">{{ t('inventory.filters.low') }}</dt>
              <dd><span :class="data.inventory.lowStock ? 'badge-pending' : 'badge-success'">{{ data.inventory.lowStock }}</span></dd>
            </div>
            <div class="flex items-center justify-between">
              <dt class="text-ink-muted">{{ t('inventory.filters.out') }}</dt>
              <dd><span :class="data.inventory.outOfStock ? 'badge-danger' : 'badge-success'">{{ data.inventory.outOfStock }}</span></dd>
            </div>
            <div class="flex items-center justify-between">
              <dt class="text-ink-muted">{{ t('inventory.filters.expiring') }}</dt>
              <dd><span :class="data.inventory.expiringSoon ? 'badge-pending' : 'badge-success'">{{ data.inventory.expiringSoon }}</span></dd>
            </div>
          </dl>
          <p class="mt-4 border-t border-line/70 pt-3 text-xs text-ink-muted">
            {{ t('dashboard.thresholds', { threshold: data.inventory.lowStockThreshold, days: data.inventory.expiryWarningDays }) }}
          </p>
        </section>
      </div>

      <div class="grid gap-6 xl:grid-cols-2">
        <section class="card overflow-hidden">
          <header class="flex items-center justify-between border-b border-line/70 px-5 py-4">
            <h2 class="font-bold text-ink">{{ t('dashboard.lowStock') }}</h2>
            <NuxtLink to="/inventory?filter=low" class="text-sm font-semibold text-brand-light hover:text-brand">{{ t('common.viewAll') }}</NuxtLink>
          </header>
          <EmptyState v-if="!data.lowStockItems.length" icon="check" :title="t('dashboard.allStocked')" />
          <ul v-else>
            <li v-for="item in data.lowStockItems" :key="item.variantId" class="flex items-center gap-3 border-t border-line/50 px-5 py-3 first:border-t-0">
              <img v-if="item.imageUrl" :src="thumb(item.imageUrl, 80)" alt="" class="h-10 w-10 rounded-lg border border-line object-contain" loading="lazy">
              <div class="min-w-0 flex-1">
                <NuxtLink :to="`/products/${item.productId}`" class="block truncate text-sm font-semibold text-ink hover:text-brand">{{ item.productNameEn }}</NuxtLink>
                <p class="truncate text-xs text-ink-muted">{{ item.sku }} · {{ item.optionName }}</p>
              </div>
              <span :class="item.stockQty <= 0 ? 'badge-danger' : 'badge-pending'">{{ t('inventory.left', { count: item.stockQty }) }}</span>
            </li>
          </ul>
        </section>

        <section class="card overflow-hidden">
          <header class="flex items-center justify-between border-b border-line/70 px-5 py-4">
            <h2 class="font-bold text-ink">{{ t('dashboard.expiring') }}</h2>
            <NuxtLink to="/inventory?filter=expiring" class="text-sm font-semibold text-brand-light hover:text-brand">{{ t('common.viewAll') }}</NuxtLink>
          </header>
          <EmptyState v-if="!data.expiringItems.length" icon="check" :title="t('dashboard.noExpiring')" :description="t('dashboard.noExpiringHint', { days: data.inventory.expiryWarningDays })" />
          <ul v-else>
            <li v-for="item in data.expiringItems" :key="item.variantId" class="flex items-center gap-3 border-t border-line/50 px-5 py-3 first:border-t-0">
              <div class="min-w-0 flex-1">
                <NuxtLink :to="`/products/${item.productId}`" class="block truncate text-sm font-semibold text-ink hover:text-brand">{{ item.productNameEn }}</NuxtLink>
                <p class="truncate text-xs text-ink-muted">{{ item.sku }} · {{ formatDate(item.expiryDate, locale) }}</p>
              </div>
              <span :class="(daysUntil(item.expiryDate) ?? 0) < 0 ? 'badge-danger' : 'badge-pending'">
                {{ t('inventory.daysLeft', { count: daysUntil(item.expiryDate) ?? 0 }) }}
              </span>
            </li>
          </ul>
        </section>
      </div>

      <div class="grid gap-6 xl:grid-cols-3">
        <section class="card p-5">
          <h2 class="font-bold text-ink">{{ t('dashboard.byCategory') }}</h2>
          <ul class="mt-4 space-y-3">
            <li v-for="row in data.productsByCategory" :key="row.name">
              <div class="flex justify-between text-sm">
                <span class="truncate text-ink">{{ row.name }}</span>
                <span class="font-semibold text-ink">{{ row.count }}</span>
              </div>
              <svg class="mt-1 h-1.5 w-full rounded-full" viewBox="0 0 100 2" preserveAspectRatio="none" aria-hidden="true">
                <rect width="100" height="2" class="fill-surface" />
                <rect :width="(row.count / categoryMax) * 100" height="2" class="fill-brand-light" />
              </svg>
            </li>
          </ul>
        </section>

        <section class="card overflow-hidden">
          <header class="flex items-center justify-between border-b border-line/70 px-5 py-4">
            <h2 class="font-bold text-ink">{{ t('dashboard.recentProducts') }}</h2>
            <NuxtLink to="/products" class="text-sm font-semibold text-brand-light hover:text-brand">{{ t('common.viewAll') }}</NuxtLink>
          </header>
          <ul>
            <li v-for="product in data.recentProducts" :key="product.id" class="border-t border-line/50 px-5 py-3 first:border-t-0">
              <NuxtLink :to="`/products/${product.id}`" class="block truncate text-sm font-semibold text-ink hover:text-brand">{{ product.nameEn }}</NuxtLink>
              <p class="text-xs text-ink-muted">{{ product.categoryName }} · {{ formatDate(product.createdAt, locale) }}</p>
            </li>
          </ul>
        </section>

        <section class="card overflow-hidden">
          <header class="flex items-center justify-between border-b border-line/70 px-5 py-4">
            <h2 class="font-bold text-ink">{{ t('dashboard.recentCustomers') }}</h2>
            <NuxtLink to="/customers" class="text-sm font-semibold text-brand-light hover:text-brand">{{ t('common.viewAll') }}</NuxtLink>
          </header>
          <EmptyState v-if="!data.recentCustomers.length" icon="customers" :title="t('customers.empty')" />
          <ul v-else>
            <li v-for="customer in data.recentCustomers" :key="customer.id" class="flex items-center gap-3 border-t border-line/50 px-5 py-3 first:border-t-0">
              <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-brand-tint text-xs font-bold text-ink">{{ initials(customer.fullName) }}</span>
              <div class="min-w-0 flex-1">
                <NuxtLink :to="`/customers/${customer.id}`" class="block truncate text-sm font-semibold text-ink hover:text-brand">{{ customer.fullName }}</NuxtLink>
                <p class="truncate text-xs text-ink-muted">{{ formatDate(customer.createdAt, locale) }}</p>
              </div>
            </li>
          </ul>
        </section>
      </div>
    </div>
  </div>
</template>
