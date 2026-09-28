<template>
  <div class="flex min-h-screen flex-col bg-cream">
    <AppHeader />
    <main class="flex-1">
      <slot />
    </main>
    <AppFooter />
    <MiniCart />
  </div>
</template>

<script setup lang="ts">
const cart = useCartStore()
onMounted(() => cart.hydrate())
</script>
