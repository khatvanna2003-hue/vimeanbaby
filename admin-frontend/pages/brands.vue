<script setup lang="ts">
import type { Brand } from '~/types/admin'

const { t } = useI18n()
const { apiFetch } = useApi()
const toast = useToast()
const { confirm } = useConfirm()
const errorMessage = useApiErrorMessage()

useHead({ title: () => `${t('nav.brands')} | Vimean Baby Admin` })

const { brands } = useCatalogOptions()
const { data, status, error, refresh } = brands

type BrandForm = Omit<Brand, 'id'>
const blank = (): BrandForm => ({ name: '', slug: '', logoUrl: null, active: true })

const modalOpen = ref(false)
const editing = ref<Brand | null>(null)
const form = reactive<BrandForm>(blank())
const slugTouched = ref(false)
const submitted = ref(false)
const saving = ref(false)
const search = ref('')

const filtered = computed(() => {
  const q = search.value.trim().toLowerCase()
  const rows = data.value ?? []
  return q ? rows.filter(b => `${b.name} ${b.slug}`.toLowerCase().includes(q)) : rows
})

watch(() => form.name, (value) => {
  if (!slugTouched.value) form.slug = slugify(value)
})

const errors = computed(() => {
  if (!submitted.value) return {} as Record<string, string>
  const result: Record<string, string> = {}
  if (!form.name.trim()) result.name = t('validation.required')
  if (!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(form.slug)) result.slug = t('validation.slug')
  return result
})

function open(brand: Brand | null) {
  editing.value = brand
  Object.assign(form, brand ? { ...brand } : blank())
  slugTouched.value = !!brand
  submitted.value = false
  modalOpen.value = true
}

async function save() {
  submitted.value = true
  if (Object.keys(errors.value).length) return
  saving.value = true
  try {
    const body = { ...form, name: form.name.trim() }
    if (editing.value) {
      await apiFetch(`/admin/brands/${editing.value.id}`, { method: 'PUT', body })
      toast.success(t('brands.saved'))
    }
    else {
      await apiFetch('/admin/brands', { method: 'POST', body })
      toast.success(t('brands.created'))
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

async function toggleActive(brand: Brand) {
  if (brand.active) {
    const ok = await confirm({
      title: t('brands.deactivateTitle'),
      message: t('brands.deactivateMessage', { name: brand.name }),
      confirmLabel: t('common.deactivate'),
      danger: true,
    })
    if (!ok) return
  }
  try {
    if (brand.active) {
      await apiFetch(`/admin/brands/${brand.id}`, { method: 'DELETE' })
    }
    else {
      await apiFetch(`/admin/brands/${brand.id}`, { method: 'PUT', body: { ...brand, active: true } })
    }
    toast.success(brand.active ? t('common.deactivated') : t('common.activated'))
    await refresh()
  }
  catch (err) {
    toast.error(errorMessage(err))
  }
}
</script>

<template>
  <div>
    <PageHeader :title="t('nav.brands')" :description="t('brands.subtitle')">
      <template #actions>
        <button type="button" class="btn-primary" @click="open(null)">
          <AppIcon name="plus" :size="16" /> {{ t('brands.add') }}
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
      <div v-else-if="status === 'pending' && !data?.length" class="grid gap-3 p-4 sm:grid-cols-2 lg:grid-cols-3" aria-busy="true">
        <div v-for="i in 6" :key="i" class="h-20 animate-pulse rounded-xl bg-surface" />
      </div>
      <EmptyState v-else-if="!filtered.length" icon="brands" :title="search ? t('common.noResults') : t('brands.empty')">
        <button v-if="!search" type="button" class="btn-primary" @click="open(null)">{{ t('brands.add') }}</button>
      </EmptyState>
      <ul v-else class="grid gap-3 p-4 sm:grid-cols-2 lg:grid-cols-3">
        <li v-for="brand in filtered" :key="brand.id" class="flex items-center gap-3 rounded-xl border border-line/80 p-3" :class="brand.active ? '' : 'opacity-70'">
          <div class="flex h-12 w-12 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-line bg-white">
            <img v-if="brand.logoUrl" :src="thumb(brand.logoUrl, 96)" alt="" class="h-full w-full object-contain" loading="lazy">
            <span v-else class="text-sm font-bold text-ink-muted">{{ initials(brand.name) }}</span>
          </div>
          <div class="min-w-0 flex-1">
            <p class="truncate font-semibold text-ink">{{ brand.name }}</p>
            <span :class="brand.active ? 'badge-success' : 'badge-neutral'" class="mt-1">{{ brand.active ? t('common.active') : t('common.inactive') }}</span>
          </div>
          <div class="flex flex-col gap-1">
            <button type="button" class="btn-icon" :aria-label="t('common.edit')" @click="open(brand)"><AppIcon name="edit" :size="18" /></button>
            <button type="button" class="btn-icon" :class="brand.active ? 'hover:text-danger' : 'hover:text-ink'" :aria-label="brand.active ? t('common.deactivate') : t('common.activate')" @click="toggleActive(brand)">
              <AppIcon :name="brand.active ? 'trash' : 'refresh'" :size="18" />
            </button>
          </div>
        </li>
      </ul>
    </div>

    <UiModal :open="modalOpen" :title="editing ? t('brands.edit') : t('brands.add')" size="sm" @close="modalOpen = false">
      <form id="brand-form" class="space-y-4" novalidate @submit.prevent="save">
        <div>
          <label class="field-label" for="b-name">{{ t('brands.name') }} *</label>
          <input id="b-name" v-model="form.name" class="field-input" :class="errors.name ? 'border-danger' : ''" maxlength="150">
          <p v-if="errors.name" class="field-error">{{ errors.name }}</p>
        </div>
        <div>
          <label class="field-label" for="b-slug">{{ t('products.slug') }} *</label>
          <input id="b-slug" v-model="form.slug" class="field-input font-mono" :class="errors.slug ? 'border-danger' : ''" maxlength="180" @input="slugTouched = true">
          <p v-if="errors.slug" class="field-error">{{ errors.slug }}</p>
        </div>
        <ImageUploader v-model="form.logoUrl" :label="t('brands.logo')" folder="vimeanbaby/brands" />
        <UiToggle v-model="form.active" :label="t('common.active')" />
      </form>
      <template #footer>
        <button type="button" class="btn-secondary" @click="modalOpen = false">{{ t('common.cancel') }}</button>
        <button type="submit" form="brand-form" class="btn-primary" :disabled="saving">{{ t('common.save') }}</button>
      </template>
    </UiModal>
  </div>
</template>
