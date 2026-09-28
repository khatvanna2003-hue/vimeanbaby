export function formatUsd(amount: number | string | null | undefined) {
  const value = Number(amount ?? 0)
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
  }).format(Number.isFinite(value) ? value : 0)
}

export function formatKhr(amountUsd: number, rate = 4100) {
  const khr = Math.round(Number(amountUsd || 0) * rate)
  return new Intl.NumberFormat('km-KH').format(khr) + ' ៛'
}
