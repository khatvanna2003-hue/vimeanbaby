<script setup lang="ts">
import type { Address } from '~/types/auth'

const props = defineProps<{ address: Address }>()
const { t } = useI18n()

const provinceLabel = computed(() => {
  const key = provinceKey(props.address.province)
  return key ? t(`provinces.${key}`) : props.address.province
})
</script>

<template>
  <address class="space-y-1 text-sm not-italic text-muted">
    <p class="font-semibold text-ink">{{ address.receiverName }}</p>
    <p>{{ address.phone }}</p>
    <p>{{ address.streetDetail }}</p>
    <p>{{ address.commune }}, {{ address.district }}, {{ provinceLabel }}</p>
    <p v-if="address.note" class="pt-1 text-xs">{{ t('address.note') }}: {{ address.note }}</p>
  </address>
</template>
