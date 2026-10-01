<script setup lang="ts">
import type { ProductDetail, ProductPayload } from '~/types/admin'

const { t } = useI18n()
const router = useRouter()
const { apiFetch } = useApi()
const toast = useToast()
const errorMessage = useApiErrorMessage()

useHead({ title: () => `${t('products.add')} | Vimean Baby Admin` })

const saving = ref(false)

async function create(payload: ProductPayload) {
  saving.value = true
  try {
    const product = await apiFetch<ProductDetail>('/admin/products', { method: 'POST', body: payload })
    toast.success(t('products.created'))
    await router.replace(`/products/${product.id}`)
  }
  catch (error) {
    toast.error(errorMessage(error))
  }
  finally {
    saving.value = false
  }
}
</script>

<template>
  <div>
    <PageHeader :title="t('products.add')" :description="t('products.addSubtitle')">
      <template #breadcrumb>
        <NuxtLink to="/products" class="mb-1 inline-flex items-center gap-1 text-sm text-ink-muted hover:text-ink">
          <AppIcon name="chevronLeft" :size="16" /> {{ t('nav.products') }}
        </NuxtLink>
      </template>
    </PageHeader>
    <ProductForm :product="null" :saving="saving" @submit="create" />
  </div>
</template>
