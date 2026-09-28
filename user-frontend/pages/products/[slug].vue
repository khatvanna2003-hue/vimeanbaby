<script setup lang="ts">
import type { ProductDetail } from '~/types/product'
import { formatUsd } from '~/utils/format'

const { t, locale } = useI18n()
const route = useRoute()
const localePath = useLocalePath()
const { apiFetch } = useApi()
const cart = useCartStore()

const slug = computed(() => String(route.params.slug))

const { data: product, error, pending } = await useAsyncData(
  () => `product-${slug.value}`,
  () => apiFetch<ProductDetail>(`/public/products/${slug.value}`),
  { watch: [slug] },
)

const selectedVariantId = ref<number | null>(null)

watch(product, (p) => {
  selectedVariantId.value = p?.variants?.[0]?.id ?? null
}, { immediate: true })

const selectedVariant = computed(() =>
  product.value?.variants.find(v => v.id === selectedVariantId.value) || product.value?.variants[0],
)

const name = computed(() => {
  if (!product.value) return ''
  return locale.value === 'km' ? product.value.nameKm : product.value.nameEn
})

const description = computed(() => {
  if (!product.value) return ''
  return locale.value === 'km' ? product.value.descriptionKm : product.value.descriptionEn
})

const activeImage = computed(() =>
  product.value?.images.find(i => i.primary)?.url
  || product.value?.images[0]?.url
  || '/logo.svg',
)

function addToCart() {
  if (!product.value || !selectedVariant.value) return
  cart.addDetail(product.value, selectedVariant.value, name.value)
}

useSeoMeta({
  title: () => name.value ? `${name.value} | ${t('app.name')}` : t('app.name'),
  description: () => description.value || t('home.heroSubtitle'),
  ogImage: () => activeImage.value,
})
</script>

<template>
  <div class="container-store section-pad">
    <p v-if="pending" class="py-16 text-center text-sm text-muted">{{ t('shop.loading') }}</p>
    <p v-else-if="error || !product" class="py-16 text-center text-sm text-muted">{{ t('shop.error') }}</p>
    <template v-else>
      <p class="mb-4 text-xs text-muted">
        <NuxtLink :to="localePath('/')">{{ t('nav.home') }}</NuxtLink>
        /
        <NuxtLink :to="localePath('/products')">{{ t('shop.title') }}</NuxtLink>
        / {{ name }}
      </p>

      <div class="grid gap-8 lg:grid-cols-2">
        <div class="animate-fade-up overflow-hidden rounded-[1.5rem] border border-line bg-white p-4 shadow-soft">
          <SafeImage :src="activeImage" :alt="name" img-class="aspect-square w-full rounded-2xl object-cover" />
          <div v-if="product.images.length > 1" class="mt-3 grid grid-cols-4 gap-2">
            <SafeImage
              v-for="image in product.images"
              :key="image.id"
              :src="image.url"
              alt=""
              img-class="aspect-square rounded-xl object-cover"
            />
          </div>
        </div>

        <div class="animate-fade-up [animation-delay:150ms]">
          <p class="text-xs font-semibold uppercase tracking-[0.16em] text-muted">
            {{ product.brand?.name || t('product.brand') }}
          </p>
          <h1 class="mt-2 text-3xl font-bold">{{ name }}</h1>
          <p class="mt-4 text-2xl font-bold">
            {{ formatUsd(selectedVariant?.price) }}
          </p>
          <p
            v-if="selectedVariant?.compareAtPrice && Number(selectedVariant.compareAtPrice) > Number(selectedVariant.price)"
            class="text-sm text-muted line-through"
          >
            {{ formatUsd(selectedVariant.compareAtPrice) }}
          </p>

          <div class="mt-6 space-y-2 text-sm text-muted">
            <p v-if="product.ageRange"><span class="font-semibold text-ink">{{ t('product.age') }}:</span> {{ product.ageRange }}</p>
            <p v-if="product.originCountry"><span class="font-semibold text-ink">{{ t('product.origin') }}:</span> {{ product.originCountry }}</p>
            <p v-if="selectedVariant?.expiryDate"><span class="font-semibold text-ink">{{ t('product.expiry') }}:</span> {{ selectedVariant.expiryDate }}</p>
          </div>

          <div class="mt-6">
            <p class="mb-2 text-sm font-semibold">{{ t('product.variants') }}</p>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="variant in product.variants"
                :key="variant.id"
                type="button"
                class="min-h-11 rounded-2xl border px-4 text-sm font-semibold transition"
                :class="selectedVariantId === variant.id ? 'border-brand bg-brand-tint' : 'border-line bg-white'"
                @click="selectedVariantId = variant.id"
              >
                {{ variant.optionName }}
              </button>
            </div>
          </div>

          <button
            type="button"
            class="btn-primary mt-8 w-full sm:w-auto"
            :disabled="!selectedVariant || selectedVariant.stockQty <= 0"
            @click="addToCart"
          >
            {{ selectedVariant && selectedVariant.stockQty > 0 ? t('product.addToCart') : t('product.outOfStock') }}
          </button>

          <div class="mt-8 rounded-2xl border border-line bg-white p-5">
            <h2 class="mb-2 text-sm font-semibold">{{ t('product.description') }}</h2>
            <p class="text-sm leading-relaxed text-muted">
              {{ description || t('home.storyBody') }}
            </p>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
