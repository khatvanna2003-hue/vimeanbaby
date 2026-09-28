<script setup lang="ts">
const { t } = useI18n()
const localePath = useLocalePath()
const year = new Date().getFullYear()
const email = ref('')
const subscribed = ref(false)

const menuLinks = computed(() => [
  { to: localePath('/'), label: t('nav.home') },
  { to: localePath('/products'), label: t('nav.shop') },
  { to: localePath('/categories'), label: t('nav.categories') },
  { to: localePath('/contact'), label: t('nav.contact') },
])

const aboutLinks = computed(() => [
  { to: localePath('/contact'), label: t('footer.shipping') },
  { to: localePath('/contact'), label: t('footer.returns') },
  { to: localePath('/contact'), label: t('footer.privacy') },
  { to: localePath('/contact'), label: t('footer.terms') },
])

const socials = [
  { label: 'Facebook', href: '#', icon: 'facebook' },
  { label: 'Instagram', href: '#', icon: 'instagram' },
  { label: 'YouTube', href: '#', icon: 'youtube' },
  { label: 'TikTok', href: '#', icon: 'tiktok' },
] as const

function subscribe() {
  if (!email.value) return
  subscribed.value = true
}

function scrollTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <footer class="mt-auto border-t border-line bg-ink text-white">
    <div class="border-b border-line bg-cream">
      <div class="container-store flex flex-col gap-4 px-4 py-8 sm:flex-row sm:items-center sm:justify-between sm:px-6 lg:px-8">
        <div>
          <p class="text-lg font-bold text-ink">{{ t('footer.newsletterTitle') }}</p>
          <p class="mt-1 text-sm text-muted">{{ t('footer.newsletterBody') }}</p>
        </div>
        <form
          class="flex w-full max-w-md overflow-hidden rounded-2xl border border-line bg-white"
          @submit.prevent="subscribe"
        >
          <input
            v-model="email"
            type="email"
            required
            class="min-h-12 w-full px-4 text-sm outline-none"
            :placeholder="t('footer.newsletterPlaceholder')"
          >
          <button type="submit" class="bg-brand px-5 text-sm font-semibold text-white hover:bg-brand/90">
            {{ t('footer.subscribe') }}
          </button>
        </form>
      </div>
      <p v-if="subscribed" class="pb-4 text-center text-sm font-semibold text-brand">
        {{ t('footer.newsletterThanks') }}
      </p>
    </div>

    <div class="container-store grid gap-10 px-4 py-12 sm:px-6 md:grid-cols-2 lg:grid-cols-4 lg:px-8 lg:py-14">
      <div>
        <img
          src="/logo-white.svg"
          :alt="t('app.name')"
          class="h-12 w-auto"
          width="199"
          height="48"
          loading="lazy"
        >
        <p class="mt-3 max-w-xs text-sm leading-relaxed text-white/70">
          {{ t('footer.brandBlurb') }}
        </p>
        <div class="mt-5 flex flex-nowrap items-center gap-2">
          <span class="inline-flex h-9 shrink-0 items-center justify-center rounded-md bg-white px-2.5">
            <img
              src="/payment/khqr.webp"
              alt="KHQR"
              class="h-5 w-auto max-w-none object-contain"
              width="49"
              height="20"
              loading="lazy"
            >
          </span>
          <span class="inline-flex h-9 shrink-0 items-center justify-center rounded-md bg-white px-2.5">
            <img
              src="/payment/payway-logo.svg"
              alt="ABA PAYWAY"
              class="h-3.5 w-auto max-w-none object-contain"
              width="132"
              height="14"
              loading="lazy"
            >
          </span>
        </div>
      </div>

      <div>
        <p class="mb-4 text-sm font-bold text-white">{{ t('footer.menu') }}</p>
        <ul class="space-y-2.5 text-sm text-white/70">
          <li v-for="link in menuLinks" :key="link.label">
            <NuxtLink :to="link.to" class="transition hover:text-brand">
              {{ link.label }}
            </NuxtLink>
          </li>
        </ul>
      </div>

      <div>
        <p class="mb-4 text-sm font-bold text-white">{{ t('footer.about') }}</p>
        <ul class="space-y-2.5 text-sm text-white/70">
          <li v-for="link in aboutLinks" :key="link.label">
            <NuxtLink :to="link.to" class="transition hover:text-brand">
              {{ link.label }}
            </NuxtLink>
          </li>
        </ul>
      </div>

      <div>
        <p class="mb-4 text-sm font-bold text-white">{{ t('footer.help') }}</p>
        <p class="text-sm leading-relaxed text-white/70">
          {{ t('footer.helpIntro') }}
        </p>
        <NuxtLink
          :to="localePath('/contact')"
          class="mt-4 inline-block text-sm font-semibold text-white underline decoration-white/30 underline-offset-4 transition hover:text-brand"
        >
          {{ t('footer.contactUs') }}
        </NuxtLink>
        <p class="mt-4 text-sm text-white/70">+855 12 345 678</p>
      </div>
    </div>

    <div class="border-t border-white/10">
      <div class="container-store flex flex-col gap-4 px-4 py-5 sm:flex-row sm:items-center sm:justify-between sm:px-6 lg:px-8">
        <p class="text-xs text-white/60">
          © {{ year }}, {{ t('app.name') }}. {{ t('footer.tagline') }}
        </p>

        <div class="flex flex-wrap items-center gap-3 sm:gap-4">
          <div class="flex items-center gap-1">
            <a
              v-for="social in socials"
              :key="social.label"
              :href="social.href"
              :aria-label="social.label"
              class="inline-flex h-9 w-9 items-center justify-center rounded-full text-white transition hover:bg-white/10 hover:text-brand-light"
            >
              <svg
                v-if="social.icon === 'facebook'"
                class="h-4 w-4"
                viewBox="0 0 24 24"
                fill="currentColor"
                aria-hidden="true"
              >
                <path d="M14 9h3V6h-3c-1.7 0-3 1.3-3 3v2H9v3h2v7h3v-7h2.6l.4-3H14V9z" />
              </svg>
              <svg
                v-else-if="social.icon === 'instagram'"
                class="h-4 w-4"
                viewBox="0 0 24 24"
                fill="currentColor"
                aria-hidden="true"
              >
                <path d="M7 2h10a5 5 0 0 1 5 5v10a5 5 0 0 1-5 5H7a5 5 0 0 1-5-5V7a5 5 0 0 1 5-5zm0 2a3 3 0 0 0-3 3v10a3 3 0 0 0 3 3h10a3 3 0 0 0 3-3V7a3 3 0 0 0-3-3H7zm5 3.5A4.5 4.5 0 1 1 7.5 12 4.5 4.5 0 0 1 12 7.5zm0 2A2.5 2.5 0 1 0 14.5 12 2.5 2.5 0 0 0 12 9.5zM17.5 6.75a1 1 0 1 1-1 1 1 1 0 0 1 1-1z" />
              </svg>
              <svg
                v-else-if="social.icon === 'youtube'"
                class="h-4 w-4"
                viewBox="0 0 24 24"
                fill="currentColor"
                aria-hidden="true"
              >
                <path d="M23 12.2s0-3.2-.4-4.7c-.2-.9-.9-1.6-1.8-1.8C18.9 5.3 12 5.3 12 5.3s-6.9 0-8.8.4c-.9.2-1.6.9-1.8 1.8C1 9 1 12.2 1 12.2s0 3.2.4 4.7c.2.9.9 1.6 1.8 1.8 1.9.4 8.8.4 8.8.4s6.9 0 8.8-.4c.9-.2 1.6-.9 1.8-1.8.4-1.5.4-4.7.4-4.7zM9.8 15.5v-6.6l5.8 3.3-5.8 3.3z" />
              </svg>
              <svg
                v-else
                class="h-4 w-4"
                viewBox="0 0 24 24"
                fill="currentColor"
                aria-hidden="true"
              >
                <path d="M16.6 5.8A4.7 4.7 0 0 1 14.9 2h-2.8v13.4a2.4 2.4 0 1 1-1.7-2.3V10a5.1 5.1 0 1 0 4.5 5V9.2a7.4 7.4 0 0 0 4.3 1.4V7.8a4.7 4.7 0 0 1-2.6-2z" />
              </svg>
            </a>
          </div>

          <LanguageSwitcher />

          <span class="hidden items-center gap-2 rounded-full border border-white/20 px-3 py-2 text-xs font-medium text-white/70 sm:inline-flex">
            <span class="text-sm leading-none" aria-hidden="true">🇰🇭</span>
            {{ t('locale.cambodia') }}
          </span>

          <button
            type="button"
            class="inline-flex h-9 w-9 items-center justify-center rounded-full border border-white/20 text-white transition hover:bg-white/10 hover:text-brand-light"
            :aria-label="t('footer.backToTop')"
            @click="scrollTop"
          >
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </footer>
</template>
