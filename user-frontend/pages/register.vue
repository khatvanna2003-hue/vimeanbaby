<script setup lang="ts">
definePageMeta({ middleware: 'guest' })

const { t } = useI18n()
const localePath = useLocalePath()
const route = useRoute()
const auth = useAuthStore()
const errorMessage = useApiErrorMessage()

useSeoMeta({
  title: () => `${t('auth.registerTitle')} | ${t('app.name')}`,
  description: () => t('auth.registerSubtitle'),
  ogTitle: () => `${t('auth.registerTitle')} | ${t('app.name')}`,
  ogDescription: () => t('auth.registerSubtitle'),
  robots: 'noindex, follow',
})

const form = reactive({
  fullName: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  acceptTerms: false,
})
type Field = 'fullName' | 'email' | 'phone' | 'password' | 'confirmPassword' | 'acceptTerms'
const touched = reactive<Record<Field, boolean>>({
  fullName: false,
  email: false,
  phone: false,
  password: false,
  confirmPassword: false,
  acceptTerms: false,
})
const submitted = ref(false)
const loading = ref(false)
const serverError = ref<string | null>(null)

const errors = computed<Record<Field, string | null>>(() => {
  const show = (field: Field) => touched[field] || submitted.value
  return {
    fullName: show('fullName') && form.fullName.trim().length < 2 ? t('auth.validation.fullName') : null,
    email: show('email') && !isEmail(form.email) ? t('auth.validation.email') : null,
    phone: show('phone') && !isCambodianPhone(form.phone) ? t('auth.validation.phone') : null,
    password: show('password') && !isStrongEnoughPassword(form.password) ? t('auth.validation.password') : null,
    confirmPassword: show('confirmPassword') && form.confirmPassword !== form.password ? t('auth.validation.confirmPassword') : null,
    acceptTerms: show('acceptTerms') && !form.acceptTerms ? t('auth.validation.terms') : null,
  }
})

const redirectTo = computed(() => safeRedirect(route.query.redirect, localePath('/account')))

async function submit() {
  submitted.value = true
  serverError.value = null
  if (Object.values(errors.value).some(Boolean)) return
  loading.value = true
  try {
    await auth.register({
      fullName: form.fullName.trim(),
      email: form.email.trim(),
      phone: form.phone.trim(),
      password: form.password,
    })
    await navigateTo({ path: redirectTo.value, query: { welcome: '1' } }, { replace: true })
  }
  catch (error) {
    serverError.value = errorMessage(error)
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthShell :title="t('auth.registerTitle')" :subtitle="t('auth.registerSubtitle')">
    <form class="space-y-5" novalidate @submit.prevent="submit">
      <AlertBanner v-if="serverError" variant="error" :message="serverError" />

      <FormInput
        v-model="form.fullName"
        :label="t('auth.fullName')"
        :placeholder="t('auth.fullNamePlaceholder')"
        autocomplete="name"
        :maxlength="150"
        :error="errors.fullName"
        required
        @blur="touched.fullName = true"
      />

      <div class="grid gap-5 sm:grid-cols-2">
        <FormInput
          v-model="form.email"
          type="email"
          inputmode="email"
          :label="t('auth.email')"
          placeholder="you@example.com"
          autocomplete="email"
          :maxlength="180"
          :error="errors.email"
          required
          @blur="touched.email = true"
        />
        <FormInput
          v-model="form.phone"
          type="tel"
          inputmode="tel"
          :label="t('auth.phone')"
          placeholder="012 345 678"
          autocomplete="tel"
          :maxlength="20"
          :error="errors.phone"
          :hint="t('auth.phoneHint')"
          required
          @blur="touched.phone = true"
        />
      </div>

      <div class="space-y-3">
        <FormInput
          v-model="form.password"
          type="password"
          :label="t('auth.password')"
          autocomplete="new-password"
          :maxlength="72"
          :error="errors.password"
          required
          @blur="touched.password = true"
        />
        <PasswordStrength :password="form.password" />
      </div>

      <FormInput
        v-model="form.confirmPassword"
        type="password"
        :label="t('auth.confirmPassword')"
        autocomplete="new-password"
        :maxlength="72"
        :error="errors.confirmPassword"
        required
        @blur="touched.confirmPassword = true"
      />

      <div>
        <label class="flex cursor-pointer items-start gap-3 text-sm text-muted">
          <input
            v-model="form.acceptTerms"
            type="checkbox"
            class="mt-0.5 h-5 w-5 shrink-0 rounded border-line accent-brand"
            @change="touched.acceptTerms = true"
          >
          <span>{{ t('auth.acceptTerms') }}</span>
        </label>
        <p v-if="errors.acceptTerms" class="mt-1.5 text-xs font-medium text-danger" role="alert">
          {{ errors.acceptTerms }}
        </p>
      </div>

      <SubmitButton :loading="loading" block>
        {{ loading ? t('auth.creatingAccount') : t('auth.createAccount') }}
      </SubmitButton>
    </form>

    <template #footer>
      {{ t('auth.haveAccount') }}
      <NuxtLink
        :to="localePath({ path: '/login', query: route.query.redirect ? { redirect: String(route.query.redirect) } : undefined })"
        class="font-semibold text-brand-light hover:text-brand"
      >
        {{ t('auth.signIn') }}
      </NuxtLink>
    </template>
  </AuthShell>
</template>
