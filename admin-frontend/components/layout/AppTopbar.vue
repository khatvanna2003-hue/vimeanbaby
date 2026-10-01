<script setup lang="ts">
const emit = defineEmits<{ menu: [] }>()

const { t } = useI18n()
const auth = useAuthStore()
const router = useRouter()
const menuOpen = ref(false)
const root = ref<HTMLElement | null>(null)

function onDocumentClick(event: MouseEvent) {
  if (root.value && !root.value.contains(event.target as Node)) menuOpen.value = false
}
onMounted(() => document.addEventListener('click', onDocumentClick))
onBeforeUnmount(() => document.removeEventListener('click', onDocumentClick))

async function logout() {
  menuOpen.value = false
  auth.logout()
  await router.push('/login')
}
</script>

<template>
  <header class="sticky top-0 z-30 flex h-16 items-center gap-3 border-b border-line/70 bg-white/90 px-4 backdrop-blur sm:px-6">
    <button type="button" class="btn-icon lg:hidden" :aria-label="t('nav.openMenu')" @click="emit('menu')">
      <AppIcon name="menu" />
    </button>
    <div class="ml-auto flex items-center gap-3">
      <LocaleSwitcher />
      <div ref="root" class="relative">
        <button
          type="button"
          class="flex items-center gap-2 rounded-xl px-1.5 py-1 transition hover:bg-surface"
          aria-haspopup="menu"
          :aria-expanded="menuOpen"
          @click="menuOpen = !menuOpen"
        >
          <span class="flex h-9 w-9 items-center justify-center rounded-full bg-brand text-sm font-bold text-white">
            {{ initials(auth.user?.fullName) }}
          </span>
          <span class="hidden text-left sm:block">
            <span class="block max-w-40 truncate text-sm font-semibold text-ink">{{ auth.user?.fullName }}</span>
            <span class="block text-xs text-ink-muted">{{ t('app.roleAdmin') }}</span>
          </span>
        </button>
        <div v-if="menuOpen" class="absolute right-0 top-full mt-2 w-56 overflow-hidden rounded-xl border border-line bg-white shadow-lg" role="menu">
          <div class="border-b border-line/70 px-4 py-3">
            <p class="truncate text-sm font-semibold text-ink">{{ auth.user?.fullName }}</p>
            <p class="truncate text-xs text-ink-muted">{{ auth.user?.email }}</p>
          </div>
          <NuxtLink to="/settings" class="flex items-center gap-2 px-4 py-2.5 text-sm text-ink hover:bg-surface" role="menuitem" @click="menuOpen = false">
            <AppIcon name="settings" :size="18" /> {{ t('nav.settings') }}
          </NuxtLink>
          <button type="button" class="flex w-full items-center gap-2 border-t border-line/70 px-4 py-2.5 text-left text-sm text-ink-muted hover:bg-surface hover:text-danger" role="menuitem" @click="logout">
            <AppIcon name="logout" :size="18" /> {{ t('auth.logout') }}
          </button>
        </div>
      </div>
    </div>
  </header>
</template>
