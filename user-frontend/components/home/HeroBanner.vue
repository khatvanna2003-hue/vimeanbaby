<script setup lang="ts">
const { t } = useI18n()
const localePath = useLocalePath()

const slides = [
  {
    eyebrow: 'home.heroEyebrow',
    title: 'home.heroTitle',
    subtitle: 'home.heroSubtitle',
    image: 'https://res.cloudinary.com/vimeanbaby/image/upload/vimeanbaby/packshots/p-0003',
  },
  {
    eyebrow: 'home.heroEyebrow',
    title: 'home.topOffers',
    subtitle: 'home.topOffersSub',
    image: 'https://res.cloudinary.com/vimeanbaby/image/upload/vimeanbaby/packshots/p-0001',
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
    <div class="relative min-h-[380px] overflow-hidden rounded-[1.75rem] bg-ink text-white shadow-soft sm:min-h-[440px] lg:min-h-[520px]">
      <div
        v-for="(slide, i) in slides"
        :key="i"
        class="absolute inset-0 transition-opacity duration-500"
        :class="i === index ? 'opacity-100' : 'pointer-events-none opacity-0'"
      >
        <SafeImage
          :src="slide.image"
          alt=""
          img-class="absolute inset-0 h-full w-full object-cover"
          :width="1400"
        />
        <div class="absolute inset-0 bg-gradient-to-r from-ink/85 via-ink/55 to-ink/20" />
      </div>

      <div class="relative z-10 flex h-full min-h-[380px] flex-col justify-center px-6 py-12 sm:min-h-[440px] sm:px-10 lg:min-h-[520px] lg:max-w-xl lg:px-14">
        <p class="mb-3 text-xs font-semibold uppercase tracking-[0.2em] text-brand-light">
          {{ t(slides[index].eyebrow) }}
        </p>
        <h1 class="text-3xl font-bold leading-tight sm:text-4xl lg:text-5xl">
          {{ t('app.name') }}
        </h1>
        <p class="mt-2 text-xl font-semibold text-mint sm:text-2xl">
          {{ t(slides[index].title) }}
        </p>
        <p class="mt-4 max-w-md text-sm text-white/80 sm:text-base">
          {{ t(slides[index].subtitle) }}
        </p>
        <div class="mt-8">
          <NuxtLink :to="localePath('/products')" class="btn-primary bg-white text-ink hover:bg-cream">
            {{ t('home.shopNow') }}
          </NuxtLink>
        </div>
      </div>

      <div class="absolute bottom-4 right-4 z-10 flex items-center gap-2 rounded-full bg-white/95 px-2 py-1 text-ink">
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
