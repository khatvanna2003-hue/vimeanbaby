<script setup lang="ts">
definePageMeta({ layout: false })

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const errorMessage = useApiErrorMessage()

useHead({ title: () => `${t('auth.title')} | Vimean Baby Admin` })

const form = reactive({ identifier: '', password: '' })
const showPassword = ref(false)
const submitted = ref(false)
const loading = ref(false)
const serverError = ref<string | null>(null)

const errors = computed(() => ({
  identifier: submitted.value && !form.identifier.trim() ? t('auth.validation.identifier') : null,
  password: submitted.value && !form.password ? t('auth.validation.password') : null,
}))

function redirectTarget() {
  const value = route.query.redirect
  return typeof value === 'string' && value.startsWith('/') && !value.startsWith('//') ? value : '/'
}

async function submit() {
  submitted.value = true
  serverError.value = null
  if (errors.value.identifier || errors.value.password) return
  loading.value = true
  try {
    await auth.login(form.identifier.trim(), form.password)
    await router.replace(redirectTarget())
  }
  catch (error) {
    serverError.value = errorMessage(error)
    form.password = ''
  }
  finally {
    loading.value = false
  }
}

const highlights = computed(() => [
  t('auth.highlights.catalog'),
  t('auth.highlights.inventory'),
  t('auth.highlights.customers'),
])
</script>

<template>
  <NuxtLayout name="auth">
    <template #aside>
      <h2 class="text-3xl font-bold leading-tight">{{ t('auth.asideTitle') }}</h2>
      <p class="mt-3 text-white/85">{{ t('auth.asideBody') }}</p>
      <ul class="mt-8 space-y-3">
        <li v-for="item in highlights" :key="item" class="flex items-center gap-3 text-sm font-medium">
          <span class="flex h-7 w-7 items-center justify-center rounded-full bg-white/15">
            <AppIcon name="check" :size="16" />
          </span>
          {{ item }}
        </li>
      </ul>
    </template>

    <div class="w-full max-w-md">
      <img src="/logo.svg" alt="Vimean Baby" class="mb-8 h-10 w-auto lg:hidden" width="166" height="40">
      <div class="card p-6 sm:p-8">
        <div class="mb-6 flex items-center justify-between gap-4">
          <div>
            <h1 class="text-2xl font-bold text-ink">{{ t('auth.title') }}</h1>
            <p class="mt-1 text-sm text-ink-muted">{{ t('auth.subtitle') }}</p>
          </div>
          <LocaleSwitcher />
        </div>

        <form class="space-y-5" novalidate @submit.prevent="submit">
          <div v-if="serverError" class="flex items-start gap-2 rounded-xl border border-danger/30 bg-danger/5 px-3 py-2.5 text-sm text-ink" role="alert">
            <AppIcon name="alert" :size="18" class="mt-0.5 text-danger" />
            {{ serverError }}
          </div>

          <div>
            <label for="identifier" class="field-label">{{ t('auth.identifier') }}</label>
            <input
              id="identifier"
              v-model="form.identifier"
              class="field-input min-h-11"
              :class="errors.identifier ? 'border-danger' : ''"
              autocomplete="username"
              placeholder="admin@vimeanbaby.com"
              :aria-invalid="!!errors.identifier"
            >
            <p v-if="errors.identifier" class="field-error">{{ errors.identifier }}</p>
          </div>

          <div>
            <label for="password" class="field-label">{{ t('auth.password') }}</label>
            <div class="relative">
              <input
                id="password"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                class="field-input min-h-11 pr-11"
                :class="errors.password ? 'border-danger' : ''"
                autocomplete="current-password"
                :aria-invalid="!!errors.password"
              >
              <button
                type="button"
                class="absolute inset-y-0 right-0 flex w-11 items-center justify-center text-ink-muted hover:text-ink"
                :aria-label="showPassword ? t('auth.hidePassword') : t('auth.showPassword')"
                @click="showPassword = !showPassword"
              >
                <AppIcon :name="showPassword ? 'eyeOff' : 'eye'" :size="18" />
              </button>
            </div>
            <p v-if="errors.password" class="field-error">{{ errors.password }}</p>
          </div>

          <button type="submit" class="btn-primary min-h-11 w-full" :disabled="loading">
            <span v-if="loading" class="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" aria-hidden="true" />
            {{ loading ? t('auth.signingIn') : t('auth.signIn') }}
          </button>
        </form>
      </div>
      <p class="mt-6 flex items-center justify-center gap-2 text-xs text-ink-muted">
        <AppIcon name="lock" :size="14" />
        {{ t('auth.restricted') }}
      </p>
    </div>
  </NuxtLayout>
</template>
