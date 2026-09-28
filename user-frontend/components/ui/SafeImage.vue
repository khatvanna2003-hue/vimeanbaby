<script setup lang="ts">
const props = withDefaults(defineProps<{
  src?: string | null
  alt?: string
  imgClass?: string
}>(), {
  src: '',
  alt: '',
  imgClass: 'aspect-square w-full object-cover',
})

const fallback = '/catalog/fallback.svg'
const failed = ref(false)

const resolved = computed(() => {
  if (failed.value || !props.src) return fallback
  return props.src
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
    @error="onError"
  >
</template>
