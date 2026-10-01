export default defineNuxtRouteMiddleware((to) => {
  const auth = useAuthStore()
  if (auth.isLoggedIn) return
  const localePath = useLocalePath()
  return navigateTo(localePath({ path: '/login', query: { redirect: to.fullPath } }))
})
