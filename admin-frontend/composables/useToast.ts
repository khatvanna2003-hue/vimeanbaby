export type Toast = {
  id: number
  variant: 'success' | 'error' | 'info'
  message: string
}

let nextId = 1

export function useToast() {
  const toasts = useState<Toast[]>('admin-toasts', () => [])

  function dismiss(id: number) {
    toasts.value = toasts.value.filter(toast => toast.id !== id)
  }

  function push(variant: Toast['variant'], message: string, timeout = 4000) {
    const id = nextId++
    toasts.value = [...toasts.value, { id, variant, message }]
    if (import.meta.client) window.setTimeout(() => dismiss(id), timeout)
  }

  return {
    toasts,
    dismiss,
    success: (message: string) => push('success', message),
    error: (message: string) => push('error', message, 6000),
    info: (message: string) => push('info', message),
  }
}
