import { defineStore } from 'pinia'
import type { ProductDetail, ProductSummary, ProductVariant } from '~/types/product'

export type CartLine = {
  productId: number
  variantId: number
  slug: string
  name: string
  brand: string
  price: number
  image: string
  optionName: string
  quantity: number
}

const CART_COOKIE = 'vb_cart'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [] as CartLine[],
    open: false,
    hydrated: false,
  }),
  getters: {
    count: (state) => state.items.reduce((sum, item) => sum + item.quantity, 0),
    subtotal: (state) => state.items.reduce((sum, item) => sum + item.price * item.quantity, 0),
  },
  actions: {
    hydrate() {
      if (import.meta.server || this.hydrated) return
      const cookie = useCookie<CartLine[]>(CART_COOKIE, {
        default: () => [],
        maxAge: 60 * 60 * 24 * 30,
        sameSite: 'lax',
      })
      this.items = Array.isArray(cookie.value) ? cookie.value : []
      this.hydrated = true
    },
    persist() {
      if (import.meta.server) return
      const cookie = useCookie<CartLine[]>(CART_COOKIE, {
        default: () => [],
        maxAge: 60 * 60 * 24 * 30,
        sameSite: 'lax',
      })
      cookie.value = this.items
    },
    toggle(force?: boolean) {
      this.open = typeof force === 'boolean' ? force : !this.open
    },
    addSummary(product: ProductSummary, name: string) {
      this.addLine({
        productId: product.id,
        variantId: product.id,
        slug: product.slug,
        name,
        brand: product.brandName || '',
        price: Number(product.price),
        image: product.primaryImageUrl || '',
        optionName: 'Default',
        quantity: 1,
      })
    },
    addDetail(product: ProductDetail, variant: ProductVariant, name: string) {
      this.addLine({
        productId: product.id,
        variantId: variant.id,
        slug: product.slug,
        name,
        brand: product.brand?.name || '',
        price: Number(variant.price),
        image: product.images.find(i => i.primary)?.url || product.images[0]?.url || '',
        optionName: variant.optionName,
        quantity: 1,
      })
    },
    addLine(line: CartLine) {
      this.hydrate()
      const existing = this.items.find(item => item.variantId === line.variantId)
      if (existing) {
        existing.quantity += line.quantity
      }
      else {
        this.items.push({ ...line })
      }
      this.open = true
      this.persist()
    },
    setQuantity(variantId: number, quantity: number) {
      this.hydrate()
      const line = this.items.find(item => item.variantId === variantId)
      if (!line) return
      if (quantity <= 0) {
        this.items = this.items.filter(item => item.variantId !== variantId)
      }
      else {
        line.quantity = quantity
      }
      this.persist()
    },
    remove(variantId: number) {
      this.hydrate()
      this.items = this.items.filter(item => item.variantId !== variantId)
      this.persist()
    },
  },
})
