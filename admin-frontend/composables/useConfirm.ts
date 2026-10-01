type ConfirmOptions = {
  title: string
  message: string
  confirmLabel?: string
  danger?: boolean
}

type ConfirmState = ConfirmOptions & { open: boolean }

let resolver: ((value: boolean) => void) | null = null

/** Promise-based confirmation dialog rendered once by <ConfirmDialog /> in the layout. */
export function useConfirm() {
  const state = useState<ConfirmState>('admin-confirm', () => ({ open: false, title: '', message: '' }))

  function confirm(options: ConfirmOptions) {
    resolver?.(false)
    state.value = { ...options, open: true }
    return new Promise<boolean>((resolve) => {
      resolver = resolve
    })
  }

  function settle(value: boolean) {
    state.value = { ...state.value, open: false }
    resolver?.(value)
    resolver = null
  }

  return { state, confirm, settle }
}
