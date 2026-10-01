import type { ApiResponse } from '~/types/product'

export function useApi() {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase as string
  const auth = useAuthStore()

  async function send<T>(path: string, opts: Parameters<typeof $fetch>[1]) {
    const token = auth.getAccessToken()
    const headers = new Headers((opts?.headers ?? {}) as HeadersInit)
    if (token) headers.set('Authorization', `Bearer ${token}`)

    const response = await $fetch<ApiResponse<T>>(path, { baseURL, ...opts, headers })
    if (!response?.success) {
      throw new Error(response?.message || 'Request failed')
    }
    return response.data
  }

  /** Calls the API, retrying once after a token refresh when the access token has expired. */
  async function apiFetch<T>(path: string, opts: Parameters<typeof $fetch>[1] = {}) {
    try {
      return await send<T>(path, opts)
    }
    catch (error) {
      const apiError = toApiError(error)
      if (apiError.status === 401 && await auth.refresh()) {
        try {
          return await send<T>(path, opts)
        }
        catch (retryError) {
          throw toApiError(retryError)
        }
      }
      throw apiError
    }
  }

  return { apiFetch, baseURL }
}
