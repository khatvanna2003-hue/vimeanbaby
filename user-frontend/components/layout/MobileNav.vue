<script setup lang="ts">
defineProps<{
  open: boolean
  links: Array<{ to: string, label: string }>
}>()

const emit = defineEmits<{ close: [] }>()

const { t } = useI18n()
const localePath = useLocalePath()
const auth = useAuthStore()

async function logout() {
  emit('close')
  auth.logout()
  await navigateTo(localePath('/'))
}
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="fixed inset-0 z-50 lg:hidden">
      <button class="absolute inset-0 bg-ink/40" type="button" :aria-label="t('nav.home')" @click="$emit('close')" />
      <aside class="absolute inset-y-0 left-0 flex w-[86%] max-w-sm flex-col bg-white shadow-soft">
        <div class="flex items-center justify-between border-b border-line px-5 py-4">
          <img src="/logo.svg" :alt="t('app.name')" class="h-10 w-auto" width="166" height="40">
          <button type="button" class="h-10 w-10 rounded-full border border-line" @click="$emit('close')">
            ✕
          </button>
        </div>
        <nav class="flex-1 space-y-1 px-3 py-4">
          <NuxtLink
            v-for="link in links"
            :key="link.label"
            :to="link.to"
            class="block rounded-2xl px-4 py-3 text-sm font-semibold text-ink hover:bg-cream"
            @click="$emit('close')"
          >
            {{ link.label }}
          </NuxtLink>
        </nav>
        <div class="space-y-2 border-t border-line px-5 py-4">
          <template v-if="auth.isLoggedIn">
            <NuxtLink
              :to="localePath('/account')"
              class="flex items-center gap-3 rounded-2xl bg-cream px-3 py-3"
              @click="$emit('close')"
            >
              <span class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-brand text-sm font-bold text-white">
                {{ initials(auth.user?.fullName) }}
              </span>
              <span class="min-w-0">
                <span class="block truncate text-sm font-semibold text-ink">{{ auth.user?.fullName }}</span>
                <span class="block text-xs text-muted">{{ t('nav.account') }}</span>
              </span>
            </NuxtLink>
            <button
              type="button"
              class="w-full rounded-2xl px-4 py-3 text-left text-sm font-semibold text-muted hover:bg-cream hover:text-danger"
              @click="logout"
            >
              {{ t('account.logout') }}
            </button>
          </template>
          <div v-else class="grid grid-cols-2 gap-2">
            <NuxtLink :to="localePath('/login')" class="btn-primary" @click="$emit('close')">{{ t('auth.signIn') }}</NuxtLink>
            <NuxtLink :to="localePath('/register')" class="btn-ghost" @click="$emit('close')">{{ t('auth.register') }}</NuxtLink>
          </div>
        </div>
        <div class="border-t border-line px-5 py-4">
          <p class="rounded-full bg-cream px-3 py-2 text-center text-xs text-muted">
            {{ t('locale.cambodia') }}
          </p>
        </div>
      </aside>
    </div>
  </Teleport>
</template>
