import { useApi } from './useApi'

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
  const { baseURL, apiFetch } = useApi()
  const uploading = ref(false)
  const error = ref<string | null>(null)

  function validate(file: File) {
    if (!ALLOWED_TYPES.includes(file.type) && !/\.(jpe?g|png|webp)$/i.test(file.name)) {
      throw new Error('Only jpg, png, and webp images are allowed')
    }
    if (file.size > MAX_BYTES) {
      throw new Error('File size must be <= 5 MB')
    }
  }

  async function uploadImage(file: File, folder = 'vimeanbaby/products'): Promise<MediaUploadResult> {
    validate(file)
    uploading.value = true
    error.value = null
    try {
      const form = new FormData()
      form.append('file', file)
      form.append('folder', folder)

      const response = await $fetch<{ success: boolean; message?: string; data: MediaUploadResult }>(
        '/admin/media/upload',
        {
          baseURL,
          method: 'POST',
          body: form,
        },
      )
      if (!response?.success) {
        throw new Error(response?.message || 'Upload failed')
      }
      return response.data
    } catch (e: unknown) {
      const message = e instanceof Error ? e.message : 'Upload failed'
      error.value = message
      throw e
    } finally {
      uploading.value = false
    }
  }

  async function deleteImage(publicId: string) {
    if (!publicId) return
    await apiFetch<null>(`/admin/media/${publicId}`, { method: 'DELETE' })
  }

  return { uploadImage, deleteImage, uploading, error }
}
