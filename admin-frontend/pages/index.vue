<template>
  <div>
    <h1 class="mb-2 text-2xl font-semibold text-ink">Admin Dashboard</h1>
    <p class="mb-6 text-ink-muted">Admin scaffold ready. Features come in Phase 7.</p>
    <div class="grid gap-4 sm:grid-cols-3">
      <div class="rounded-card border border-line bg-white p-4">
        <p class="text-xs font-semibold uppercase tracking-wide text-ink-muted">Today sales</p>
        <p class="mt-2 text-2xl font-bold text-brand">$0.00</p>
      </div>
      <div class="rounded-card border border-line bg-white p-4">
        <p class="text-xs font-semibold uppercase tracking-wide text-ink-muted">Orders</p>
        <p class="mt-2 text-2xl font-bold text-brand">0</p>
        <span class="badge-pending mt-2 inline-flex">Pending</span>
      </div>
      <div class="rounded-card border border-line bg-white p-4">
        <p class="text-xs font-semibold uppercase tracking-wide text-ink-muted">Low stock</p>
        <p class="mt-2 text-2xl font-bold text-warn">0</p>
      </div>
    </div>
  </div>
</template>
