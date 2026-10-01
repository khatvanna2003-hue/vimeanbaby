<script setup lang="ts">
defineProps<{ open: boolean }>()
const emit = defineEmits<{ close: [] }>()

const { t } = useI18n()
const route = useRoute()
const storeUrl = useRuntimeConfig().public.storeUrl as string

const groups = computed(() => [
  {
    label: t('nav.groups.overview'),
    items: [{ to: '/', icon: 'dashboard', label: t('nav.dashboard') }],
  },
  {
    label: t('nav.groups.sales'),
    items: [
      { to: '/orders', icon: 'orders', label: t('nav.orders') },
      { to: '/customers', icon: 'customers', label: t('nav.customers') },
    ],
  },
  {
    label: t('nav.groups.catalog'),
    items: [
      { to: '/products', icon: 'products', label: t('nav.products') },
      { to: '/inventory', icon: 'inventory', label: t('nav.inventory') },
      { to: '/categories', icon: 'categories', label: t('nav.categories') },
      { to: '/brands', icon: 'brands', label: t('nav.brands') },
    ],
  },
  {
    label: t('nav.groups.system'),
    items: [{ to: '/settings', icon: 'settings', label: t('nav.settings') }],
  },
])

function isActive(to: string) {
  return to === '/' ? route.path === '/' : route.path.startsWith(to)
}

watch(() => route.fullPath, () => emit('close'))
</script>

<template>
  <div>
    <div v-if="open" class="fixed inset-0 z-40 bg-ink/40 lg:hidden" aria-hidden="true" @click="emit('close')" />
    <aside
      class="fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r border-line/70 bg-white transition-transform lg:translate-x-0"
      :class="open ? 'translate-x-0' : '-translate-x-full'"
      :aria-label="t('nav.main')"
    >
      <div class="flex h-16 items-center justify-between border-b border-line/70 px-5">
        <NuxtLink to="/" class="flex items-center gap-2">
          <img src="/logo.svg" alt="Vimean Baby" class="h-8 w-auto" width="133" height="32">
        </NuxtLink>
        <span class="badge-brand">{{ t('app.adminBadge') }}</span>
      </div>
      <nav class="flex-1 space-y-6 overflow-y-auto px-3 py-5">
        <div v-for="group in groups" :key="group.label">
          <p class="mb-2 px-3 text-[11px] font-semibold uppercase tracking-wider text-ink-muted/80">{{ group.label }}</p>
          <ul class="space-y-0.5">
            <li v-for="item in group.items" :key="item.to">
              <NuxtLink
                :to="item.to"
                class="flex items-center gap-3 rounded-xl px-3 py-2 text-sm font-medium transition"
                :class="isActive(item.to) ? 'bg-brand-tint text-ink' : 'text-ink-muted hover:bg-surface hover:text-ink'"
                :aria-current="isActive(item.to) ? 'page' : undefined"
              >
                <AppIcon :name="item.icon" :class="isActive(item.to) ? 'text-brand' : ''" />
                {{ item.label }}
              </NuxtLink>
            </li>
          </ul>
        </div>
      </nav>
      <div class="border-t border-line/70 p-4">
        <a :href="storeUrl" target="_blank" rel="noopener" class="flex items-center gap-2 rounded-xl px-3 py-2 text-sm text-ink-muted hover:bg-surface hover:text-ink">
          <AppIcon name="external" :size="18" />
          {{ t('nav.viewStore') }}
        </a>
      </div>
    </aside>
  </div>
</template>
