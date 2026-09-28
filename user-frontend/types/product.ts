export type Category = {
  id: number
  nameKm: string
  nameEn: string
  slug: string
  imageUrl: string | null
  sortOrder: number
}

export type Brand = {
  id: number
  name: string
  slug: string
  logoUrl: string | null
}

export type ProductSummary = {
  id: number
  nameKm: string
  nameEn: string
  slug: string
  brandName: string | null
  categorySlug: string | null
  primaryImageUrl: string | null
  price: number
  compareAtPrice: number | null
  stockQty: number
  featured: boolean
  ageRange: string | null
}

export type ProductImage = {
  id: number
  url: string
  sortOrder: number
  primary: boolean
}

export type ProductVariant = {
  id: number
  sku: string
  optionName: string
  price: number
  compareAtPrice: number | null
  stockQty: number
  expiryDate: string | null
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
  category: Category
  brand: Brand | null
  images: ProductImage[]
  variants: ProductVariant[]
}

export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type ApiResponse<T> = {
  success: boolean
  data: T
  message: string | null
  errors?: unknown
}
