<script setup lang="ts">
import type { Category } from '~/types/product'

defineProps<{
  categories: Category[]
}>()

const { locale, t } = useI18n()
const localePath = useLocalePath()
</script>

<template>
  <section class="bg-gradient-to-r from-mint-tint via-brand-tint to-blush-tint">
    <div class="container-store section-pad">
      <h2 class="mb-6 text-2xl font-bold uppercase tracking-wide text-ink sm:text-3xl">
        {{ t('home.categories') }}
      </h2>
      <div class="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
        <NuxtLink
          v-for="category in categories"
          :key="category.id"
          :to="localePath(`/categories/${category.slug}`)"
          class="group text-center"
        >
          <div class="overflow-hidden rounded-2xl bg-white p-3 shadow-soft transition group-hover:-translate-y-1">
            <SafeImage
              :src="category.imageUrl"
              :alt="locale === 'km' ? category.nameKm : category.nameEn"
              img-class="aspect-square w-full rounded-xl object-cover"
            />
          </div>
          <p class="mt-2 text-sm font-semibold text-ink">
            {{ locale === 'km' ? category.nameKm : category.nameEn }}
          </p>
        </NuxtLink>
      </div>
    </div>
  </section>
</template>
