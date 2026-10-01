export type ApiResponse<T> = {
  success: boolean
  data: T
  message: string | null
  errors?: unknown
}

export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type AdminUser = {
  id: number
  fullName: string
  email: string
  phone: string
  role: 'CUSTOMER' | 'ADMIN'
  createdAt: string
  lastLoginAt: string | null
}

export type AuthResponse = {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: AdminUser
}

export type Category = {
  id: number
  nameKm: string
  nameEn: string
  slug: string
  imageUrl: string | null
  sortOrder: number
  active: boolean
}

export type Brand = {
  id: number
  name: string
  slug: string
  logoUrl: string | null
  active: boolean
}

export type ProductSummary = {
  id: number
  nameKm: string
  nameEn: string
  slug: string
  brandName: string | null
  categorySlug: string | null
  categoryName: string | null
  primaryImageUrl: string | null
  price: number
  compareAtPrice: number | null
  stockQty: number
  totalStock: number | null
  variantCount: number | null
  featured: boolean
  active: boolean
  ageRange: string | null
}

export type ProductImage = {
  id?: number
  url: string
  cloudinaryPublicId: string | null
  sortOrder: number
  primary: boolean
}

export type ProductVariant = {
  id?: number
  sku: string
  optionName: string
  price: number | null
  compareAtPrice: number | null
  stockQty: number | null
  expiryDate: string | null
  active: boolean
}

export type ProductDetail = {
  id: number
  nameKm: string
  nameEn: string
  slug: string
  descriptionKm: string | null
  descriptionEn: string | null
  ageRange: string | null
  originCountry: string | null
  featured: boolean
  active: boolean
  category: Category
  brand: Brand | null
  images: ProductImage[]
  variants: ProductVariant[]
}

export type ProductPayload = {
  categoryId: number | null
  brandId: number | null
  nameKm: string
  nameEn: string
  slug: string
  descriptionKm: string | null
  descriptionEn: string | null
  ageRange: string | null
  originCountry: string | null
  featured: boolean
  active: boolean
  images: ProductImage[]
  variants: ProductVariant[]
}

export type InventoryItem = {
  variantId: number
  productId: number
  productNameEn: string
  productNameKm: string
  imageUrl: string | null
  sku: string
  optionName: string
  price: number
  stockQty: number
  expiryDate: string | null
  active: boolean
}

export type CustomerSummary = {
  id: number
  fullName: string
  email: string
  phone: string
  active: boolean
  createdAt: string
  lastLoginAt: string | null
}

export type CustomerAddress = {
  id: number
  receiverName: string
  phone: string
  province: string
  district: string
  commune: string
  streetDetail: string
  note: string | null
  defaultAddress: boolean
  createdAt: string
}

export type CustomerDetail = CustomerSummary & {
  dateOfBirth: string | null
  gender: 'FEMALE' | 'MALE' | 'OTHER' | null
  addresses: CustomerAddress[]
}

export type Dashboard = {
  catalog: { products: number, published: number, drafts: number, categories: number, brands: number }
  inventory: {
    sellableVariants: number
    stockUnits: number
    inventoryValue: number
    lowStock: number
    outOfStock: number
    expiringSoon: number
    lowStockThreshold: number
    expiryWarningDays: number
  }
  customers: { total: number, active: number, newLast30Days: number }
  productsByCategory: Array<{ name: string, count: number }>
  signups: Array<{ date: string, count: number }>
  lowStockItems: InventoryItem[]
  expiringItems: InventoryItem[]
  recentProducts: Array<{ id: number, nameEn: string, nameKm: string, categoryName: string | null, active: boolean, createdAt: string }>
  recentCustomers: CustomerSummary[]
}
