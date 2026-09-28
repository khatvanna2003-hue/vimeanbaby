<script setup lang="ts">
const { t } = useI18n()
const localePath = useLocalePath()
const year = new Date().getFullYear()
const email = ref('')
const subscribed = ref(false)

function subscribe() {
  if (!email.value) return
  subscribed.value = true
}
</script>

<template>
  <footer class="mt-auto border-t border-line bg-white">
    <div class="border-b border-line bg-gradient-to-r from-mint-tint via-brand-tint to-blush-tint">
      <div class="container-store flex flex-col gap-4 px-4 py-8 sm:flex-row sm:items-center sm:justify-between sm:px-6 lg:px-8">
        <div>
          <p class="text-lg font-bold">{{ t('footer.newsletterTitle') }}</p>
          <p class="text-sm text-muted">{{ t('footer.newsletterBody') }}</p>
        </div>
        <form class="flex w-full max-w-md overflow-hidden rounded-2xl border border-line bg-white" @submit.prevent="subscribe">
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
      <p v-if="subscribed" class="pb-4 text-center text-sm font-semibold text-ink">
        {{ t('footer.newsletterThanks') }}
      </p>
    </div>

    <div class="container-store grid gap-8 section-pad md:grid-cols-4">
      <div class="md:col-span-2">
        <p class="text-2xl font-bold text-ink">{{ t('app.name') }}</p>
        <p class="mt-2 max-w-md text-sm text-muted">{{ t('home.storyBody') }}</p>
        <div class="mt-4 flex gap-2">
          <a
            v-for="social in ['Facebook', 'Instagram', 'Telegram', 'TikTok']"
            :key="social"
            href="#"
            class="inline-flex h-10 items-center rounded-full border border-line px-3 text-xs font-semibold text-muted hover:bg-cream"
          >
            {{ social }}
          </a>
        </div>
      </div>
      <div>
        <p class="mb-3 text-sm font-semibold">{{ t('footer.about') }}</p>
        <ul class="space-y-2 text-sm text-muted">
          <li><NuxtLink :to="localePath('/')">{{ t('nav.home') }}</NuxtLink></li>
          <li><NuxtLink :to="localePath('/products')">{{ t('nav.shop') }}</NuxtLink></li>
          <li><NuxtLink :to="localePath('/categories')">{{ t('nav.categories') }}</NuxtLink></li>
          <li><NuxtLink :to="localePath('/contact')">{{ t('nav.contact') }}</NuxtLink></li>
        </ul>
      </div>
      <div>
        <p class="mb-3 text-sm font-semibold">{{ t('footer.help') }}</p>
        <ul class="space-y-2 text-sm text-muted">
          <li><NuxtLink :to="localePath('/contact')">{{ t('footer.shipping') }}</NuxtLink></li>
          <li><NuxtLink :to="localePath('/contact')">{{ t('footer.returns') }}</NuxtLink></li>
          <li><NuxtLink :to="localePath('/contact')">{{ t('footer.privacy') }}</NuxtLink></li>
          <li><a href="tel:+85512345678">+855 12 345 678</a></li>
        </ul>
      </div>
    </div>
    <div class="border-t border-line px-4 py-4 text-center text-xs text-muted">
      © {{ year }} {{ t('app.name') }}. {{ t('footer.rights') }}.
    </div>
  </footer>
</template>
