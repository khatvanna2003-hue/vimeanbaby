type ErrorBody = {
  message?: string | null
  errors?: unknown
}

export class ApiError extends Error {
  readonly status: number
  readonly code: string | null
  readonly details: string[]

  constructor(message: string, status: number, code: string | null, details: string[] = []) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.details = details
  }
}

/** Converts a $fetch failure (or anything thrown) into an ApiError carrying the backend error code. */
export function toApiError(error: unknown): ApiError {
  if (error instanceof ApiError) return error
  const fetchError = error as { statusCode?: number, status?: number, data?: ErrorBody, message?: string }
  const status = fetchError?.statusCode ?? fetchError?.status ?? 0
  const body = fetchError?.data
  let code: string | null = null
  let details: string[] = []

  if (body?.errors && typeof body.errors === 'object' && !Array.isArray(body.errors)) {
    const value = (body.errors as Record<string, unknown>).code
    code = typeof value === 'string' ? value : null
  }
  else if (Array.isArray(body?.errors)) {
    details = body.errors.filter((item): item is string => typeof item === 'string')
    code = 'VALIDATION'
  }
  if (!code && status === 0) code = 'NETWORK'

  return new ApiError(body?.message || fetchError?.message || 'Request failed', status, code, details)
}
