<script setup lang="ts">
import type { CustomerDetail } from '~/types/admin'

const { t, locale } = useI18n()
const route = useRoute()
const { apiFetch } = useApi()
const toast = useToast()
const { confirm } = useConfirm()
const errorMessage = useApiErrorMessage()

const id = computed(() => String(route.params.id))
const { data: customer, status, error, refresh } = await useAsyncData(
  () => `admin-customer-${id.value}`,
  () => apiFetch<CustomerDetail>(`/admin/customers/${id.value}`),
)

useHead({ title: () => `${customer.value?.fullName ?? t('nav.customers')} | Vimean Baby Admin` })

const updating = ref(false)

async function toggleStatus() {
  if (!customer.value) return
  const disabling = customer.value.active
  const ok = await confirm({
    title: disabling ? t('customers.disableTitle') : t('customers.enableTitle'),
    message: disabling
      ? t('customers.disableMessage', { name: customer.value.fullName })
      : t('customers.enableMessage', { name: customer.value.fullName }),
    confirmLabel: disabling ? t('customers.disable') : t('customers.enable'),
    danger: disabling,
  })
  if (!ok) return
  updating.value = true
  try {
    customer.value = await apiFetch<CustomerDetail>(`/admin/customers/${id.value}/status`, {
      method: 'PATCH',
      body: { active: !disabling },
    })
    toast.success(disabling ? t('customers.disabledToast') : t('customers.enabledToast'))
  }
  catch (err) {
    toast.error(errorMessage(err))
  }
  finally {
    updating.value = false
  }
}
</script>

<template>
  <div>
    <PageHeader :title="customer?.fullName ?? t('nav.customers')">
      <template #breadcrumb>
        <NuxtLink to="/customers" class="mb-1 inline-flex items-center gap-1 text-sm text-ink-muted hover:text-ink">
          <AppIcon name="chevronLeft" :size="16" /> {{ t('nav.customers') }}
        </NuxtLink>
      </template>
      <template v-if="customer" #actions>
        <button type="button" :class="customer.active ? 'btn-danger' : 'btn-primary'" :disabled="updating" @click="toggleStatus">
          {{ customer.active ? t('customers.disable') : t('customers.enable') }}
        </button>
      </template>
    </PageHeader>

    <div v-if="error" class="card p-6">
      <EmptyState icon="alert" :title="t('errors.loadTitle')" :description="errorMessage(error)">
        <NuxtLink to="/customers" class="btn-secondary">{{ t('nav.customers') }}</NuxtLink>
      </EmptyState>
    </div>
    <div v-else-if="status === 'pending' || !customer" class="grid gap-6 lg:grid-cols-3" aria-busy="true">
      <div class="card h-64 animate-pulse" />
      <div class="card h-64 animate-pulse lg:col-span-2" />
    </div>
    <div v-else class="grid gap-6 lg:grid-cols-3">
      <section class="card p-6">
        <div class="flex flex-col items-center text-center">
          <span class="flex h-20 w-20 items-center justify-center rounded-full bg-brand text-2xl font-bold text-white">{{ initials(customer.fullName) }}</span>
          <h2 class="mt-4 text-lg font-bold text-ink">{{ customer.fullName }}</h2>
          <span class="mt-2" :class="customer.active ? 'badge-success' : 'badge-danger'">{{ customer.active ? t('common.active') : t('customers.disabled') }}</span>
        </div>
        <dl class="mt-6 space-y-4 border-t border-line/70 pt-5 text-sm">
          <div>
            <dt class="text-xs font-semibold uppercase tracking-wide text-ink-muted">{{ t('customers.email') }}</dt>
            <dd class="mt-0.5 break-all"><a :href="`mailto:${customer.email}`" class="text-ink hover:text-brand">{{ customer.email }}</a></dd>
          </div>
          <div>
            <dt class="text-xs font-semibold uppercase tracking-wide text-ink-muted">{{ t('customers.phone') }}</dt>
            <dd class="mt-0.5"><a :href="`tel:${customer.phone}`" class="text-ink hover:text-brand">{{ customer.phone }}</a></dd>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <dt class="text-xs font-semibold uppercase tracking-wide text-ink-muted">{{ t('customers.dateOfBirth') }}</dt>
              <dd class="mt-0.5">{{ formatDate(customer.dateOfBirth, locale) }}</dd>
            </div>
            <div>
              <dt class="text-xs font-semibold uppercase tracking-wide text-ink-muted">{{ t('customers.gender') }}</dt>
              <dd class="mt-0.5">{{ customer.gender ? t(`customers.genders.${customer.gender}`) : '—' }}</dd>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <dt class="text-xs font-semibold uppercase tracking-wide text-ink-muted">{{ t('customers.joined') }}</dt>
              <dd class="mt-0.5">{{ formatDate(customer.createdAt, locale) }}</dd>
            </div>
            <div>
              <dt class="text-xs font-semibold uppercase tracking-wide text-ink-muted">{{ t('customers.lastLogin') }}</dt>
              <dd class="mt-0.5">{{ formatDateTime(customer.lastLoginAt, locale) }}</dd>
            </div>
          </div>
        </dl>
      </section>

      <div class="space-y-6 lg:col-span-2">
        <section class="card overflow-hidden">
          <header class="border-b border-line/70 px-5 py-4">
            <h2 class="font-bold text-ink">{{ t('customers.addresses', { count: customer.addresses.length }) }}</h2>
          </header>
          <EmptyState v-if="!customer.addresses.length" icon="customers" :title="t('customers.noAddresses')" />
          <ul v-else class="grid gap-3 p-4 sm:grid-cols-2">
            <li v-for="address in customer.addresses" :key="address.id" class="rounded-xl border p-4 text-sm" :class="address.defaultAddress ? 'border-brand-light bg-brand-tint/30' : 'border-line/80'">
              <div class="mb-2 flex items-center justify-between gap-2">
                <p class="font-semibold text-ink">{{ address.receiverName }}</p>
                <span v-if="address.defaultAddress" class="badge-brand">{{ t('customers.default') }}</span>
              </div>
              <p class="text-ink-muted">{{ address.phone }}</p>
              <p class="text-ink-muted">{{ address.streetDetail }}</p>
              <p class="text-ink-muted">{{ address.commune }}, {{ address.district }}, {{ address.province }}</p>
              <p v-if="address.note" class="mt-2 text-xs text-ink-muted">{{ t('customers.note') }}: {{ address.note }}</p>
            </li>
          </ul>
        </section>

        <section class="card overflow-hidden">
          <header class="border-b border-line/70 px-5 py-4">
            <h2 class="font-bold text-ink">{{ t('customers.orderHistory') }}</h2>
          </header>
          <EmptyState icon="orders" :title="t('orders.comingTitle')" :description="t('customers.ordersComing')" />
        </section>
      </div>
    </div>
  </div>
</template>
