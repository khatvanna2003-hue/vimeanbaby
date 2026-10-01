import { defineStore } from 'pinia'
import type { ApiResponse } from '~/types/product'
import type { AuthResponse, LoginPayload, RegisterPayload, UserProfile } from '~/types/auth'

const ACCESS_COOKIE = 'vb_at'
const REFRESH_COOKIE = 'vb_rt'
const SESSION_MAX_AGE = 60 * 60 * 24 * 7

export const useAuthStore = defineStore('auth', () => {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase as string
  const cookieOptions = {
    maxAge: SESSION_MAX_AGE,
    sameSite: 'lax' as const,
    secure: !import.meta.dev,
    path: '/',
  }
  // Tokens stay in cookies only (not Pinia state) so they are never serialised into the SSR payload.
  const accessToken = useCookie<string | null>(ACCESS_COOKIE, { ...cookieOptions, default: () => null })
  const refreshToken = useCookie<string | null>(REFRESH_COOKIE, { ...cookieOptions, default: () => null })

  const user = ref<UserProfile | null>(null)
  const isLoggedIn = computed(() => !!user.value)
  let refreshing: Promise<boolean> | null = null

  async function post<T>(path: string, body: unknown, token?: string | null) {
    const response = await $fetch<ApiResponse<T>>(path, {
      baseURL,
      method: 'POST',
      body,
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    })
    return response.data
  }

  function setSession(auth: AuthResponse) {
    accessToken.value = auth.accessToken
    refreshToken.value = auth.refreshToken
    user.value = auth.user
  }

  function clearSession() {
    accessToken.value = null
    refreshToken.value = null
    user.value = null
  }

  async function login(payload: LoginPayload) {
    try {
      setSession(await post<AuthResponse>('/auth/login', payload))
    }
    catch (error) {
      throw toApiError(error)
    }
  }

  async function register(payload: RegisterPayload) {
    try {
      setSession(await post<AuthResponse>('/auth/register', payload))
    }
    catch (error) {
      throw toApiError(error)
    }
  }

  /** Exchanges the refresh token for a new pair. Concurrent callers share one request. */
  function refresh(): Promise<boolean> {
    if (!refreshToken.value) return Promise.resolve(false)
    refreshing ??= post<AuthResponse>('/auth/refresh', { refreshToken: refreshToken.value })
      .then((auth) => {
        setSession(auth)
        return true
      })
      .catch((error) => {
        if (isAuthFailure(error)) clearSession()
        return false
      })
      .finally(() => {
        refreshing = null
      })
    return refreshing
  }

  async function fetchMe() {
    const response = await $fetch<ApiResponse<UserProfile>>('/me', {
      baseURL,
      headers: { Authorization: `Bearer ${accessToken.value}` },
    })
    user.value = response.data
  }

  /** Restores the session from cookies; called once per app load (server and client). */
  async function init() {
    if (user.value || (!accessToken.value && !refreshToken.value)) return
    try {
      if (accessToken.value) {
        await fetchMe()
        return
      }
    }
    catch (error) {
      if (!isAuthFailure(error)) return
    }
    if (await refresh()) {
      await fetchMe().catch(() => undefined)
    }
  }

  function isAuthFailure(error: unknown) {
    const status = toApiError(error).status
    return status === 400 || status === 401
  }

  function logout() {
    clearSession()
  }

  function setUser(profile: UserProfile) {
    user.value = profile
  }

  function getAccessToken() {
    return accessToken.value
  }

  return {
    user,
    isLoggedIn,
    login,
    register,
    refresh,
    init,
    logout,
    setSession,
    setUser,
    getAccessToken,
  }
})
