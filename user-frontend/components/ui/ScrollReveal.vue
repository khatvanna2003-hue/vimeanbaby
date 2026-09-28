<script setup lang="ts">
const props = withDefaults(defineProps<{
  index?: number
}>(), {
  index: 0,
})

const delayClasses = ['delay-0', 'delay-75', 'delay-150', 'delay-200', 'delay-300']

const target = ref<HTMLElement | null>(null)
const visible = ref(false)

const { stop } = useIntersectionObserver(
  target,
  ([entry]) => {
    if (entry?.isIntersecting) {
      visible.value = true
      stop()
    }
  },
  { rootMargin: '0px 0px -40px 0px', threshold: 0.1 },
)

const delayClass = computed(() => delayClasses[props.index % delayClasses.length])
</script>

<template>
  <div ref="target" class="reveal" :class="[delayClass, { 'reveal-visible': visible }]">
    <slot />
  </div>
</template>
