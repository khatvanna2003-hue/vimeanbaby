/** Translates an API error via `errors.<CODE>`; falls back to the server message for uncoded admin errors. */
export function useApiErrorMessage() {
  const { t, te } = useI18n()

  return function message(error: unknown) {
    const apiError = toApiError(error)
    if (apiError.code && te(`errors.${apiError.code}`)) {
      const base = t(`errors.${apiError.code}`)
      return apiError.details.length ? `${base} (${apiError.details.join('; ')})` : base
    }
    if (apiError.status >= 400 && apiError.status < 500 && apiError.message) return apiError.message
    return t('errors.GENERIC')
  }
}
