<script setup lang="ts">
import type { Address } from '~/types/auth'

const { t, locale } = useI18n()
const localePath = useLocalePath()
const route = useRoute()
const auth = useAuthStore()
const { apiFetch } = useApi()

useSeoMeta({
  title: () => `${t('account.nav.overview')} | ${t('app.name')}`,
})

const showWelcome = ref(route.query.welcome === '1')

const { data: addresses, status, error, refresh } = await useAsyncData(
  'account-overview-addresses',
  () => apiFetch<Address[]>('/me/addresses'),
  { default: () => [] as Address[] },
)

const defaultAddress = computed(() => addresses.value?.find(a => a.defaultAddress) ?? null)

const tiles = computed(() => [
  { to: localePath('/account/orders'), title: t('account.nav.orders'), body: t('account.tiles.orders') },
  { to: localePath('/account/addresses'), title: t('account.nav.addresses'), body: t('account.tiles.addresses', { count: addresses.value?.length ?? 0 }) },
  { to: localePath('/account/security'), title: t('account.nav.security'), body: t('account.tiles.security') },
])
</script>

<template>
  <div class="space-y-6">
    <AlertBanner v-if="showWelcome" variant="success" :message="t('account.welcomeNew')" />

    <section class="relative overflow-hidden rounded-[1.5rem] bg-brand p-6 text-white shadow-soft sm:p-8">
      <div class="absolute -right-10 -top-10 h-40 w-40 rounded-full bg-white/10" aria-hidden="true" />
      <p class="relative text-sm text-white/85">{{ t('account.hello') }}</p>
      <h1 class="relative mt-1 text-2xl font-bold sm:text-3xl">{{ auth.user?.fullName }}</h1>
      <p class="relative mt-3 text-sm text-white/85">
        {{ t('account.memberSince', { date: formatDate(auth.user?.createdAt, locale) }) }}
      </p>
    </section>

    <div class="grid gap-4 sm:grid-cols-3">
      <NuxtLink
        v-for="tile in tiles"
        :key="tile.to"
        :to="tile.to"
        class="group rounded-[1.5rem] border border-line bg-white p-5 shadow-soft transition hover:-translate-y-0.5 hover:border-brand-light"
      >
        <p class="font-semibold text-ink">{{ tile.title }}</p>
        <p class="mt-1 text-sm text-muted">{{ tile.body }}</p>
        <span class="mt-4 inline-flex text-sm font-semibold text-brand-light group-hover:text-brand">{{ t('account.manage') }} →</span>
      </NuxtLink>
    </div>

    <div class="grid gap-6 xl:grid-cols-2">
      <AccountSection :title="t('account.personalInfo')">
        <template #actions>
          <NuxtLink :to="localePath('/account/profile')" class="text-sm font-semibold text-brand-light hover:text-brand">
            {{ t('account.edit') }}
          </NuxtLink>
        </template>
        <dl class="space-y-4 text-sm">
          <div class="flex justify-between gap-4">
            <dt class="text-muted">{{ t('auth.email') }}</dt>
            <dd class="truncate font-medium text-ink">{{ auth.user?.email }}</dd>
          </div>
          <div class="flex justify-between gap-4">
            <dt class="text-muted">{{ t('auth.phone') }}</dt>
            <dd class="font-medium text-ink">{{ auth.user?.phone }}</dd>
          </div>
          <div class="flex justify-between gap-4">
            <dt class="text-muted">{{ t('account.dateOfBirth') }}</dt>
            <dd class="font-medium text-ink">{{ formatDate(auth.user?.dateOfBirth, locale) || '—' }}</dd>
          </div>
        </dl>
      </AccountSection>

      <AccountSection :title="t('account.defaultAddress')">
        <template #actions>
          <NuxtLink :to="localePath('/account/addresses')" class="text-sm font-semibold text-brand-light hover:text-brand">
            {{ t('account.manage') }}
          </NuxtLink>
        </template>
        <div v-if="status === 'pending'" class="space-y-2" aria-busy="true">
          <div class="h-4 w-1/2 animate-pulse rounded bg-line" />
          <div class="h-4 w-3/4 animate-pulse rounded bg-line" />
        </div>
        <div v-else-if="error" class="space-y-3">
          <AlertBanner variant="error" :message="t('errors.GENERIC')" />
          <button type="button" class="btn-ghost" @click="refresh()">{{ t('common.retry') }}</button>
        </div>
        <AddressSummary v-else-if="defaultAddress" :address="defaultAddress" />
        <div v-else class="text-sm text-muted">
          <p>{{ t('account.noAddress') }}</p>
          <NuxtLink :to="localePath('/account/addresses')" class="btn-ghost mt-4">{{ t('account.addAddress') }}</NuxtLink>
        </div>
      </AccountSection>
    </div>
  </div>
</template>
