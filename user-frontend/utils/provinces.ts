/** Cambodia's 25 provinces. `value` is stored in the DB; `key` maps to i18n `provinces.*`. */
export const PROVINCES = [
  { key: 'phnomPenh', value: 'Phnom Penh' },
  { key: 'banteayMeanchey', value: 'Banteay Meanchey' },
  { key: 'battambang', value: 'Battambang' },
  { key: 'kampongCham', value: 'Kampong Cham' },
  { key: 'kampongChhnang', value: 'Kampong Chhnang' },
  { key: 'kampongSpeu', value: 'Kampong Speu' },
  { key: 'kampongThom', value: 'Kampong Thom' },
  { key: 'kampot', value: 'Kampot' },
  { key: 'kandal', value: 'Kandal' },
  { key: 'kep', value: 'Kep' },
  { key: 'kohKong', value: 'Koh Kong' },
  { key: 'kratie', value: 'Kratie' },
  { key: 'mondulkiri', value: 'Mondulkiri' },
  { key: 'oddarMeanchey', value: 'Oddar Meanchey' },
  { key: 'pailin', value: 'Pailin' },
  { key: 'preahSihanouk', value: 'Preah Sihanouk' },
  { key: 'preahVihear', value: 'Preah Vihear' },
  { key: 'preyVeng', value: 'Prey Veng' },
  { key: 'pursat', value: 'Pursat' },
  { key: 'ratanakiri', value: 'Ratanakiri' },
  { key: 'siemReap', value: 'Siem Reap' },
  { key: 'stungTreng', value: 'Stung Treng' },
  { key: 'svayRieng', value: 'Svay Rieng' },
  { key: 'takeo', value: 'Takeo' },
  { key: 'tbongKhmum', value: 'Tbong Khmum' },
] as const

export function provinceKey(value: string) {
  return PROVINCES.find(p => p.value === value)?.key ?? null
}
