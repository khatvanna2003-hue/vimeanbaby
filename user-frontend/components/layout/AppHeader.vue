<script setup lang="ts">
const { t, locale } = useI18n()
const localePath = useLocalePath()
const cart = useCartStore()
const auth = useAuthStore()
const mobileOpen = ref(false)
const searchQuery = ref('')

const navLinks = computed(() => [
  { to: localePath('/'), label: t('nav.home') },
  { to: localePath('/products'), label: t('nav.shop') },
  { to: localePath('/categories'), label: t('nav.categories') },
  { to: localePath('/contact'), label: t('nav.contact') },
])

function onSearch() {
  navigateTo({
    path: localePath('/products'),
    query: searchQuery.value ? { q: searchQuery.value } : undefined,
  })
  mobileOpen.value = false
}
</script>

<template>
  <header class="sticky top-0 z-40">
    <!-- Utility bar (Leu24 structure, soft baby tones) -->
    <div class="bg-ink text-white">
      <div class="container-store flex items-center justify-between gap-3 px-4 py-2 text-xs sm:px-6 lg:px-8">
        <p class="truncate italic opacity-90">
          {{ t('app.utilityTagline') }}
        </p>
        <div class="flex shrink-0 items-center gap-3 sm:gap-4">
          <a href="tel:+85512345678" class="hidden items-center gap-1.5 sm:inline-flex">
            <span>Tel: +855 12 345 678</span>
          </a>
          <NuxtLink v-if="auth.isLoggedIn" :to="localePath('/account')" class="hidden max-w-40 truncate sm:inline">
            {{ t('account.hi', { name: auth.user?.fullName.split(' ')[0] }) }}
          </NuxtLink>
          <span v-else class="hidden sm:inline">
            <NuxtLink :to="localePath('/login')" class="hover:underline">{{ t('auth.signIn') }}</NuxtLink>
            <span class="px-1 opacity-60">/</span>
            <NuxtLink :to="localePath('/register')" class="hover:underline">{{ t('auth.register') }}</NuxtLink>
          </span>
          <span class="rounded-full bg-white/10 px-2.5 py-1">
            {{ t('locale.cambodia') }}
          </span>
        </div>
      </div>
    </div>

    <!-- Main header -->
    <div class="border-b border-line bg-white">
      <div class="container-store flex items-center gap-3 px-4 py-3 sm:px-6 lg:gap-6 lg:px-8 lg:py-4">
        <button
          type="button"
          class="inline-flex h-11 w-11 items-center justify-center rounded-full border border-line lg:hidden"
          :aria-label="t('nav.shop')"
          @click="mobileOpen = true"
        >
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M4 7h16M4 12h16M4 17h16" />
          </svg>
        </button>

        <NuxtLink :to="localePath('/')" class="shrink-0" :aria-label="t('app.name')">
          <img
            src="/logo.svg"
            :alt="t('app.name')"
            class="h-10 w-auto sm:h-12"
            width="199"
            height="48"
          >
        </NuxtLink>

        <form class="mx-auto hidden min-w-0 flex-1 md:flex" @submit.prevent="onSearch">
          <div class="flex w-full max-w-2xl overflow-hidden rounded-2xl border border-line bg-cream">
            <span class="hidden items-center border-r border-line px-3 text-xs text-muted lg:inline-flex">
              {{ t('search.all') }}
            </span>
            <input
              v-model="searchQuery"
              type="search"
              class="min-h-12 w-full bg-transparent px-4 text-sm outline-none"
              :placeholder="t('search.placeholder')"
            >
            <button
              type="submit"
              class="inline-flex min-h-12 min-w-12 items-center justify-center bg-brand text-white"
              :aria-label="t('nav.search')"
            >
              <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m21 21-4.3-4.3m1.8-5.2a7 7 0 1 1-14 0 7 7 0 0 1 14 0Z" />
              </svg>
            </button>
          </div>
        </form>

        <div class="ml-auto flex items-center gap-2 sm:gap-3">
          <div class="hidden text-right xl:block">
            <p class="text-xs font-semibold text-ink">{{ t('nav.support') }}</p>
            <p class="text-xs text-muted">+855 12 345 678</p>
          </div>
          <LanguageSwitcher />
          <AccountMenu />
          <button
            type="button"
            class="relative inline-flex h-11 w-11 items-center justify-center rounded-full border border-line"
            :aria-label="t('nav.cart')"
            @click="cart.toggle(true)"
          >
            <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M2.25 3h1.386c.51 0 .955.343 1.087.835l.383 1.437M7.5 14.25h9.75l2.055-7.72H5.106M7.5 14.25 5.106 5.272M7.5 14.25l-1.28 4.16A1.125 1.125 0 0 0 7.29 20.25h9.96" />
            </svg>
            <span
              v-if="cart.count"
              class="absolute -right-1 -top-1 inline-flex h-5 min-w-5 items-center justify-center rounded-full bg-brand px-1 text-[10px] font-bold text-white"
            >
              {{ cart.count }}
            </span>
          </button>
        </div>
      </div>
    </div>

    <!-- Primary nav -->
    <nav class="hidden bg-brand lg:block">
      <div class="container-store flex items-center justify-center gap-8 px-8 py-3">
        <NuxtLink
          v-for="link in navLinks"
          :key="link.label"
          :to="link.to"
          class="text-sm font-semibold text-white/95 transition hover:text-white"
        >
          {{ link.label }}
        </NuxtLink>
      </div>
    </nav>

    <MobileNav :open="mobileOpen" :links="navLinks" @close="mobileOpen = false" />
  </header>
</template>
