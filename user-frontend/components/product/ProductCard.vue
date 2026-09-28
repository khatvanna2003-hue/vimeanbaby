<script setup lang="ts">
import type { ProductSummary } from '~/types/product'
import { formatUsd } from '~/utils/format'

const props = defineProps<{
  product: ProductSummary
}>()

const { t, locale } = useI18n()
const localePath = useLocalePath()
const cart = useCartStore()

const name = computed(() => (locale.value === 'km' ? props.product.nameKm : props.product.nameEn))
const lowStock = computed(() => props.product.stockQty > 0 && props.product.stockQty <= 10)

function addToCart() {
  cart.addSummary(props.product, name.value)
}
</script>

<template>
  <article class="flex h-full flex-col rounded-2xl border border-line bg-white p-3 shadow-soft transition hover:-translate-y-0.5 hover:shadow-md">
    <NuxtLink :to="localePath(`/products/${product.slug}`)" class="block overflow-hidden rounded-xl bg-cream">
      <SafeImage
        :src="product.primaryImageUrl"
        :alt="name"
        img-class="aspect-square w-full object-cover transition duration-500 hover:scale-105"
      />
    </NuxtLink>

    <div class="mt-3 flex flex-1 flex-col">
      <p class="text-[11px] font-medium uppercase tracking-[0.14em] text-muted">
        {{ product.brandName || t('product.brand') }}
      </p>
      <NuxtLink :to="localePath(`/products/${product.slug}`)" class="mt-1 line-clamp-2 text-sm font-semibold text-ink">
        {{ name }}
      </NuxtLink>

      <p
        v-if="lowStock"
        class="badge-warn mt-2"
      >
        <span class="h-1.5 w-1.5 rounded-full bg-warn" />
        {{ t('product.onlyLeft', { count: product.stockQty }) }}
      </p>

      <div class="mt-auto pt-3">
        <div class="mb-3 flex items-baseline gap-2">
          <span class="text-base font-bold">{{ formatUsd(product.price) }}</span>
          <span
            v-if="product.compareAtPrice && Number(product.compareAtPrice) > Number(product.price)"
            class="text-xs text-muted line-through"
          >
            {{ formatUsd(product.compareAtPrice) }}
          </span>
        </div>
        <button type="button" class="btn-ghost w-full" :disabled="product.stockQty <= 0" @click="addToCart">
          {{ product.stockQty > 0 ? t('product.addToCart') : t('product.outOfStock') }}
        </button>
      </div>
    </div>
  </article>
</template>
