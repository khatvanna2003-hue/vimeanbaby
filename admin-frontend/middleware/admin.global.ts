export default defineNuxtRouteMiddleware((to) => {
  const auth = useAuthStore()
  if (to.path === '/login') {
    return auth.isAdmin ? navigateTo('/') : undefined
  }
  if (!auth.isAdmin) {
    return navigateTo({ path: '/login', query: to.fullPath !== '/' ? { redirect: to.fullPath } : undefined })
  }
})
