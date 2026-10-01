export default defineNuxtPlugin({
  name: 'auth',
  async setup() {
    await useAuthStore().init()
  },
})
