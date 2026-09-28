<script setup lang="ts">
import type { ProductSummary } from '~/types/product'

defineProps<{
  products: ProductSummary[]
}>()
</script>

<template>
  <div class="grid grid-cols-2 gap-3 sm:gap-4 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5">
    <ProductCard v-for="product in products" :key="product.id" :product="product" />
  </div>
</template>
