<script setup lang="ts">
import type { Category } from '~/types/product'

defineProps<{
  categories: Category[]
}>()

const { locale, t } = useI18n()
const localePath = useLocalePath()
</script>

<template>
  <section class="bg-brand">
    <div class="container-store section-pad">
      <div class="mb-6 flex items-end justify-between gap-4">
        <h2 class="text-2xl font-bold uppercase tracking-wide text-white sm:text-3xl">
          {{ t('home.categories') }}
        </h2>
        <NuxtLink :to="localePath('/categories')" class="shrink-0 text-sm font-semibold text-white hover:underline">
          {{ t('home.viewAllShort') }} →
        </NuxtLink>
      </div>
      <div class="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
        <ScrollReveal v-for="(category, index) in categories" :key="category.id" :index="index">
          <NuxtLink
            :to="localePath(`/categories/${category.slug}`)"
            class="group block text-center"
          >
            <div class="overflow-hidden rounded-2xl bg-white p-3 shadow-soft transition duration-300 group-hover:-translate-y-1">
              <SafeImage
                :src="category.imageUrl"
                :alt="locale === 'km' ? category.nameKm : category.nameEn"
                img-class="aspect-square w-full rounded-xl object-cover transition duration-500 group-hover:scale-105"
              />
            </div>
            <p class="mt-2 text-sm font-semibold text-white">
              {{ locale === 'km' ? category.nameKm : category.nameEn }}
            </p>
          </NuxtLink>
        </ScrollReveal>
      </div>
    </div>
  </section>
</template>
