<script setup lang="ts">
import type { Category } from '~/types/admin'

const { t, locale } = useI18n()
const { apiFetch } = useApi()
const toast = useToast()
const { confirm } = useConfirm()
const errorMessage = useApiErrorMessage()

useHead({ title: () => `${t('nav.categories')} | Vimean Baby Admin` })

const { categories } = useCatalogOptions()
const { data, status, error, refresh } = categories

type CategoryForm = Omit<Category, 'id'>
const blank = (): CategoryForm => ({ nameEn: '', nameKm: '', slug: '', imageUrl: null, sortOrder: (data.value?.length ?? 0) + 1, active: true })

const modalOpen = ref(false)
const editing = ref<Category | null>(null)
const form = reactive<CategoryForm>(blank())
const slugTouched = ref(false)
const submitted = ref(false)
const saving = ref(false)
const search = ref('')

const filtered = computed(() => {
  const q = search.value.trim().toLowerCase()
  const rows = data.value ?? []
  return q ? rows.filter(c => `${c.nameEn} ${c.nameKm} ${c.slug}`.toLowerCase().includes(q)) : rows
})

watch(() => form.nameEn, (value) => {
  if (!slugTouched.value) form.slug = slugify(value)
})

const errors = computed(() => {
  if (!submitted.value) return {} as Record<string, string>
  const result: Record<string, string> = {}
  if (!form.nameEn.trim()) result.nameEn = t('validation.required')
  if (!form.nameKm.trim()) result.nameKm = t('validation.required')
  if (!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(form.slug)) result.slug = t('validation.slug')
  return result
})

function open(category: Category | null) {
  editing.value = category
  Object.assign(form, category ? { ...category } : blank())
  slugTouched.value = !!category
  submitted.value = false
  modalOpen.value = true
}

async function save() {
  submitted.value = true
  if (Object.keys(errors.value).length) return
  saving.value = true
  try {
    const body = { ...form, nameEn: form.nameEn.trim(), nameKm: form.nameKm.trim(), sortOrder: Number(form.sortOrder) || 0 }
    if (editing.value) {
      await apiFetch(`/admin/categories/${editing.value.id}`, { method: 'PUT', body })
      toast.success(t('categories.saved'))
    }
    else {
      await apiFetch('/admin/categories', { method: 'POST', body })
      toast.success(t('categories.created'))
    }
    modalOpen.value = false
    await refresh()
  }
  catch (err) {
    toast.error(errorMessage(err))
  }
  finally {
    saving.value = false
  }
}

async function toggleActive(category: Category) {
  if (category.active) {
    const ok = await confirm({
      title: t('categories.deactivateTitle'),
      message: t('categories.deactivateMessage', { name: category.nameEn }),
      confirmLabel: t('common.deactivate'),
      danger: true,
    })
    if (!ok) return
  }
  try {
    if (category.active) {
      await apiFetch(`/admin/categories/${category.id}`, { method: 'DELETE' })
    }
    else {
      await apiFetch(`/admin/categories/${category.id}`, { method: 'PUT', body: { ...category, active: true } })
    }
    toast.success(category.active ? t('common.deactivated') : t('common.activated'))
    await refresh()
  }
  catch (err) {
    toast.error(errorMessage(err))
  }
}
</script>

<template>
  <div>
    <PageHeader :title="t('nav.categories')" :description="t('categories.subtitle')">
      <template #actions>
        <button type="button" class="btn-primary" @click="open(null)">
          <AppIcon name="plus" :size="16" /> {{ t('categories.add') }}
        </button>
      </template>
    </PageHeader>

    <div class="card overflow-hidden">
      <div class="border-b border-line/70 p-4">
        <label class="relative block max-w-md">
          <span class="sr-only">{{ t('common.search') }}</span>
          <AppIcon name="search" :size="18" class="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-ink-muted" />
          <input v-model="search" type="search" class="field-input pl-10" :placeholder="t('common.search')">
        </label>
      </div>

      <div v-if="error" class="p-6">
        <EmptyState icon="alert" :title="t('errors.loadTitle')" :description="errorMessage(error)">
          <button type="button" class="btn-primary" @click="refresh()">{{ t('common.retry') }}</button>
        </EmptyState>
      </div>
      <div v-else-if="status === 'pending' && !data?.length" class="space-y-2 p-4" aria-busy="true">
        <div v-for="i in 6" :key="i" class="h-12 animate-pulse rounded-lg bg-surface" />
      </div>
      <EmptyState v-else-if="!filtered.length" icon="categories" :title="search ? t('common.noResults') : t('categories.empty')">
        <button v-if="!search" type="button" class="btn-primary" @click="open(null)">{{ t('categories.add') }}</button>
      </EmptyState>
      <div v-else class="overflow-x-auto">
        <table class="w-full min-w-[640px]">
          <thead class="table-head">
            <tr>
              <th class="px-4 py-3">{{ t('categories.name') }}</th>
              <th class="px-4 py-3">{{ t('products.slug') }}</th>
              <th class="px-4 py-3 text-right">{{ t('categories.sortOrder') }}</th>
              <th class="px-4 py-3">{{ t('common.status') }}</th>
              <th class="px-4 py-3"><span class="sr-only">{{ t('common.actions') }}</span></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="category in filtered" :key="category.id" class="hover:bg-surface/60">
              <td class="table-cell">
                <div class="flex items-center gap-3">
                  <div class="flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-line bg-white">
                    <img v-if="category.imageUrl" :src="thumb(category.imageUrl, 80)" alt="" class="h-full w-full object-contain" loading="lazy">
                    <AppIcon v-else name="categories" :size="18" class="text-ink-muted" />
                  </div>
                  <div>
                    <p class="font-semibold text-ink">{{ locale === 'km' ? category.nameKm : category.nameEn }}</p>
                    <p class="text-xs text-ink-muted">{{ locale === 'km' ? category.nameEn : category.nameKm }}</p>
                  </div>
                </div>
              </td>
              <td class="table-cell font-mono text-xs text-ink-muted">{{ category.slug }}</td>
              <td class="table-cell text-right">{{ category.sortOrder }}</td>
              <td class="table-cell">
                <span :class="category.active ? 'badge-success' : 'badge-neutral'">{{ category.active ? t('common.active') : t('common.inactive') }}</span>
              </td>
              <td class="table-cell">
                <div class="flex justify-end gap-1">
                  <NuxtLink :to="{ path: '/products', query: { categoryId: category.id } }" class="btn-icon" :aria-label="t('categories.viewProducts')"><AppIcon name="products" :size="18" /></NuxtLink>
                  <button type="button" class="btn-icon" :aria-label="t('common.edit')" @click="open(category)"><AppIcon name="edit" :size="18" /></button>
                  <button type="button" class="btn-secondary min-h-8 px-3 text-xs" @click="toggleActive(category)">
                    {{ category.active ? t('common.deactivate') : t('common.activate') }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <UiModal :open="modalOpen" :title="editing ? t('categories.edit') : t('categories.add')" @close="modalOpen = false">
      <form id="category-form" class="space-y-4" novalidate @submit.prevent="save">
        <div class="grid gap-4 sm:grid-cols-2">
          <div>
            <label class="field-label" for="c-en">{{ t('products.nameEn') }} *</label>
            <input id="c-en" v-model="form.nameEn" class="field-input" :class="errors.nameEn ? 'border-danger' : ''" maxlength="150">
            <p v-if="errors.nameEn" class="field-error">{{ errors.nameEn }}</p>
          </div>
          <div>
            <label class="field-label" for="c-km">{{ t('products.nameKm') }} *</label>
            <input id="c-km" v-model="form.nameKm" class="field-input" :class="errors.nameKm ? 'border-danger' : ''" maxlength="150">
            <p v-if="errors.nameKm" class="field-error">{{ errors.nameKm }}</p>
          </div>
          <div>
            <label class="field-label" for="c-slug">{{ t('products.slug') }} *</label>
            <input id="c-slug" v-model="form.slug" class="field-input font-mono" :class="errors.slug ? 'border-danger' : ''" maxlength="180" @input="slugTouched = true">
            <p v-if="errors.slug" class="field-error">{{ errors.slug }}</p>
          </div>
          <div>
            <label class="field-label" for="c-sort">{{ t('categories.sortOrder') }}</label>
            <input id="c-sort" v-model.number="form.sortOrder" type="number" min="0" class="field-input">
          </div>
        </div>
        <ImageUploader v-model="form.imageUrl" :label="t('categories.image')" folder="vimeanbaby/categories" />
        <UiToggle v-model="form.active" :label="t('common.active')" :description="t('categories.activeHint')" />
      </form>
      <template #footer>
        <button type="button" class="btn-secondary" @click="modalOpen = false">{{ t('common.cancel') }}</button>
        <button type="submit" form="category-form" class="btn-primary" :disabled="saving">{{ t('common.save') }}</button>
      </template>
    </UiModal>
  </div>
</template>
