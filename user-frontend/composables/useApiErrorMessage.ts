/** Translates an API error into a user-facing message using `errors.<CODE>` i18n keys. */
export function useApiErrorMessage() {
  const { t, te } = useI18n()

  return function message(error: unknown) {
    const { code } = toApiError(error)
    if (code && te(`errors.${code}`)) return t(`errors.${code}`)
    return t('errors.GENERIC')
  }
}
