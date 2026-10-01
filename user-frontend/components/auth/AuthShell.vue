<script setup lang="ts">
defineProps<{
  title: string
  subtitle: string
}>()

const { t } = useI18n()
const localePath = useLocalePath()

const perks = computed(() => [
  { icon: 'M9 12.75 11.25 15 15 9.75m-3-7.04A11.96 11.96 0 0 1 3.6 6 12 12 0 0 0 3 9.75c0 5.6 3.82 10.3 9 11.62 5.18-1.33 9-6.03 9-11.62 0-1.31-.21-2.57-.6-3.75h-.15c-3.2 0-6.1-1.25-8.25-3.29Z', label: t('auth.perks.authentic') },
  { icon: 'M8.25 18.75a1.5 1.5 0 0 1-3 0m3 0a1.5 1.5 0 0 0-3 0m3 0h6m-9 0H3.38a1.13 1.13 0 0 1-1.13-1.13V14.25m17.25 4.5a1.5 1.5 0 0 1-3 0m3 0a1.5 1.5 0 0 0-3 0m3 0h1.13c.62 0 1.13-.5 1.13-1.13v-3.37M16.5 18.75h-2.25m0-11.18v-.95c0-.57-.42-1.05-.98-1.12a48.6 48.6 0 0 0-10.04 0 1.13 1.13 0 0 0-.98 1.12v10.13m12-8.18h2.54c.43 0 .83.25 1.01.64l1.82 3.89c.07.15.11.31.11.48v3.17', label: t('auth.perks.tracking') },
  { icon: 'M15.75 10.5V6a3.75 3.75 0 1 0-7.5 0v4.5m11.36-1.99 1.26 12c.07.67-.45 1.24-1.12 1.24H4.25a1.13 1.13 0 0 1-1.12-1.24l1.26-12A1.13 1.13 0 0 1 5.51 7.5h12.98c.58 0 1.06.43 1.12 1.01Z', label: t('auth.perks.checkout') },
  { icon: 'M21 11.25v8.25a1.5 1.5 0 0 1-1.5 1.5H5.25a1.5 1.5 0 0 1-1.5-1.5v-8.25M12 4.88A2.63 2.63 0 1 0 9.38 7.5H12m0-2.63V7.5m0-2.63A2.63 2.63 0 1 1 14.63 7.5H12m0 0V21m-8.63-9.75h18c.62 0 1.13-.5 1.13-1.13v-1.5c0-.62-.5-1.12-1.13-1.12h-18c-.62 0-1.12.5-1.12 1.12v1.5c0 .63.5 1.13 1.12 1.13Z', label: t('auth.perks.offers') },
])
</script>

<template>
  <div class="container-store px-4 py-8 sm:px-6 lg:px-8 lg:py-14">
    <div class="mx-auto grid max-w-5xl overflow-hidden rounded-[1.75rem] border border-line bg-white shadow-soft lg:grid-cols-[1fr_1.1fr]">
      <aside class="relative hidden overflow-hidden bg-brand p-10 text-white lg:flex lg:flex-col">
        <div class="absolute -right-16 -top-16 h-56 w-56 rounded-full bg-white/10" aria-hidden="true" />
        <div class="absolute -bottom-20 -left-10 h-64 w-64 rounded-full bg-white/10" aria-hidden="true" />
        <NuxtLink :to="localePath('/')" class="relative inline-flex" :aria-label="t('app.name')">
          <img src="/logo-white.svg" :alt="t('app.name')" class="h-11 w-auto" width="183" height="44">
        </NuxtLink>
        <div class="relative mt-auto">
          <h2 class="text-3xl font-bold leading-tight">{{ t('auth.panelTitle') }}</h2>
          <p class="mt-3 text-sm text-white/85">{{ t('auth.panelBody') }}</p>
          <ul class="mt-8 space-y-4">
            <li v-for="perk in perks" :key="perk.label" class="flex items-center gap-3 text-sm font-medium">
              <span class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-white/15">
                <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.6" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" :d="perk.icon" />
                </svg>
              </span>
              {{ perk.label }}
            </li>
          </ul>
        </div>
      </aside>

      <section class="p-6 sm:p-10 lg:p-12">
        <h1 class="text-2xl font-bold text-ink sm:text-3xl">{{ title }}</h1>
        <p class="mt-2 text-sm text-muted">{{ subtitle }}</p>
        <div class="mt-8">
          <slot />
        </div>
        <div v-if="$slots.footer" class="mt-8 border-t border-line pt-6 text-center text-sm text-muted">
          <slot name="footer" />
        </div>
      </section>
    </div>
    <p class="mx-auto mt-6 flex max-w-5xl items-center justify-center gap-2 text-center text-xs text-muted">
      <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
        <path stroke-linecap="round" stroke-linejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 1 0-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 0 0 2.25-2.25v-6.75a2.25 2.25 0 0 0-2.25-2.25H6.75a2.25 2.25 0 0 0-2.25 2.25v6.75a2.25 2.25 0 0 0 2.25 2.25Z" />
      </svg>
      {{ t('auth.secureNote') }}
    </p>
  </div>
</template>
