<script setup lang="ts">
const { t } = useI18n()
const localePath = useLocalePath()

useSeoMeta({
  title: () => `${t('contact.title')} | ${t('app.name')}`,
  description: () => t('contact.subtitle'),
})

const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  topic: 'order',
  message: '',
})
const sent = ref(false)

const cards = computed(() => [
  { title: t('contact.helpTitle'), body: t('contact.helpBody'), icon: '💬' },
  { title: t('contact.returnsTitle'), body: t('contact.returnsBody'), icon: '📦' },
  { title: t('contact.shipTitle'), body: t('contact.shipBody'), icon: '🚚' },
])

function submit() {
  sent.value = true
}
</script>

<template>
  <div>
    <div class="bg-brand">
      <div class="container-store section-pad text-center">
        <h1 class="text-4xl font-bold text-white">{{ t('contact.title') }}</h1>
        <p class="mt-3 text-white/80">{{ t('contact.subtitle') }}</p>
        <p class="mt-4 text-lg font-semibold text-white">{{ t('contact.phone') }}</p>
        <p class="mt-1 text-sm text-white/75">{{ t('contact.hours') }}</p>
      </div>
    </div>

    <div class="container-store section-pad pt-8">
      <div class="grid gap-4 md:grid-cols-3">
        <article
          v-for="card in cards"
          :key="card.title"
          class="rounded-[1.5rem] border border-line bg-white p-6 text-center shadow-soft"
        >
          <div class="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-brand-tint text-2xl">
            {{ card.icon }}
          </div>
          <h2 class="text-lg font-bold">{{ card.title }}</h2>
          <p class="mt-2 text-sm text-ink-muted">{{ card.body }}</p>
        </article>
      </div>

      <div class="mt-10 grid gap-8 overflow-hidden rounded-[1.75rem] border border-line bg-white shadow-soft lg:grid-cols-2">
        <div class="bg-brand p-8 text-white lg:p-10">
          <h2 class="text-2xl font-bold">{{ t('contact.supportTitle') }}</h2>
          <p class="mt-3 text-sm text-white/75">{{ t('contact.supportBody') }}</p>
          <ul class="mt-6 space-y-3 text-sm text-white/85">
            <li>{{ t('contact.address') }}</li>
            <li>{{ t('contact.phone') }}</li>
            <li>{{ t('contact.email') }}</li>
            <li>{{ t('contact.hours') }}</li>
          </ul>
          <NuxtLink :to="localePath('/products')" class="btn-primary mt-8 inline-flex bg-white text-brand hover:bg-cream">
            {{ t('home.shopNow') }}
          </NuxtLink>
        </div>

        <form class="p-8 lg:p-10" @submit.prevent="submit">
          <h2 class="text-2xl font-bold">{{ t('contact.formTitle') }}</h2>
          <p class="mt-2 text-sm text-ink-muted">{{ t('contact.formHint') }}</p>

          <div v-if="sent" class="mt-6 rounded-2xl bg-mint-tint px-4 py-6 text-sm font-semibold text-ink">
            {{ t('contact.formThanks') }}
          </div>
          <div v-else class="mt-6 space-y-4">
            <div class="grid gap-4 sm:grid-cols-2">
              <label class="block text-sm">
                <span class="mb-1 block font-semibold">{{ t('contact.firstName') }}</span>
                <input v-model="form.firstName" required class="min-h-11 w-full rounded-2xl border border-line px-3">
              </label>
              <label class="block text-sm">
                <span class="mb-1 block font-semibold">{{ t('contact.lastName') }}</span>
                <input v-model="form.lastName" required class="min-h-11 w-full rounded-2xl border border-line px-3">
              </label>
            </div>
            <label class="block text-sm">
              <span class="mb-1 block font-semibold">{{ t('contact.emailLabel') }}</span>
              <input v-model="form.email" type="email" required class="min-h-11 w-full rounded-2xl border border-line px-3">
            </label>
            <label class="block text-sm">
              <span class="mb-1 block font-semibold">{{ t('contact.topic') }}</span>
              <select v-model="form.topic" class="min-h-11 w-full rounded-2xl border border-line px-3">
                <option value="order">{{ t('contact.topicOrder') }}</option>
                <option value="product">{{ t('contact.topicProduct') }}</option>
                <option value="shipping">{{ t('contact.topicShipping') }}</option>
                <option value="other">{{ t('contact.topicOther') }}</option>
              </select>
            </label>
            <label class="block text-sm">
              <span class="mb-1 block font-semibold">{{ t('contact.message') }}</span>
              <textarea v-model="form.message" required rows="4" class="w-full rounded-2xl border border-line px-3 py-2" />
            </label>
            <button type="submit" class="btn-primary">{{ t('contact.send') }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
