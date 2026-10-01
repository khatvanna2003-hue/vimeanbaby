<script setup lang="ts">
import type { AdminUser, AuthResponse } from '~/types/admin'

type Profile = AdminUser & { dateOfBirth: string | null, gender: string | null }

const { t, locale } = useI18n()
const auth = useAuthStore()
const { apiFetch } = useApi()
const toast = useToast()
const errorMessage = useApiErrorMessage()

useHead({ title: () => `${t('nav.settings')} | Vimean Baby Admin` })

const { data: profile, error, refresh } = await useAsyncData('admin-me', () => apiFetch<Profile>('/me'))

const profileForm = reactive({ fullName: '', email: '', phone: '' })
watch(profile, (value) => {
  if (value) Object.assign(profileForm, { fullName: value.fullName, email: value.email, phone: value.phone })
}, { immediate: true })

const profileSubmitted = ref(false)
const savingProfile = ref(false)
const profileErrors = computed(() => {
  if (!profileSubmitted.value) return {} as Record<string, string>
  const result: Record<string, string> = {}
  if (profileForm.fullName.trim().length < 2) result.fullName = t('validation.required')
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(profileForm.email.trim())) result.email = t('validation.email')
  if (!/^\+?[0-9 ()-]{8,20}$/.test(profileForm.phone.trim())) result.phone = t('validation.phone')
  return result
})

async function saveProfile() {
  profileSubmitted.value = true
  if (Object.keys(profileErrors.value).length || !profile.value) return
  savingProfile.value = true
  try {
    const updated = await apiFetch<Profile>('/me', {
      method: 'PUT',
      body: {
        fullName: profileForm.fullName.trim(),
        email: profileForm.email.trim(),
        phone: profileForm.phone.trim(),
        dateOfBirth: profile.value.dateOfBirth,
        gender: profile.value.gender,
      },
    })
    profile.value = updated
    auth.setUser(updated)
    profileSubmitted.value = false
    toast.success(t('settings.profileSaved'))
  }
  catch (err) {
    toast.error(errorMessage(err))
  }
  finally {
    savingProfile.value = false
  }
}

const passwordForm = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })
const showPasswords = ref(false)
const passwordSubmitted = ref(false)
const savingPassword = ref(false)
const passwordErrors = computed(() => {
  if (!passwordSubmitted.value) return {} as Record<string, string>
  const result: Record<string, string> = {}
  if (!passwordForm.currentPassword) result.currentPassword = t('validation.required')
  if (!/^(?=.*[A-Za-z])(?=.*\d).{8,72}$/.test(passwordForm.newPassword)) result.newPassword = t('validation.password')
  if (passwordForm.confirmPassword !== passwordForm.newPassword) result.confirmPassword = t('validation.passwordMatch')
  return result
})

async function changePassword() {
  passwordSubmitted.value = true
  if (Object.keys(passwordErrors.value).length) return
  savingPassword.value = true
  try {
    const response = await apiFetch<AuthResponse>('/me/password', {
      method: 'PUT',
      body: { currentPassword: passwordForm.currentPassword, newPassword: passwordForm.newPassword },
    })
    auth.setSession(response)
    Object.assign(passwordForm, { currentPassword: '', newPassword: '', confirmPassword: '' })
    passwordSubmitted.value = false
    toast.success(t('settings.passwordChanged'))
  }
  catch (err) {
    toast.error(errorMessage(err))
  }
  finally {
    savingPassword.value = false
  }
}
</script>

<template>
  <div>
    <PageHeader :title="t('nav.settings')" :description="t('settings.subtitle')" />

    <div v-if="error" class="card p-6">
      <EmptyState icon="alert" :title="t('errors.loadTitle')" :description="errorMessage(error)">
        <button type="button" class="btn-primary" @click="refresh()">{{ t('common.retry') }}</button>
      </EmptyState>
    </div>

    <div v-else class="grid gap-6 xl:grid-cols-3">
      <section class="card p-6">
        <div class="flex items-center gap-4">
          <span class="flex h-14 w-14 items-center justify-center rounded-full bg-brand text-lg font-bold text-white">{{ initials(profile?.fullName ?? auth.user?.fullName ?? '') }}</span>
          <div class="min-w-0">
            <p class="truncate font-bold text-ink">{{ profile?.fullName ?? auth.user?.fullName }}</p>
            <span class="badge-brand mt-1">{{ t('app.roleAdmin') }}</span>
          </div>
        </div>
        <dl class="mt-6 space-y-3 border-t border-line/70 pt-5 text-sm">
          <div class="flex justify-between gap-4">
            <dt class="text-ink-muted">{{ t('customers.joined') }}</dt>
            <dd class="font-medium text-ink">{{ formatDate(profile?.createdAt, locale) }}</dd>
          </div>
          <div class="flex justify-between gap-4">
            <dt class="text-ink-muted">{{ t('customers.lastLogin') }}</dt>
            <dd class="font-medium text-ink">{{ formatDateTime(profile?.lastLoginAt, locale) }}</dd>
          </div>
        </dl>
        <div class="mt-6 border-t border-line/70 pt-5">
          <p class="field-label">{{ t('settings.language') }}</p>
          <LocaleSwitcher />
        </div>
      </section>

      <div class="space-y-6 xl:col-span-2">
        <form class="card p-6" novalidate @submit.prevent="saveProfile">
          <h2 class="font-bold text-ink">{{ t('settings.profile') }}</h2>
          <p class="mt-1 text-sm text-ink-muted">{{ t('settings.profileHint') }}</p>
          <div class="mt-5 grid gap-4 sm:grid-cols-2">
            <div class="sm:col-span-2">
              <label class="field-label" for="s-name">{{ t('settings.fullName') }}</label>
              <input id="s-name" v-model="profileForm.fullName" class="field-input" autocomplete="name" :class="profileErrors.fullName ? 'border-danger' : ''">
              <p v-if="profileErrors.fullName" class="field-error">{{ profileErrors.fullName }}</p>
            </div>
            <div>
              <label class="field-label" for="s-email">{{ t('customers.email') }}</label>
              <input id="s-email" v-model="profileForm.email" type="email" class="field-input" autocomplete="email" :class="profileErrors.email ? 'border-danger' : ''">
              <p v-if="profileErrors.email" class="field-error">{{ profileErrors.email }}</p>
            </div>
            <div>
              <label class="field-label" for="s-phone">{{ t('customers.phone') }}</label>
              <input id="s-phone" v-model="profileForm.phone" type="tel" class="field-input" autocomplete="tel" :class="profileErrors.phone ? 'border-danger' : ''">
              <p v-if="profileErrors.phone" class="field-error">{{ profileErrors.phone }}</p>
            </div>
          </div>
          <div class="mt-6 flex justify-end">
            <button type="submit" class="btn-primary" :disabled="savingProfile || !profile">{{ t('common.saveChanges') }}</button>
          </div>
        </form>

        <form class="card p-6" novalidate @submit.prevent="changePassword">
          <div class="flex items-start justify-between gap-4">
            <div>
              <h2 class="font-bold text-ink">{{ t('settings.password') }}</h2>
              <p class="mt-1 text-sm text-ink-muted">{{ t('settings.passwordHint') }}</p>
            </div>
            <button type="button" class="btn-icon" :aria-label="showPasswords ? t('auth.hidePassword') : t('auth.showPassword')" @click="showPasswords = !showPasswords">
              <AppIcon :name="showPasswords ? 'eyeOff' : 'eye'" :size="18" />
            </button>
          </div>
          <div class="mt-5 grid gap-4 sm:grid-cols-2">
            <div class="sm:col-span-2">
              <label class="field-label" for="s-current">{{ t('settings.currentPassword') }}</label>
              <input id="s-current" v-model="passwordForm.currentPassword" :type="showPasswords ? 'text' : 'password'" class="field-input" autocomplete="current-password" :class="passwordErrors.currentPassword ? 'border-danger' : ''">
              <p v-if="passwordErrors.currentPassword" class="field-error">{{ passwordErrors.currentPassword }}</p>
            </div>
            <div>
              <label class="field-label" for="s-new">{{ t('settings.newPassword') }}</label>
              <input id="s-new" v-model="passwordForm.newPassword" :type="showPasswords ? 'text' : 'password'" class="field-input" autocomplete="new-password" :class="passwordErrors.newPassword ? 'border-danger' : ''">
              <p v-if="passwordErrors.newPassword" class="field-error">{{ passwordErrors.newPassword }}</p>
            </div>
            <div>
              <label class="field-label" for="s-confirm">{{ t('settings.confirmPassword') }}</label>
              <input id="s-confirm" v-model="passwordForm.confirmPassword" :type="showPasswords ? 'text' : 'password'" class="field-input" autocomplete="new-password" :class="passwordErrors.confirmPassword ? 'border-danger' : ''">
              <p v-if="passwordErrors.confirmPassword" class="field-error">{{ passwordErrors.confirmPassword }}</p>
            </div>
          </div>
          <p class="mt-3 flex items-center gap-2 text-xs text-ink-muted">
            <AppIcon name="lock" :size="14" /> {{ t('settings.signOutOthers') }}
          </p>
          <div class="mt-6 flex justify-end">
            <button type="submit" class="btn-primary" :disabled="savingPassword">{{ t('settings.updatePassword') }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
