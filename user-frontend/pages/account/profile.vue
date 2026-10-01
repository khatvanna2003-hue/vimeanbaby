<script setup lang="ts">
import type { Gender, UserProfile } from '~/types/auth'

const { t } = useI18n()
const auth = useAuthStore()
const { apiFetch } = useApi()
const errorMessage = useApiErrorMessage()

useSeoMeta({
  title: () => `${t('account.nav.profile')} | ${t('app.name')}`,
})

const form = reactive({
  fullName: auth.user?.fullName ?? '',
  email: auth.user?.email ?? '',
  phone: auth.user?.phone ?? '',
  dateOfBirth: auth.user?.dateOfBirth ?? '',
  gender: (auth.user?.gender ?? '') as Gender | '',
})
const submitted = ref(false)
const loading = ref(false)
const serverError = ref<string | null>(null)
const success = ref(false)
const today = new Date().toISOString().slice(0, 10)

const genders = computed(() => [
  { value: '', label: t('account.genderUnset') },
  { value: 'FEMALE', label: t('account.genders.FEMALE') },
  { value: 'MALE', label: t('account.genders.MALE') },
  { value: 'OTHER', label: t('account.genders.OTHER') },
])

const errors = computed(() => {
  if (!submitted.value) return { fullName: null, email: null, phone: null, dateOfBirth: null }
  return {
    fullName: form.fullName.trim().length < 2 ? t('auth.validation.fullName') : null,
    email: !isEmail(form.email) ? t('auth.validation.email') : null,
    phone: !isCambodianPhone(form.phone) ? t('auth.validation.phone') : null,
    dateOfBirth: form.dateOfBirth && form.dateOfBirth >= today ? t('account.validation.dateOfBirth') : null,
  }
})

const dirty = computed(() => {
  const user = auth.user
  if (!user) return false
  return form.fullName !== user.fullName
    || form.email !== user.email
    || form.phone !== user.phone
    || form.dateOfBirth !== (user.dateOfBirth ?? '')
    || form.gender !== (user.gender ?? '')
})

async function submit() {
  submitted.value = true
  serverError.value = null
  success.value = false
  if (Object.values(errors.value).some(Boolean)) return
  loading.value = true
  try {
    const profile = await apiFetch<UserProfile>('/me', {
      method: 'PUT',
      body: {
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        phone: form.phone.trim(),
        dateOfBirth: form.dateOfBirth || null,
        gender: form.gender || null,
      },
    })
    auth.setUser(profile)
    form.email = profile.email
    form.phone = profile.phone
    success.value = true
    submitted.value = false
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
  <AccountSection :title="t('account.nav.profile')" :description="t('account.profileSubtitle')">
    <form class="space-y-5" novalidate @submit.prevent="submit">
      <AlertBanner v-if="success" variant="success" :message="t('account.profileSaved')" />
      <AlertBanner v-if="serverError" variant="error" :message="serverError" />

      <FormInput
        v-model="form.fullName"
        :label="t('auth.fullName')"
        autocomplete="name"
        :maxlength="150"
        :error="errors.fullName"
        required
      />

      <div class="grid gap-5 sm:grid-cols-2">
        <FormInput
          v-model="form.email"
          type="email"
          inputmode="email"
          :label="t('auth.email')"
          autocomplete="email"
          :maxlength="180"
          :error="errors.email"
          required
        />
        <FormInput
          v-model="form.phone"
          type="tel"
          inputmode="tel"
          :label="t('auth.phone')"
          autocomplete="tel"
          :maxlength="20"
          :error="errors.phone"
          required
        />
      </div>

      <div class="grid gap-5 sm:grid-cols-2">
        <FormInput
          v-model="form.dateOfBirth"
          type="date"
          :label="t('account.dateOfBirth')"
          autocomplete="bday"
          :error="errors.dateOfBirth"
        />
        <div>
          <label for="profile-gender" class="mb-1.5 block text-sm font-semibold text-ink">{{ t('account.gender') }}</label>
          <select
            id="profile-gender"
            v-model="form.gender"
            class="min-h-12 w-full rounded-2xl border border-line bg-white px-4 text-sm text-ink outline-none transition focus:border-brand focus:ring-4 focus:ring-brand/10"
          >
            <option v-for="option in genders" :key="option.value" :value="option.value">{{ option.label }}</option>
          </select>
        </div>
      </div>

      <div class="flex flex-wrap items-center gap-3 border-t border-line pt-5">
        <SubmitButton :loading="loading" :disabled="!dirty">{{ t('account.saveChanges') }}</SubmitButton>
        <p v-if="!dirty && !success" class="text-xs text-muted">{{ t('account.noChanges') }}</p>
      </div>
    </form>
  </AccountSection>
</template>
