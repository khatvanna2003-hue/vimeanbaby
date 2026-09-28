<script setup lang="ts">
const { t } = useI18n()
const localePath = useLocalePath()

const slides = [
  {
    eyebrow: 'home.heroEyebrow',
    title: 'home.heroTitle',
    subtitle: 'home.heroSubtitle',
    image: '/catalog/cat-bath.svg',
  },
  {
    eyebrow: 'home.heroEyebrow',
    title: 'home.topOffers',
    subtitle: 'home.topOffersSub',
    image: '/catalog/cat-diapers.svg',
  },
]

const index = ref(0)

function next() {
  index.value = (index.value + 1) % slides.length
}

function prev() {
  index.value = (index.value - 1 + slides.length) % slides.length
}

let timer: ReturnType<typeof setInterval> | undefined

onMounted(() => {
  timer = setInterval(next, 6000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <section class="container-store section-pad pb-6 pt-6 sm:pt-8">
    <div class="relative overflow-hidden rounded-[1.75rem] bg-ink text-white shadow-soft">
      <div
        v-for="(slide, i) in slides"
        :key="i"
        class="grid min-h-[340px] transition-opacity duration-500 lg:min-h-[420px] lg:grid-cols-2"
        :class="i === index ? 'relative opacity-100' : 'pointer-events-none absolute inset-0 opacity-0'"
      >
        <div class="flex flex-col justify-center px-6 py-10 sm:px-10 lg:px-14">
          <p class="mb-3 text-xs font-semibold uppercase tracking-[0.2em] text-brand-light">
            {{ t(slide.eyebrow) }}
          </p>
          <h1 class="text-3xl font-bold leading-tight sm:text-4xl lg:text-5xl">
            {{ t('app.name') }}
          </h1>
          <p class="mt-2 text-xl font-semibold text-mint sm:text-2xl">
            {{ t(slide.title) }}
          </p>
          <p class="mt-4 max-w-md text-sm text-white/75 sm:text-base">
            {{ t(slide.subtitle) }}
          </p>
          <div class="mt-8">
            <NuxtLink :to="localePath('/products')" class="btn-primary bg-white text-ink hover:bg-cream">
              {{ t('home.shopNow') }}
            </NuxtLink>
          </div>
        </div>
        <div class="relative hidden min-h-[280px] lg:block">
          <img :src="slide.image" alt="" class="absolute inset-0 h-full w-full object-cover opacity-90">
          <div class="absolute inset-0 bg-gradient-to-l from-transparent to-ink/40" />
        </div>
      </div>

      <div class="absolute bottom-4 right-4 flex items-center gap-2 rounded-full bg-white/95 px-2 py-1 text-ink">
        <button type="button" class="h-8 w-8 rounded-full hover:bg-cream" @click="prev">‹</button>
        <div class="flex gap-1.5 px-1">
          <span
            v-for="(_, i) in slides"
            :key="i"
            class="h-2 w-2 rounded-full"
            :class="i === index ? 'bg-brand' : 'bg-line'"
          />
        </div>
        <button type="button" class="h-8 w-8 rounded-full hover:bg-cream" @click="next">›</button>
      </div>
    </div>
  </section>
</template>
