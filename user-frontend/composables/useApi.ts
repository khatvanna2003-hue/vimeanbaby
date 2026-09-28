import type { ApiResponse } from '~/types/product'

export function useApi() {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase as string

  async function apiFetch<T>(path: string, opts: Parameters<typeof $fetch>[1] = {}) {
    const response = await $fetch<ApiResponse<T>>(path, {
      baseURL,
      ...opts,
    })
    if (!response?.success) {
      throw new Error(response?.message || 'Request failed')
    }
    return response.data
  }

  return { apiFetch, baseURL }
}
