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
  imgClass: '',
  width: 600,
})

const broken = ref(false)
const resolved = computed(() => {
  if (broken.value || !props.src) return '/catalog/fallback.svg'
  return cloudinaryUrl(props.src, { width: props.width })
})

function onError() {
  broken.value = true
}

watch(() => props.src, () => {
  broken.value = false
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
