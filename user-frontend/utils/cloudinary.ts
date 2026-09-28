/**
 * Build a Cloudinary delivery URL with f_auto,q_auto and optional width.
 * Passes through non-Cloudinary URLs unchanged.
 */
export function cloudinaryUrl(
  urlOrPublicId: string | null | undefined,
  opts: { width?: number; height?: number; crop?: string } = {},
): string {
  if (!urlOrPublicId) return ''

  const { width, height, crop = 'limit' } = opts
  const transforms = ['f_auto', 'q_auto']
  if (width) transforms.push(`w_${width}`)
  if (height) transforms.push(`h_${height}`)
  if (width || height) transforms.push(`c_${crop}`)
  const t = transforms.join(',')

  // Already a full Cloudinary URL — inject transforms after /upload/
  if (urlOrPublicId.includes('res.cloudinary.com') && urlOrPublicId.includes('/upload/')) {
    return urlOrPublicId.replace('/upload/', `/upload/${t}/`)
  }

  // Absolute non-Cloudinary URL
  if (urlOrPublicId.startsWith('http://') || urlOrPublicId.startsWith('https://') || urlOrPublicId.startsWith('/')) {
    return urlOrPublicId
  }

  // Treat as public_id
  const cloud = 'vimeanbaby'
  return `https://res.cloudinary.com/${cloud}/image/upload/${t}/${urlOrPublicId}`
}
