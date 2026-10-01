import type { Brand, Category } from '~/types/admin'

/** Category and brand lists shared by product filters and forms (cached per session). */
export function useCatalogOptions() {
  const { apiFetch } = useApi()
  const categories = useAsyncData('admin-categories', () => apiFetch<Category[]>('/admin/categories'), {
    default: () => [] as Category[],
  })
  const brands = useAsyncData('admin-brands', () => apiFetch<Brand[]>('/admin/brands'), {
    default: () => [] as Brand[],
  })
  return { categories, brands }
}
