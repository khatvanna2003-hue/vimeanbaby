<script setup lang="ts">
import type { AuthResponse } from '~/types/auth'

const { t, locale } = useI18n()
const localePath = useLocalePath()
const auth = useAuthStore()
const { apiFetch } = useApi()
const errorMessage = useApiErrorMessage()

useSeoMeta({
  title: () => `${t('account.nav.security')} | ${t('app.name')}`,
})

const form = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })
const submitted = ref(false)
const loading = ref(false)
const serverError = ref<string | null>(null)
const success = ref(false)

const errors = computed(() => {
  if (!submitted.value) return { currentPassword: null, newPassword: null, confirmPassword: null }
  return {
    currentPassword: !form.currentPassword ? t('auth.validation.passwordRequired') : null,
    newPassword: !isStrongEnoughPassword(form.newPassword) ? t('auth.validation.password') : null,
    confirmPassword: form.confirmPassword !== form.newPassword ? t('auth.validation.confirmPassword') : null,
  }
})

async function submit() {
  submitted.value = true
  serverError.value = null
  success.value = false
  if (Object.values(errors.value).some(Boolean)) return
  loading.value = true
  try {
    const session = await apiFetch<AuthResponse>('/me/password', {
      method: 'PUT',
      body: { currentPassword: form.currentPassword, newPassword: form.newPassword },
    })
    auth.setSession(session)
    Object.assign(form, { currentPassword: '', newPassword: '', confirmPassword: '' })
    submitted.value = false
    success.value = true
  }
  catch (error) {
    serverError.value = errorMessage(error)
  }
  finally {
    loading.value = false
  }
}

const signingOutAll = ref(false)
const signOutError = ref<string | null>(null)

async function signOutEverywhere() {
  if (!window.confirm(t('account.security.confirmSignOutAll'))) return
  signingOutAll.value = true
  signOutError.value = null
  try {
    await apiFetch<null>('/me/logout-all', { method: 'POST' })
    auth.logout()
    await navigateTo(localePath('/login'))
  }
  catch (error) {
    signOutError.value = errorMessage(error)
  }
  finally {
    signingOutAll.value = false
  }
}
</script>

<template>
  <div class="space-y-6">
    <AccountSection :title="t('account.security.changePassword')" :description="t('account.security.changePasswordHint')">
      <form class="max-w-lg space-y-5" novalidate @submit.prevent="submit">
        <AlertBanner v-if="success" variant="success" :message="t('account.security.passwordChanged')" />
        <AlertBanner v-if="serverError" variant="error" :message="serverError" />

        <FormInput
          v-model="form.currentPassword"
          type="password"
          :label="t('account.security.currentPassword')"
          autocomplete="current-password"
          :error="errors.currentPassword"
          required
        />
        <div class="space-y-3">
          <FormInput
            v-model="form.newPassword"
            type="password"
            :label="t('account.security.newPassword')"
            autocomplete="new-password"
            :maxlength="72"
            :error="errors.newPassword"
            required
          />
          <PasswordStrength :password="form.newPassword" />
        </div>
        <FormInput
          v-model="form.confirmPassword"
          type="password"
          :label="t('auth.confirmPassword')"
          autocomplete="new-password"
          :maxlength="72"
          :error="errors.confirmPassword"
          required
        />
        <SubmitButton :loading="loading">{{ t('account.security.updatePassword') }}</SubmitButton>
      </form>
    </AccountSection>

    <AccountSection :title="t('account.security.sessions')" :description="t('account.security.sessionsHint')">
      <div class="flex flex-wrap items-center justify-between gap-4">
        <div class="text-sm">
          <p class="font-semibold text-ink">{{ t('account.security.lastSignIn') }}</p>
          <p class="text-muted">{{ formatDate(auth.user?.lastLoginAt, locale) || '—' }}</p>
        </div>
        <button
          type="button"
          class="btn-ghost gap-2 disabled:opacity-60"
          :disabled="signingOutAll"
          @click="signOutEverywhere"
        >
          {{ t('account.security.signOutAll') }}
        </button>
      </div>
      <AlertBanner v-if="signOutError" class="mt-4" variant="error" :message="signOutError" />
    </AccountSection>
  </div>
</template>
