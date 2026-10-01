<script setup lang="ts">
import type { ProductDetail, ProductPayload } from '~/types/admin'

const { t } = useI18n()
const route = useRoute()
const { apiFetch } = useApi()
const toast = useToast()
const errorMessage = useApiErrorMessage()
const storeUrl = useRuntimeConfig().public.storeUrl as string

const id = computed(() => String(route.params.id))
const { data: product, status, error, refresh } = await useAsyncData(
  () => `admin-product-${id.value}`,
  () => apiFetch<ProductDetail>(`/admin/products/${id.value}`),
)

useHead({ title: () => `${product.value?.nameEn ?? t('nav.products')} | Vimean Baby Admin` })

const saving = ref(false)
const formKey = ref(0)

async function save(payload: ProductPayload) {
  saving.value = true
  try {
    product.value = await apiFetch<ProductDetail>(`/admin/products/${id.value}`, { method: 'PUT', body: payload })
    formKey.value++
    toast.success(t('products.saved'))
  }
  catch (err) {
    toast.error(errorMessage(err))
  }
  finally {
    saving.value = false
  }
}
</script>

<template>
  <div>
    <PageHeader :title="product?.nameEn ?? t('products.edit')">
      <template #breadcrumb>
        <NuxtLink to="/products" class="mb-1 inline-flex items-center gap-1 text-sm text-ink-muted hover:text-ink">
          <AppIcon name="chevronLeft" :size="16" /> {{ t('nav.products') }}
        </NuxtLink>
      </template>
      <template v-if="product" #actions>
        <span :class="product.active ? 'badge-success' : 'badge-neutral'">{{ product.active ? t('common.active') : t('common.inactive') }}</span>
        <a v-if="product.active" :href="`${storeUrl}/products/${product.slug}`" target="_blank" rel="noopener" class="btn-secondary">
          <AppIcon name="external" :size="16" /> {{ t('products.viewInStore') }}
        </a>
      </template>
    </PageHeader>

    <div v-if="error" class="card p-6">
      <EmptyState icon="alert" :title="t('errors.loadTitle')" :description="errorMessage(error)">
        <NuxtLink to="/products" class="btn-secondary">{{ t('nav.products') }}</NuxtLink>
        <button type="button" class="btn-primary ml-2" @click="refresh()">{{ t('common.retry') }}</button>
      </EmptyState>
    </div>
    <div v-else-if="status === 'pending' || !product" class="grid gap-6 xl:grid-cols-3" aria-busy="true">
      <div class="card h-96 animate-pulse xl:col-span-2" />
      <div class="card h-64 animate-pulse" />
    </div>
    <ProductForm v-else :key="formKey" :product="product" :saving="saving" @submit="save" />
  </div>
</template>
