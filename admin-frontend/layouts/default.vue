<template>
  <div class="min-h-screen bg-surface text-ink">
    <div class="flex min-h-screen">
      <aside class="hidden w-56 border-r border-line bg-white p-4 md:block">
        <p class="mb-6 text-lg font-semibold text-brand">Vimean Baby</p>
        <nav class="space-y-2 text-sm text-ink-muted">
          <p class="rounded-lg bg-brand-tint px-3 py-2 font-semibold text-brand">Dashboard</p>
          <p class="px-3 py-2">Products</p>
          <p class="px-3 py-2">Orders</p>
        </nav>
      </aside>
      <main class="flex-1 p-6">
        <slot />
      </main>
    </div>
  </div>
</template>
