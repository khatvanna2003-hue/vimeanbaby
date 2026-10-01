<script setup lang="ts">
import type { Address, AddressPayload } from '~/types/auth'

const { t } = useI18n()
const { apiFetch } = useApi()
const errorMessage = useApiErrorMessage()

useSeoMeta({
  title: () => `${t('account.nav.addresses')} | ${t('app.name')}`,
})

const { data: addresses, status, error, refresh } = await useAsyncData(
  'account-addresses',
  () => apiFetch<Address[]>('/me/addresses'),
  { default: () => [] as Address[] },
)

const editing = ref<Address | null>(null)
const formOpen = ref(false)
const saving = ref(false)
const formError = ref<string | null>(null)
const busyId = ref<number | null>(null)
const actionError = ref<string | null>(null)
const notice = ref<string | null>(null)

function openForm(address: Address | null = null) {
  editing.value = address
  formError.value = null
  notice.value = null
  formOpen.value = true
}

function closeForm() {
  formOpen.value = false
  editing.value = null
}

async function save(payload: AddressPayload) {
  saving.value = true
  formError.value = null
  try {
    if (editing.value) {
      await apiFetch<Address>(`/me/addresses/${editing.value.id}`, { method: 'PUT', body: payload })
    }
    else {
      await apiFetch<Address>('/me/addresses', { method: 'POST', body: payload })
    }
    notice.value = t('address.saved')
    closeForm()
    await refresh()
  }
  catch (err) {
    formError.value = errorMessage(err)
  }
  finally {
    saving.value = false
  }
}

async function runAction(id: number, action: () => Promise<unknown>, successMessage: string) {
  busyId.value = id
  actionError.value = null
  notice.value = null
  try {
    await action()
    notice.value = successMessage
    await refresh()
  }
  catch (err) {
    actionError.value = errorMessage(err)
  }
  finally {
    busyId.value = null
  }
}

function makeDefault(address: Address) {
  return runAction(address.id, () => apiFetch(`/me/addresses/${address.id}/default`, { method: 'PATCH' }), t('address.defaultUpdated'))
}

function remove(address: Address) {
  if (!window.confirm(t('address.confirmDelete'))) return
  return runAction(address.id, () => apiFetch(`/me/addresses/${address.id}`, { method: 'DELETE' }), t('address.deleted'))
}
</script>

<template>
  <AccountSection :title="t('account.nav.addresses')" :description="t('address.subtitle')">
    <template #actions>
      <button v-if="!formOpen && addresses.length" type="button" class="btn-primary" @click="openForm()">
        + {{ t('account.addAddress') }}
      </button>
    </template>

    <div class="space-y-4">
      <AlertBanner v-if="notice" variant="success" :message="notice" />
      <AlertBanner v-if="actionError" variant="error" :message="actionError" />

      <div v-if="formOpen" class="rounded-2xl border border-line bg-cream/60 p-5 sm:p-6">
        <h3 class="mb-5 font-bold text-ink">{{ editing ? t('address.editTitle') : t('address.newTitle') }}</h3>
        <AddressForm
          :key="editing?.id ?? 'new'"
          :address="editing"
          :loading="saving"
          :server-error="formError"
          @submit="save"
          @cancel="closeForm"
        />
      </div>

      <div v-if="status === 'pending' && !addresses.length" class="grid gap-4 md:grid-cols-2" aria-busy="true">
        <div v-for="i in 2" :key="i" class="h-40 animate-pulse rounded-2xl bg-line/60" />
      </div>

      <div v-else-if="error" class="space-y-3">
        <AlertBanner variant="error" :message="t('errors.GENERIC')" />
        <button type="button" class="btn-ghost" @click="refresh()">{{ t('common.retry') }}</button>
      </div>

      <div v-else-if="!addresses.length && !formOpen" class="flex flex-col items-center px-4 py-10 text-center">
        <span class="flex h-16 w-16 items-center justify-center rounded-full bg-brand-tint text-brand">
          <svg class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.6" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 10.5a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" />
            <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 10.5c0 7.14-7.5 11.25-7.5 11.25S4.5 17.64 4.5 10.5a7.5 7.5 0 1 1 15 0Z" />
          </svg>
        </span>
        <h3 class="mt-5 text-lg font-bold text-ink">{{ t('address.emptyTitle') }}</h3>
        <p class="mt-2 max-w-sm text-sm text-muted">{{ t('address.emptyBody') }}</p>
        <button type="button" class="btn-primary mt-6" @click="openForm()">+ {{ t('account.addAddress') }}</button>
      </div>

      <ul v-else class="grid gap-4 md:grid-cols-2">
        <li
          v-for="address in addresses"
          :key="address.id"
          class="flex flex-col rounded-2xl border p-5 transition"
          :class="address.defaultAddress ? 'border-brand-light bg-brand-tint/30' : 'border-line bg-white'"
        >
          <div class="mb-3 flex items-center justify-between gap-2">
            <span v-if="address.defaultAddress" class="badge-sale">{{ t('address.default') }}</span>
            <span v-else />
          </div>
          <AddressSummary :address="address" class="flex-1" />
          <div class="mt-4 flex flex-wrap gap-x-4 gap-y-2 border-t border-line pt-4 text-sm font-semibold">
            <button type="button" class="text-brand-light hover:text-brand" :disabled="busyId === address.id" @click="openForm(address)">
              {{ t('account.edit') }}
            </button>
            <button
              v-if="!address.defaultAddress"
              type="button"
              class="text-brand-light hover:text-brand disabled:opacity-50"
              :disabled="busyId === address.id"
              @click="makeDefault(address)"
            >
              {{ t('address.makeDefault') }}
            </button>
            <button
              type="button"
              class="ml-auto text-muted hover:text-danger disabled:opacity-50"
              :disabled="busyId === address.id"
              @click="remove(address)"
            >
              {{ t('address.delete') }}
            </button>
          </div>
        </li>
      </ul>
    </div>
  </AccountSection>
</template>
