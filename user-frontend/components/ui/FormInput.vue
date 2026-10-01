<script setup lang="ts">
const props = withDefaults(defineProps<{
  label: string
  type?: string
  error?: string | null
  hint?: string | null
  autocomplete?: string
  required?: boolean
  placeholder?: string
  inputmode?: 'text' | 'email' | 'tel' | 'numeric'
  maxlength?: number
}>(), {
  type: 'text',
  error: null,
  hint: null,
  autocomplete: 'off',
  required: false,
  placeholder: '',
  inputmode: undefined,
  maxlength: undefined,
})

const model = defineModel<string>({ default: '' })
const emit = defineEmits<{ blur: [] }>()

const { t } = useI18n()
const id = useId()
const revealed = ref(false)
const isPassword = computed(() => props.type === 'password')
const inputType = computed(() => (isPassword.value && revealed.value ? 'text' : props.type))
const describedBy = computed(() => (props.error ? `${id}-error` : props.hint ? `${id}-hint` : undefined))
</script>

<template>
  <div>
    <label :for="id" class="mb-1.5 block text-sm font-semibold text-ink">
      {{ label }}
      <span v-if="required" class="text-brand" aria-hidden="true">*</span>
    </label>
    <div class="relative">
      <input
        :id="id"
        v-model="model"
        :type="inputType"
        :autocomplete="autocomplete"
        :required="required"
        :placeholder="placeholder"
        :inputmode="inputmode"
        :maxlength="maxlength"
        :aria-invalid="!!error"
        :aria-describedby="describedBy"
        class="min-h-12 w-full rounded-2xl border bg-white px-4 text-sm text-ink outline-none transition placeholder:text-muted/70 focus:border-brand focus:ring-4 focus:ring-brand/10"
        :class="[error ? 'border-danger' : 'border-line', isPassword ? 'pr-12' : '']"
        @blur="emit('blur')"
      >
      <button
        v-if="isPassword"
        type="button"
        class="absolute inset-y-0 right-0 flex w-12 items-center justify-center text-muted transition hover:text-ink"
        :aria-label="revealed ? t('auth.hidePassword') : t('auth.showPassword')"
        :aria-pressed="revealed"
        @click="revealed = !revealed"
      >
        <svg v-if="revealed" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
          <path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.22A10.48 10.48 0 0 0 1.93 12c1.3 4.34 5.31 7.5 10.07 7.5.99 0 1.95-.14 2.86-.4M6.23 6.23A10.45 10.45 0 0 1 12 4.5c4.76 0 8.77 3.16 10.07 7.5a10.52 10.52 0 0 1-4.29 5.77M6.23 6.23 3 3m3.23 3.23 3.65 3.65m7.89 7.89L21 21m-3.23-3.23-3.65-3.65m0 0a3 3 0 1 0-4.24-4.24m4.24 4.24L9.88 9.88" />
        </svg>
        <svg v-else class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
          <path stroke-linecap="round" stroke-linejoin="round" d="M2.04 12.32a1 1 0 0 1 0-.64C3.42 7.51 7.36 4.5 12 4.5s8.57 3.01 9.96 7.18a1 1 0 0 1 0 .64C20.58 16.49 16.64 19.5 12 19.5s-8.57-3.01-9.96-7.18Z" />
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" />
        </svg>
      </button>
    </div>
    <p v-if="error" :id="`${id}-error`" class="mt-1.5 text-xs font-medium text-danger" role="alert">
      {{ error }}
    </p>
    <p v-else-if="hint" :id="`${id}-hint`" class="mt-1.5 text-xs text-muted">
      {{ hint }}
    </p>
  </div>
</template>
