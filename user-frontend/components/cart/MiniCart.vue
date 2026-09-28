<script setup lang="ts">
import { formatUsd } from '~/utils/format'

const { t } = useI18n()
const localePath = useLocalePath()
const cart = useCartStore()
</script>

<template>
  <Teleport to="body">
    <div v-if="cart.open" class="fixed inset-0 z-[60]">
      <button class="absolute inset-0 bg-ink/40" type="button" aria-label="Close cart" @click="cart.toggle(false)" />
      <aside class="absolute inset-y-0 right-0 flex w-full max-w-md flex-col bg-white shadow-soft">
        <div class="flex items-center justify-between border-b border-line px-5 py-4">
          <h2 class="text-lg font-bold">{{ t('cart.title') }}</h2>
          <button type="button" class="h-10 w-10 rounded-full border border-line" @click="cart.toggle(false)">
            ✕
          </button>
        </div>

        <div class="flex-1 overflow-y-auto px-5 py-4">
          <div v-if="!cart.items.length" class="rounded-2xl bg-cream px-4 py-10 text-center">
            <p class="mb-4 text-sm text-muted">{{ t('cart.empty') }}</p>
            <button type="button" class="btn-ghost" @click="cart.toggle(false)">
              {{ t('cart.continue') }}
            </button>
          </div>

          <ul v-else class="space-y-4">
            <li
              v-for="item in cart.items"
              :key="item.variantId"
              class="flex gap-3 rounded-2xl border border-line p-3"
            >
              <SafeImage :src="item.image" :alt="item.name" img-class="h-20 w-20 rounded-xl object-cover" />
              <div class="min-w-0 flex-1">
                <p class="text-[11px] uppercase tracking-wide text-muted">{{ item.brand }}</p>
                <p class="truncate text-sm font-semibold">{{ item.name }}</p>
                <p class="mt-1 text-sm">{{ formatUsd(item.price) }}</p>
                <div class="mt-2 flex items-center gap-2">
                  <input
                    type="number"
                    min="1"
                    class="h-9 w-16 rounded-lg border border-line px-2 text-sm"
                    :value="item.quantity"
                    @change="cart.setQuantity(item.variantId, Number(($event.target as HTMLInputElement).value))"
                  >
                  <button type="button" class="text-xs text-muted underline" @click="cart.remove(item.variantId)">
                    {{ t('cart.remove') }}
                  </button>
                </div>
              </div>
            </li>
          </ul>
        </div>

        <div class="border-t border-line px-5 py-4">
          <div class="mb-1 flex items-center justify-between text-sm font-semibold">
            <span>{{ t('cart.estimatedTotal') }}</span>
            <span>{{ formatUsd(cart.subtotal) }}</span>
          </div>
          <p class="mb-4 text-xs text-muted">{{ t('cart.note') }}</p>
          <NuxtLink
            :to="localePath('/checkout')"
            class="btn-primary w-full"
            @click="cart.toggle(false)"
          >
            {{ t('cart.checkout') }}
          </NuxtLink>
        </div>
      </aside>
    </div>
  </Teleport>
</template>
