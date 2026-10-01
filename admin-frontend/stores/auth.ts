import { defineStore } from 'pinia'
import type { AdminUser, ApiResponse, AuthResponse } from '~/types/admin'

const ACCESS_COOKIE = 'vb_admin_at'
const REFRESH_COOKIE = 'vb_admin_rt'
const SESSION_MAX_AGE = 60 * 60 * 24 * 7

export const useAuthStore = defineStore('auth', () => {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase as string
  const cookieOptions = {
    maxAge: SESSION_MAX_AGE,
    sameSite: 'strict' as const,
    secure: !import.meta.dev,
    path: '/',
  }
  const accessToken = useCookie<string | null>(ACCESS_COOKIE, { ...cookieOptions, default: () => null })
  const refreshToken = useCookie<string | null>(REFRESH_COOKIE, { ...cookieOptions, default: () => null })

  const user = ref<AdminUser | null>(null)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  let refreshing: Promise<boolean> | null = null

  function setSession(auth: AuthResponse) {
    if (auth.user.role !== 'ADMIN') {
      clearSession()
      throw new ApiError('Admin access required', 403, 'NOT_ADMIN')
    }
    accessToken.value = auth.accessToken
    refreshToken.value = auth.refreshToken
    user.value = auth.user
  }

  function clearSession() {
    accessToken.value = null
    refreshToken.value = null
    user.value = null
  }

  async function login(identifier: string, password: string) {
    let auth: AuthResponse
    try {
      const response = await $fetch<ApiResponse<AuthResponse>>('/auth/login', {
        baseURL,
        method: 'POST',
        body: { identifier, password },
      })
      auth = response.data
    }
    catch (error) {
      throw toApiError(error)
    }
    setSession(auth)
  }

  /** Exchanges the refresh token for a new pair; concurrent callers share one request. */
  function refresh(): Promise<boolean> {
    if (!refreshToken.value) return Promise.resolve(false)
    refreshing ??= $fetch<ApiResponse<AuthResponse>>('/auth/refresh', {
      baseURL,
      method: 'POST',
      body: { refreshToken: refreshToken.value },
    })
      .then((response) => {
        setSession(response.data)
        return true
      })
      .catch((error) => {
        const status = toApiError(error).status
        if (status === 400 || status === 401 || status === 403) clearSession()
        return false
      })
      .finally(() => {
        refreshing = null
      })
    return refreshing
  }

  async function fetchMe() {
    const response = await $fetch<ApiResponse<AdminUser>>('/me', {
      baseURL,
      headers: { Authorization: `Bearer ${accessToken.value}` },
    })
    if (response.data.role !== 'ADMIN') {
      clearSession()
      return
    }
    user.value = response.data
  }

  async function init() {
    if (user.value || (!accessToken.value && !refreshToken.value)) return
    if (accessToken.value) {
      try {
        await fetchMe()
        return
      }
      catch (error) {
        if (toApiError(error).status !== 401) return
      }
    }
    if (await refresh()) {
      await fetchMe().catch(() => undefined)
    }
  }

  function logout() {
    clearSession()
  }

  function setUser(profile: AdminUser) {
    user.value = profile
  }

  function getAccessToken() {
    return accessToken.value
  }

  return { user, isAdmin, login, refresh, init, logout, setSession, setUser, getAccessToken }
})
