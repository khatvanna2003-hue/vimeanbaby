<script setup lang="ts">
import type { ProductDetail, ProductImage, ProductPayload, ProductVariant } from '~/types/admin'

const props = defineProps<{
  product: ProductDetail | null
  saving: boolean
}>()

const emit = defineEmits<{ submit: [payload: ProductPayload] }>()

const { t } = useI18n()
const toast = useToast()
const { categories, brands } = useCatalogOptions()
const { uploadImage, uploading } = useUpload()

function emptyVariant(): ProductVariant {
  return { sku: '', optionName: '', price: null, compareAtPrice: null, stockQty: 0, expiryDate: null, active: true }
}

const form = reactive<ProductPayload>({
  categoryId: props.product?.category?.id ?? null,
  brandId: props.product?.brand?.id ?? null,
  nameEn: props.product?.nameEn ?? '',
  nameKm: props.product?.nameKm ?? '',
  slug: props.product?.slug ?? '',
  descriptionEn: props.product?.descriptionEn ?? '',
  descriptionKm: props.product?.descriptionKm ?? '',
  ageRange: props.product?.ageRange ?? '',
  originCountry: props.product?.originCountry ?? '',
  featured: props.product?.featured ?? false,
  active: props.product?.active ?? true,
  images: (props.product?.images ?? []).map(image => ({ ...image })),
  variants: props.product?.variants.length
    ? props.product.variants.map(variant => ({ ...variant }))
    : [emptyVariant()],
})

const slugTouched = ref(!!props.product)
watch(() => form.nameEn, (value) => {
  if (!slugTouched.value) form.slug = slugify(value)
})

const submitted = ref(false)
const errors = computed(() => {
  if (!submitted.value) return {} as Record<string, string>
  const result: Record<string, string> = {}
  const required = t('validation.required')
  if (!form.nameEn.trim()) result.nameEn = required
  if (!form.nameKm.trim()) result.nameKm = required
  if (!form.slug.trim()) result.slug = required
  else if (!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(form.slug)) result.slug = t('validation.slug')
  if (!form.categoryId) result.categoryId = required
  const skus = new Set<string>()
  form.variants.forEach((variant, index) => {
    if (!variant.optionName.trim()) result[`v${index}.optionName`] = required
    const sku = variant.sku.trim().toUpperCase()
    if (!sku) result[`v${index}.sku`] = required
    else if (skus.has(sku)) result[`v${index}.sku`] = t('validation.duplicateSku')
    skus.add(sku)
    if (variant.price === null || Number.isNaN(Number(variant.price)) || Number(variant.price) < 0) result[`v${index}.price`] = t('validation.price')
    if (variant.compareAtPrice !== null && Number(variant.compareAtPrice) <= Number(variant.price ?? 0)) result[`v${index}.compareAtPrice`] = t('validation.compareAt')
    if (variant.stockQty === null || Number(variant.stockQty) < 0 || !Number.isInteger(Number(variant.stockQty))) result[`v${index}.stockQty`] = t('validation.stock')
  })
  return result
})

function addVariant() {
  form.variants.push(emptyVariant())
}

function removeVariant(index: number) {
  if (form.variants.length > 1) form.variants.splice(index, 1)
}

const fileInput = ref<HTMLInputElement | null>(null)

async function onFiles(event: Event) {
  const files = Array.from((event.target as HTMLInputElement).files ?? [])
  for (const file of files) {
    try {
      const result = await uploadImage(file, 'vimeanbaby/products')
      form.images.push({
        url: result.secureUrl,
        cloudinaryPublicId: result.publicId,
        sortOrder: form.images.length,
        primary: form.images.length === 0,
      })
    }
    catch (error) {
      toast.error(`${file.name}: ${error instanceof Error ? error.message : t('upload.failed')}`)
    }
  }
  if (fileInput.value) fileInput.value.value = ''
}

function setPrimary(index: number) {
  form.images.forEach((image, i) => {
    image.primary = i === index
  })
}

function moveImage(index: number, delta: number) {
  const target = index + delta
  if (target < 0 || target >= form.images.length) return
  const [image] = form.images.splice(index, 1)
  if (image) form.images.splice(target, 0, image)
}

function removeImage(index: number) {
  const [removed] = form.images.splice(index, 1)
  if (removed?.primary && form.images[0]) form.images[0].primary = true
}

function blankToNull(value: string | null) {
  return value && value.trim() ? value.trim() : null
}

function submit() {
  submitted.value = true
  if (Object.keys(errors.value).length) {
    toast.error(t('validation.fixErrors'))
    return
  }
  const images: ProductImage[] = form.images.map((image, index) => ({
    url: image.url,
    cloudinaryPublicId: image.cloudinaryPublicId,
    sortOrder: index,
    primary: image.primary,
  }))
  emit('submit', {
    ...form,
    nameEn: form.nameEn.trim(),
    nameKm: form.nameKm.trim(),
    slug: form.slug.trim(),
    descriptionEn: blankToNull(form.descriptionEn),
    descriptionKm: blankToNull(form.descriptionKm),
    ageRange: blankToNull(form.ageRange),
    originCountry: blankToNull(form.originCountry),
    images,
    variants: form.variants.map(variant => ({
      sku: variant.sku.trim().toUpperCase(),
      optionName: variant.optionName.trim(),
      price: Number(variant.price),
      compareAtPrice: variant.compareAtPrice === null ? null : Number(variant.compareAtPrice),
      stockQty: Number(variant.stockQty),
      expiryDate: variant.expiryDate || null,
      active: variant.active,
    })),
  })
}

function numberOrNull(value: string) {
  return value === '' ? null : Number(value)
}
</script>

<template>
  <form class="grid gap-6 xl:grid-cols-3" novalidate @submit.prevent="submit">
    <div class="space-y-6 xl:col-span-2">
      <section class="card p-5 sm:p-6">
        <h2 class="mb-5 font-bold text-ink">{{ t('products.sections.details') }}</h2>
        <div class="grid gap-5 sm:grid-cols-2">
          <div>
            <label class="field-label" for="p-name-en">{{ t('products.nameEn') }} *</label>
            <input id="p-name-en" v-model="form.nameEn" class="field-input" :class="errors.nameEn ? 'border-danger' : ''" maxlength="255">
            <p v-if="errors.nameEn" class="field-error">{{ errors.nameEn }}</p>
          </div>
          <div>
            <label class="field-label" for="p-name-km">{{ t('products.nameKm') }} *</label>
            <input id="p-name-km" v-model="form.nameKm" class="field-input" :class="errors.nameKm ? 'border-danger' : ''" maxlength="255">
            <p v-if="errors.nameKm" class="field-error">{{ errors.nameKm }}</p>
          </div>
          <div class="sm:col-span-2">
            <label class="field-label" for="p-slug">{{ t('products.slug') }} *</label>
            <div class="flex overflow-hidden rounded-xl border focus-within:border-brand focus-within:ring-4 focus-within:ring-brand/10" :class="errors.slug ? 'border-danger' : 'border-line'">
              <span class="hidden items-center border-r border-line bg-surface px-3 text-xs text-ink-muted sm:flex">/products/</span>
              <input id="p-slug" v-model="form.slug" class="min-h-10 w-full px-3 text-sm outline-none" maxlength="200" @input="slugTouched = true">
            </div>
            <p v-if="errors.slug" class="field-error">{{ errors.slug }}</p>
          </div>
          <div>
            <label class="field-label" for="p-desc-en">{{ t('products.descriptionEn') }}</label>
            <textarea id="p-desc-en" v-model="form.descriptionEn" rows="5" class="field-input py-2" />
          </div>
          <div>
            <label class="field-label" for="p-desc-km">{{ t('products.descriptionKm') }}</label>
            <textarea id="p-desc-km" v-model="form.descriptionKm" rows="5" class="field-input py-2" />
          </div>
        </div>
      </section>

      <section class="card p-5 sm:p-6">
        <div class="mb-5 flex items-center justify-between gap-4">
          <div>
            <h2 class="font-bold text-ink">{{ t('products.sections.images') }}</h2>
            <p class="text-xs text-ink-muted">{{ t('products.imagesHint') }}</p>
          </div>
          <button type="button" class="btn-secondary" :disabled="uploading" @click="fileInput?.click()">
            <AppIcon name="upload" :size="16" />
            {{ uploading ? t('upload.uploading') : t('products.addImages') }}
          </button>
          <input ref="fileInput" type="file" accept="image/jpeg,image/png,image/webp" multiple class="hidden" @change="onFiles">
        </div>
        <div v-if="!form.images.length" class="flex flex-col items-center rounded-xl border-2 border-dashed border-line px-6 py-10 text-center">
          <AppIcon name="photo" :size="32" class="text-ink-muted" />
          <p class="mt-2 text-sm text-ink-muted">{{ t('products.noImages') }}</p>
        </div>
        <ul v-else class="grid grid-cols-2 gap-3 sm:grid-cols-4">
          <li
            v-for="(image, index) in form.images"
            :key="image.url"
            class="group relative overflow-hidden rounded-xl border-2 bg-white"
            :class="image.primary ? 'border-brand' : 'border-line'"
          >
            <img :src="thumb(image.url, 240)" alt="" class="aspect-square w-full object-contain" loading="lazy">
            <span v-if="image.primary" class="badge-brand absolute left-2 top-2">{{ t('products.primary') }}</span>
            <div class="absolute inset-x-0 bottom-0 flex justify-between gap-1 bg-white/95 p-1.5 opacity-100 transition sm:opacity-0 sm:group-hover:opacity-100">
              <button type="button" class="btn-icon h-8 w-8" :aria-label="t('products.moveLeft')" :disabled="index === 0" @click="moveImage(index, -1)"><AppIcon name="chevronLeft" :size="16" /></button>
              <button v-if="!image.primary" type="button" class="btn-icon h-8 w-8" :aria-label="t('products.makePrimary')" @click="setPrimary(index)"><AppIcon name="star" :size="16" /></button>
              <button type="button" class="btn-icon h-8 w-8 hover:text-danger" :aria-label="t('common.remove')" @click="removeImage(index)"><AppIcon name="trash" :size="16" /></button>
              <button type="button" class="btn-icon h-8 w-8" :aria-label="t('products.moveRight')" :disabled="index === form.images.length - 1" @click="moveImage(index, 1)"><AppIcon name="chevronRight" :size="16" /></button>
            </div>
          </li>
        </ul>
      </section>

      <section class="card overflow-hidden">
        <div class="flex items-center justify-between gap-4 p-5 sm:px-6">
          <div>
            <h2 class="font-bold text-ink">{{ t('products.sections.variants') }}</h2>
            <p class="text-xs text-ink-muted">{{ t('products.variantsHint') }}</p>
          </div>
          <button type="button" class="btn-secondary" @click="addVariant">
            <AppIcon name="plus" :size="16" /> {{ t('products.addVariant') }}
          </button>
        </div>
        <div class="space-y-3 border-t border-line/70 p-4 sm:p-5">
          <div v-for="(variant, index) in form.variants" :key="index" class="rounded-xl border border-line/80 p-4">
            <div class="mb-3 flex items-center justify-between">
              <p class="text-sm font-semibold text-ink">{{ t('products.variantN', { n: index + 1 }) }}</p>
              <div class="flex items-center gap-3">
                <label class="flex items-center gap-2 text-xs font-semibold text-ink-muted">
                  <input v-model="variant.active" type="checkbox" class="h-4 w-4 accent-brand">
                  {{ t('common.active') }}
                </label>
                <button v-if="form.variants.length > 1" type="button" class="btn-icon h-8 w-8 hover:text-danger" :aria-label="t('common.remove')" @click="removeVariant(index)">
                  <AppIcon name="trash" :size="16" />
                </button>
              </div>
            </div>
            <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              <div>
                <label class="field-label text-xs" :for="`v-opt-${index}`">{{ t('products.optionName') }} *</label>
                <input :id="`v-opt-${index}`" v-model="variant.optionName" class="field-input" :class="errors[`v${index}.optionName`] ? 'border-danger' : ''" :placeholder="t('products.optionPlaceholder')" maxlength="120">
                <p v-if="errors[`v${index}.optionName`]" class="field-error">{{ errors[`v${index}.optionName`] }}</p>
              </div>
              <div>
                <label class="field-label text-xs" :for="`v-sku-${index}`">SKU *</label>
                <input :id="`v-sku-${index}`" v-model="variant.sku" class="field-input uppercase" :class="errors[`v${index}.sku`] ? 'border-danger' : ''" maxlength="80">
                <p v-if="errors[`v${index}.sku`]" class="field-error">{{ errors[`v${index}.sku`] }}</p>
              </div>
              <div>
                <label class="field-label text-xs" :for="`v-exp-${index}`">{{ t('products.expiryDate') }}</label>
                <input :id="`v-exp-${index}`" v-model="variant.expiryDate" type="date" class="field-input">
              </div>
              <div>
                <label class="field-label text-xs" :for="`v-price-${index}`">{{ t('products.price') }} (USD) *</label>
                <input :id="`v-price-${index}`" :value="variant.price ?? ''" type="number" min="0" step="0.01" inputmode="decimal" class="field-input" :class="errors[`v${index}.price`] ? 'border-danger' : ''" @input="variant.price = numberOrNull(($event.target as HTMLInputElement).value)">
                <p v-if="errors[`v${index}.price`]" class="field-error">{{ errors[`v${index}.price`] }}</p>
              </div>
              <div>
                <label class="field-label text-xs" :for="`v-cmp-${index}`">{{ t('products.compareAtPrice') }}</label>
                <input :id="`v-cmp-${index}`" :value="variant.compareAtPrice ?? ''" type="number" min="0" step="0.01" inputmode="decimal" class="field-input" :class="errors[`v${index}.compareAtPrice`] ? 'border-danger' : ''" @input="variant.compareAtPrice = numberOrNull(($event.target as HTMLInputElement).value)">
                <p v-if="errors[`v${index}.compareAtPrice`]" class="field-error">{{ errors[`v${index}.compareAtPrice`] }}</p>
              </div>
              <div>
                <label class="field-label text-xs" :for="`v-stock-${index}`">{{ t('products.stock') }} *</label>
                <input :id="`v-stock-${index}`" :value="variant.stockQty ?? ''" type="number" min="0" step="1" inputmode="numeric" class="field-input" :class="errors[`v${index}.stockQty`] ? 'border-danger' : ''" @input="variant.stockQty = numberOrNull(($event.target as HTMLInputElement).value)">
                <p v-if="errors[`v${index}.stockQty`]" class="field-error">{{ errors[`v${index}.stockQty`] }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <div class="space-y-6">
      <section class="card space-y-5 p-5 sm:p-6">
        <h2 class="font-bold text-ink">{{ t('products.sections.status') }}</h2>
        <UiToggle v-model="form.active" :label="t('products.published')" :description="t('products.publishedHint')" />
        <UiToggle v-model="form.featured" :label="t('products.featured')" :description="t('products.featuredHint')" />
      </section>

      <section class="card space-y-5 p-5 sm:p-6">
        <h2 class="font-bold text-ink">{{ t('products.sections.organization') }}</h2>
        <div>
          <label class="field-label" for="p-category">{{ t('products.category') }} *</label>
          <select id="p-category" v-model="form.categoryId" class="field-input" :class="errors.categoryId ? 'border-danger' : ''">
            <option :value="null" disabled>{{ t('products.selectCategory') }}</option>
            <option v-for="category in categories.data.value" :key="category.id" :value="category.id">
              {{ category.nameEn }}{{ category.active ? '' : ` (${t('common.inactive')})` }}
            </option>
          </select>
          <p v-if="errors.categoryId" class="field-error">{{ errors.categoryId }}</p>
        </div>
        <div>
          <label class="field-label" for="p-brand">{{ t('products.brand') }}</label>
          <select id="p-brand" v-model="form.brandId" class="field-input">
            <option :value="null">{{ t('products.noBrand') }}</option>
            <option v-for="brand in brands.data.value" :key="brand.id" :value="brand.id">
              {{ brand.name }}{{ brand.active ? '' : ` (${t('common.inactive')})` }}
            </option>
          </select>
        </div>
        <div>
          <label class="field-label" for="p-age">{{ t('products.ageRange') }}</label>
          <input id="p-age" v-model="form.ageRange" class="field-input" :placeholder="t('products.agePlaceholder')" maxlength="50">
        </div>
        <div>
          <label class="field-label" for="p-origin">{{ t('products.origin') }}</label>
          <input id="p-origin" v-model="form.originCountry" class="field-input" :placeholder="t('products.originPlaceholder')" maxlength="100">
        </div>
      </section>

      <div class="card sticky bottom-4 flex gap-2 p-4">
        <NuxtLink to="/products" class="btn-secondary flex-1">{{ t('common.cancel') }}</NuxtLink>
        <button type="submit" class="btn-primary flex-1" :disabled="saving || uploading">
          <span v-if="saving" class="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" aria-hidden="true" />
          {{ product ? t('common.saveChanges') : t('products.create') }}
        </button>
      </div>
    </div>
  </form>
</template>
