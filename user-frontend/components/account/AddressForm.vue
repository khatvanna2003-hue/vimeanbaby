<script setup lang="ts">
import type { Address, AddressPayload } from '~/types/auth'

const props = defineProps<{
  address: Address | null
  loading: boolean
  serverError: string | null
}>()

const emit = defineEmits<{
  submit: [payload: AddressPayload]
  cancel: []
}>()

const { t } = useI18n()
const auth = useAuthStore()

const form = reactive<AddressPayload>({
  receiverName: props.address?.receiverName ?? auth.user?.fullName ?? '',
  phone: props.address?.phone ?? auth.user?.phone ?? '',
  province: props.address?.province ?? 'Phnom Penh',
  district: props.address?.district ?? '',
  commune: props.address?.commune ?? '',
  streetDetail: props.address?.streetDetail ?? '',
  note: props.address?.note ?? '',
  defaultAddress: props.address?.defaultAddress ?? false,
})
const submitted = ref(false)

const errors = computed(() => {
  const required = (value: string) => (submitted.value && !value.trim() ? t('address.validation.required') : null)
  return {
    receiverName: required(form.receiverName),
    phone: submitted.value && !isCambodianPhone(form.phone) ? t('auth.validation.phone') : null,
    district: required(form.district),
    commune: required(form.commune),
    streetDetail: required(form.streetDetail),
  }
})

function submit() {
  submitted.value = true
  if (Object.values(errors.value).some(Boolean)) return
  emit('submit', {
    ...form,
    receiverName: form.receiverName.trim(),
    phone: form.phone.trim(),
    district: form.district.trim(),
    commune: form.commune.trim(),
    streetDetail: form.streetDetail.trim(),
    note: form.note?.trim() || null,
  })
}
</script>

<template>
  <form class="space-y-5" novalidate @submit.prevent="submit">
    <AlertBanner v-if="serverError" variant="error" :message="serverError" />

    <div class="grid gap-5 sm:grid-cols-2">
      <FormInput
        v-model="form.receiverName"
        :label="t('address.receiverName')"
        autocomplete="name"
        :maxlength="150"
        :error="errors.receiverName"
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

    <div>
      <label for="address-province" class="mb-1.5 block text-sm font-semibold text-ink">
        {{ t('address.province') }} <span class="text-brand" aria-hidden="true">*</span>
      </label>
      <select
        id="address-province"
        v-model="form.province"
        class="min-h-12 w-full rounded-2xl border border-line bg-white px-4 text-sm text-ink outline-none transition focus:border-brand focus:ring-4 focus:ring-brand/10"
      >
        <option v-for="province in PROVINCES" :key="province.key" :value="province.value">
          {{ t(`provinces.${province.key}`) }}
        </option>
      </select>
    </div>

    <div class="grid gap-5 sm:grid-cols-2">
      <FormInput v-model="form.district" :label="t('address.district')" :maxlength="100" :error="errors.district" required />
      <FormInput v-model="form.commune" :label="t('address.commune')" :maxlength="100" :error="errors.commune" required />
    </div>

    <FormInput
      v-model="form.streetDetail"
      :label="t('address.streetDetail')"
      :placeholder="t('address.streetPlaceholder')"
      autocomplete="street-address"
      :maxlength="255"
      :error="errors.streetDetail"
      required
    />

    <div>
      <label for="address-note" class="mb-1.5 block text-sm font-semibold text-ink">{{ t('address.note') }}</label>
      <textarea
        id="address-note"
        v-model="form.note"
        rows="2"
        maxlength="500"
        :placeholder="t('address.notePlaceholder')"
        class="w-full rounded-2xl border border-line bg-white px-4 py-3 text-sm text-ink outline-none transition placeholder:text-muted/70 focus:border-brand focus:ring-4 focus:ring-brand/10"
      />
    </div>

    <label class="flex cursor-pointer items-center gap-3 text-sm text-ink">
      <input v-model="form.defaultAddress" type="checkbox" class="h-5 w-5 rounded border-line accent-brand">
      {{ t('address.setDefault') }}
    </label>

    <div class="flex flex-wrap gap-3 border-t border-line pt-5">
      <SubmitButton :loading="loading">{{ t('address.save') }}</SubmitButton>
      <button type="button" class="btn-ghost" @click="emit('cancel')">{{ t('common.cancel') }}</button>
    </div>
  </form>
</template>
