<script setup lang="ts">
definePageMeta({ middleware: 'guest' })

const { t } = useI18n()
const localePath = useLocalePath()
const route = useRoute()
const auth = useAuthStore()
const errorMessage = useApiErrorMessage()

useSeoMeta({
  title: () => `${t('auth.loginTitle')} | ${t('app.name')}`,
  description: () => t('auth.loginSubtitle'),
  ogTitle: () => `${t('auth.loginTitle')} | ${t('app.name')}`,
  ogDescription: () => t('auth.loginSubtitle'),
  robots: 'noindex, follow',
})

const form = reactive({ identifier: '', password: '' })
const touched = reactive({ identifier: false, password: false })
const submitted = ref(false)
const loading = ref(false)
const serverError = ref<string | null>(null)

const errors = computed(() => {
  const show = (field: keyof typeof touched) => touched[field] || submitted.value
  const identifier = form.identifier.trim()
  return {
    identifier: !show('identifier')
      ? null
      : !identifier
          ? t('auth.validation.identifierRequired')
          : identifier.includes('@') ? (isEmail(identifier) ? null : t('auth.validation.email'))
            : (isCambodianPhone(identifier) ? null : t('auth.validation.phone')),
    password: show('password') && !form.password ? t('auth.validation.passwordRequired') : null,
  }
})

const redirectTo = computed(() => safeRedirect(route.query.redirect, localePath('/account')))

async function submit() {
  submitted.value = true
  serverError.value = null
  if (errors.value.identifier || errors.value.password) return
  loading.value = true
  try {
    await auth.login({ identifier: form.identifier.trim(), password: form.password })
    await navigateTo(redirectTo.value, { replace: true })
  }
  catch (error) {
    serverError.value = errorMessage(error)
    form.password = ''
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthShell :title="t('auth.loginTitle')" :subtitle="t('auth.loginSubtitle')">
    <form class="space-y-5" novalidate @submit.prevent="submit">
      <AlertBanner v-if="serverError" variant="error" :message="serverError" />

      <FormInput
        v-model="form.identifier"
        :label="t('auth.identifier')"
        :placeholder="t('auth.identifierPlaceholder')"
        autocomplete="username"
        :error="errors.identifier"
        required
        @blur="touched.identifier = true"
      />

      <div>
        <FormInput
          v-model="form.password"
          type="password"
          :label="t('auth.password')"
          autocomplete="current-password"
          :error="errors.password"
          required
          @blur="touched.password = true"
        />
        <div class="mt-2 text-right">
          <NuxtLink :to="localePath('/contact')" class="text-xs font-semibold text-brand-light hover:text-brand">
            {{ t('auth.forgotPassword') }}
          </NuxtLink>
        </div>
      </div>

      <SubmitButton :loading="loading" block>
        {{ loading ? t('auth.signingIn') : t('auth.signIn') }}
      </SubmitButton>
    </form>

    <template #footer>
      {{ t('auth.noAccount') }}
      <NuxtLink
        :to="localePath({ path: '/register', query: route.query.redirect ? { redirect: String(route.query.redirect) } : undefined })"
        class="font-semibold text-brand-light hover:text-brand"
      >
        {{ t('auth.createAccount') }}
      </NuxtLink>
    </template>
  </AuthShell>
</template>
