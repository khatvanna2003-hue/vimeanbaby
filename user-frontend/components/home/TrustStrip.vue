<script setup lang="ts">
const { t } = useI18n()

const items = computed(() => [
  t('home.trustAuth'),
  t('home.trustCod'),
  t('home.trustShip'),
])
</script>

<template>
  <section class="container-store px-4 pb-2 sm:px-6 lg:px-8">
    <div class="grid gap-3 rounded-2xl border border-line bg-white p-4 shadow-soft sm:grid-cols-3">
      <div
        v-for="item in items"
        :key="item"
        class="rounded-xl bg-mint-tint px-4 py-3 text-center text-sm font-semibold text-ink"
      >
        {{ item }}
      </div>
    </div>
  </section>
</template>
