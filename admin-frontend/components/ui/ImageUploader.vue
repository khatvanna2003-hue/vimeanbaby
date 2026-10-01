<script setup lang="ts">
const props = withDefaults(defineProps<{
  label: string
  folder?: string
}>(), { folder: 'vimeanbaby/catalog' })

const model = defineModel<string | null>({ default: null })
const { t } = useI18n()
const toast = useToast()
const { uploadImage, uploading } = useUpload()
const input = ref<HTMLInputElement | null>(null)

async function onFile(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  try {
    const result = await uploadImage(file, props.folder)
    model.value = result.secureUrl
  }
  catch (error) {
    toast.error(error instanceof Error ? error.message : t('upload.failed'))
  }
  finally {
    if (input.value) input.value.value = ''
  }
}
</script>

<template>
  <div>
    <span class="field-label">{{ label }}</span>
    <div class="flex items-center gap-4">
      <div class="flex h-20 w-20 shrink-0 items-center justify-center overflow-hidden rounded-xl border border-line bg-surface">
        <img v-if="model" :src="thumb(model, 160)" alt="" class="h-full w-full object-contain" loading="lazy">
        <AppIcon v-else name="photo" :size="28" class="text-ink-muted" />
      </div>
      <div class="flex flex-wrap gap-2">
        <button type="button" class="btn-secondary" :disabled="uploading" @click="input?.click()">
          <AppIcon name="upload" :size="16" />
          {{ uploading ? t('upload.uploading') : t('upload.choose') }}
        </button>
        <button v-if="model" type="button" class="btn-secondary text-danger" @click="model = null">
          {{ t('common.remove') }}
        </button>
      </div>
      <input ref="input" type="file" accept="image/jpeg,image/png,image/webp" class="hidden" @change="onFile">
    </div>
  </div>
</template>
