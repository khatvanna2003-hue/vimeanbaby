export interface MediaUploadResult {
  url: string
  secureUrl: string
  publicId: string
  format: string
  width: number | null
  height: number | null
  folder: string
  thumbnailUrl: string | null
}

const ALLOWED_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp']
const MAX_BYTES = 5 * 1024 * 1024

export function useUpload() {
  const { apiFetch } = useApi()
  const { t } = useI18n()
  const uploading = ref(false)
  const error = ref<string | null>(null)

  function validate(file: File) {
    if (!ALLOWED_TYPES.includes(file.type) && !/\.(jpe?g|png|webp)$/i.test(file.name)) {
      throw new Error(t('upload.invalidType'))
    }
    if (file.size > MAX_BYTES) {
      throw new Error(t('upload.tooLarge'))
    }
  }

  async function uploadImage(file: File, folder = 'vimeanbaby/products'): Promise<MediaUploadResult> {
    uploading.value = true
    error.value = null
    try {
      validate(file)
      const form = new FormData()
      form.append('file', file)
      form.append('folder', folder)
      return await apiFetch<MediaUploadResult>('/admin/media/upload', { method: 'POST', body: form })
    }
    catch (e: unknown) {
      error.value = e instanceof Error ? e.message : t('upload.failed')
      throw e
    }
    finally {
      uploading.value = false
    }
  }

  async function deleteImage(publicId: string) {
    if (!publicId) return
    await apiFetch<null>(`/admin/media/${publicId}`, { method: 'DELETE' })
  }

  return { uploadImage, deleteImage, uploading, error }
}
