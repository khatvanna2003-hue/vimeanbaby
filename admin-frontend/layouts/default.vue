<script setup lang="ts">
const sidebarOpen = ref(false)
</script>

<template>
  <div class="min-h-screen bg-surface text-ink">
    <AppSidebar :open="sidebarOpen" @close="sidebarOpen = false" />
    <div class="lg:pl-64">
      <AppTopbar @menu="sidebarOpen = true" />
      <main class="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:py-8">
        <slot />
      </main>
    </div>
    <ToastHost />
    <ConfirmDialog />
  </div>
</template>
