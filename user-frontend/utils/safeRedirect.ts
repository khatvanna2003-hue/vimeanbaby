/** Only allows same-site relative paths to prevent open redirects via `?redirect=`. */
export function safeRedirect(value: unknown, fallback: string) {
  if (typeof value !== 'string') return fallback
  if (!value.startsWith('/') || value.startsWith('//') || value.startsWith('/\\')) return fallback
  return value
}
