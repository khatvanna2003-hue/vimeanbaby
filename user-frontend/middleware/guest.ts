export default defineNuxtRouteMiddleware(() => {
  const auth = useAuthStore()
  if (!auth.isLoggedIn) return
  const localePath = useLocalePath()
  return navigateTo(localePath('/account'))
})
