import type { ApiResponse } from '~/types/admin'

export function useApi() {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase as string
  const auth = useAuthStore()
  const router = useRouter()

  async function send<T>(path: string, opts: Parameters<typeof $fetch>[1]) {
    const token = auth.getAccessToken()
    const headers = new Headers((opts?.headers ?? {}) as HeadersInit)
    if (token) headers.set('Authorization', `Bearer ${token}`)

    const response = await $fetch<ApiResponse<T>>(path, { baseURL, ...opts, headers })
    if (!response?.success) {
      throw new ApiError(response?.message || 'Request failed', 200, null)
    }
    return response.data
  }

  /** Calls the API, retrying once after refreshing an expired access token. Signs out when the session is gone. */
  async function apiFetch<T>(path: string, opts: Parameters<typeof $fetch>[1] = {}) {
    try {
      return await send<T>(path, opts)
    }
    catch (error) {
      const apiError = toApiError(error)
      if (apiError.status !== 401) throw apiError
      if (await auth.refresh()) {
        try {
          return await send<T>(path, opts)
        }
        catch (retryError) {
          throw toApiError(retryError)
        }
      }
      auth.logout()
      const current = router.currentRoute.value
      if (current.path !== '/login') {
        await router.push({ path: '/login', query: { redirect: current.fullPath } })
      }
      throw apiError
    }
  }

  return { apiFetch, baseURL }
}
