const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/

export function isEmail(value: string) {
  return EMAIL_PATTERN.test(value.trim())
}

/** Mirrors the backend: Cambodian numbers normalise to 9–10 digits starting with 0. */
export function isCambodianPhone(value: string) {
  let digits = value.replace(/\D/g, '')
  if (digits.startsWith('855') && digits.length > 10) digits = digits.slice(3)
  if (!digits.startsWith('0')) digits = `0${digits}`
  return digits.length >= 9 && digits.length <= 10
}

export function isStrongEnoughPassword(value: string) {
  return value.length >= 8 && value.length <= 72 && /[A-Za-z]/.test(value) && /\d/.test(value)
}

/** 0 = empty, 1 = weak, 2 = fair, 3 = good, 4 = strong */
export function passwordScore(value: string) {
  if (!value) return 0
  let score = 0
  if (value.length >= 8) score++
  if (value.length >= 12) score++
  if (/[a-z]/.test(value) && /[A-Z]/.test(value)) score++
  if (/\d/.test(value) && /[^A-Za-z0-9]/.test(value)) score++
  return Math.max(1, Math.min(4, score))
}
