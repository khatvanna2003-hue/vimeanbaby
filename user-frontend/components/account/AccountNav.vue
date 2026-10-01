<script setup lang="ts">
const { t } = useI18n()
const localePath = useLocalePath()
const route = useRoute()
const auth = useAuthStore()

const items = computed(() => [
  { to: localePath('/account'), label: t('account.nav.overview'), exact: true, icon: 'M2.25 12l8.95-8.95a1.13 1.13 0 0 1 1.6 0L21.75 12M4.5 9.75v10.13c0 .62.5 1.12 1.13 1.12H9.75v-4.88c0-.62.5-1.12 1.13-1.12h2.25c.62 0 1.12.5 1.12 1.12V21h4.13c.62 0 1.12-.5 1.12-1.13V9.75M8.25 21h8.25' },
  { to: localePath('/account/profile'), label: t('account.nav.profile'), exact: false, icon: 'M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.5 20.12a7.5 7.5 0 0 1 15 0A17.93 17.93 0 0 1 12 21.75c-2.68 0-5.22-.58-7.5-1.63Z' },
  { to: localePath('/account/orders'), label: t('account.nav.orders'), exact: false, icon: 'M20.25 7.5l-.63 10.63a2.25 2.25 0 0 1-2.24 2.12H6.62a2.25 2.25 0 0 1-2.24-2.12L3.75 7.5M10 11.25h4M3.38 7.5h17.25c.62 0 1.12-.5 1.12-1.13v-1.5c0-.62-.5-1.12-1.12-1.12H3.38c-.63 0-1.13.5-1.13 1.12v1.5c0 .63.5 1.13 1.13 1.13Z' },
  { to: localePath('/account/addresses'), label: t('account.nav.addresses'), exact: false, icon: 'M15 10.5a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z M19.5 10.5c0 7.14-7.5 11.25-7.5 11.25S4.5 17.64 4.5 10.5a7.5 7.5 0 1 1 15 0Z' },
  { to: localePath('/account/security'), label: t('account.nav.security'), exact: false, icon: 'M16.5 10.5V6.75a4.5 4.5 0 1 0-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 0 0 2.25-2.25v-6.75a2.25 2.25 0 0 0-2.25-2.25H6.75a2.25 2.25 0 0 0-2.25 2.25v6.75a2.25 2.25 0 0 0 2.25 2.25Z' },
])

function isActive(item: { to: string, exact: boolean }) {
  return item.exact ? route.path === item.to : route.path.startsWith(item.to)
}

async function logout() {
  auth.logout()
  await navigateTo(localePath('/'))
}
</script>

<template>
  <nav :aria-label="t('nav.account')">
    <div class="hidden rounded-[1.5rem] border border-line bg-white p-5 shadow-soft lg:block">
      <div class="flex items-center gap-3 border-b border-line pb-5">
        <span class="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-brand text-base font-bold text-white">
          {{ initials(auth.user?.fullName) }}
        </span>
        <div class="min-w-0">
          <p class="truncate font-semibold text-ink">{{ auth.user?.fullName }}</p>
          <p class="truncate text-xs text-muted">{{ auth.user?.email }}</p>
        </div>
      </div>
      <ul class="mt-4 space-y-1">
        <li v-for="item in items" :key="item.to">
          <NuxtLink
            :to="item.to"
            class="flex items-center gap-3 rounded-2xl px-3 py-2.5 text-sm font-semibold transition"
            :class="isActive(item) ? 'bg-brand-tint text-ink' : 'text-muted hover:bg-cream hover:text-ink'"
            :aria-current="isActive(item) ? 'page' : undefined"
          >
            <svg class="h-5 w-5 shrink-0" :class="isActive(item) ? 'text-brand' : ''" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.6" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" :d="item.icon" />
            </svg>
            {{ item.label }}
          </NuxtLink>
        </li>
      </ul>
      <button
        type="button"
        class="mt-4 flex w-full items-center gap-3 rounded-2xl border-t border-line px-3 pb-1 pt-4 text-sm font-semibold text-muted transition hover:text-danger"
        @click="logout"
      >
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.6" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0 0 13.5 3h-6a2.25 2.25 0 0 0-2.25 2.25v13.5A2.25 2.25 0 0 0 7.5 21h6a2.25 2.25 0 0 0 2.25-2.25V15m3 0 3-3m0 0-3-3m3 3H9" />
        </svg>
        {{ t('account.logout') }}
      </button>
    </div>

    <div class="-mx-4 flex gap-2 overflow-x-auto px-4 pb-1 lg:hidden">
      <NuxtLink
        v-for="item in items"
        :key="item.to"
        :to="item.to"
        class="shrink-0 rounded-full border px-4 py-2 text-sm font-semibold transition"
        :class="isActive(item) ? 'border-brand bg-brand text-white' : 'border-line bg-white text-ink'"
        :aria-current="isActive(item) ? 'page' : undefined"
      >
        {{ item.label }}
      </NuxtLink>
    </div>
  </nav>
</template>
