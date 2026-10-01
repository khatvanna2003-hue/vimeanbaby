<script setup lang="ts">
const { t } = useI18n()
const localePath = useLocalePath()
const route = useRoute()
const auth = useAuthStore()

const open = ref(false)
const root = ref<HTMLElement | null>(null)

onClickOutside(root, () => {
  open.value = false
})
watch(() => route.fullPath, () => {
  open.value = false
})

const links = computed(() => [
  { to: localePath('/account'), label: t('account.nav.overview') },
  { to: localePath('/account/orders'), label: t('account.nav.orders') },
  { to: localePath('/account/addresses'), label: t('account.nav.addresses') },
  { to: localePath('/account/security'), label: t('account.nav.security') },
])

async function logout() {
  open.value = false
  auth.logout()
  await navigateTo(localePath('/'))
}
</script>

<template>
  <div ref="root" class="relative hidden sm:block">
    <NuxtLink
      v-if="!auth.isLoggedIn"
      :to="localePath('/login')"
      class="inline-flex h-11 w-11 items-center justify-center rounded-full border border-line"
      :aria-label="t('auth.signIn')"
    >
      <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M15.75 7.5a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.5 20.25a7.5 7.5 0 0 1 15 0" />
      </svg>
    </NuxtLink>

    <template v-else>
      <button
        type="button"
        class="inline-flex h-11 w-11 items-center justify-center rounded-full bg-brand text-sm font-bold text-white transition hover:bg-brand/90"
        :aria-label="t('nav.account')"
        aria-haspopup="menu"
        :aria-expanded="open"
        @click="open = !open"
      >
        {{ initials(auth.user?.fullName) }}
      </button>
      <div
        v-if="open"
        class="absolute right-0 top-full z-50 mt-2 w-64 overflow-hidden rounded-2xl border border-line bg-white shadow-soft"
        role="menu"
      >
        <div class="border-b border-line px-4 py-3">
          <p class="truncate text-sm font-semibold text-ink">{{ auth.user?.fullName }}</p>
          <p class="truncate text-xs text-muted">{{ auth.user?.email }}</p>
        </div>
        <NuxtLink
          v-for="link in links"
          :key="link.to"
          :to="link.to"
          class="block px-4 py-2.5 text-sm text-ink hover:bg-cream"
          role="menuitem"
        >
          {{ link.label }}
        </NuxtLink>
        <button
          type="button"
          class="block w-full border-t border-line px-4 py-2.5 text-left text-sm font-semibold text-muted hover:bg-cream hover:text-danger"
          role="menuitem"
          @click="logout"
        >
          {{ t('account.logout') }}
        </button>
      </div>
    </template>
  </div>
</template>
