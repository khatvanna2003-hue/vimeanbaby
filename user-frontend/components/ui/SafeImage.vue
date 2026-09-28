<script setup lang="ts">
import { cloudinaryUrl } from '~/utils/cloudinary'

const props = withDefaults(defineProps<{
  src?: string | null
  alt?: string
  imgClass?: string
  width?: number
}>(), {
  src: '',
  alt: '',
  imgClass: 'aspect-square w-full object-cover',
  width: 600,
})

const fallback = '/catalog/fallback.svg'
const failed = ref(false)

const resolved = computed(() => {
  if (failed.value || !props.src) return fallback
  return cloudinaryUrl(props.src, { width: props.width })
})

function onError() {
  failed.value = true
}

watch(() => props.src, () => {
  failed.value = false
})
</script>

<template>
  <img
    :src="resolved"
    :alt="alt"
    :class="imgClass"
    loading="lazy"
    decoding="async"
    @error="onError"
  >
</template>
