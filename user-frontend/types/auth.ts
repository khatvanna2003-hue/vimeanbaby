export type Role = 'CUSTOMER' | 'ADMIN'
export type Gender = 'FEMALE' | 'MALE' | 'OTHER'

export type UserProfile = {
  id: number
  fullName: string
  email: string
  phone: string
  role: Role
  dateOfBirth: string | null
  gender: Gender | null
  createdAt: string
  lastLoginAt: string | null
}

export type AuthResponse = {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: UserProfile
}

export type LoginPayload = {
  identifier: string
  password: string
}

export type RegisterPayload = {
  fullName: string
  email: string
  phone: string
  password: string
}

export type UpdateProfilePayload = {
  fullName: string
  email: string
  phone: string
  dateOfBirth: string | null
  gender: Gender | null
}

export type ChangePasswordPayload = {
  currentPassword: string
  newPassword: string
}

export type Address = {
  id: number
  receiverName: string
  phone: string
  province: string
  district: string
  commune: string
  streetDetail: string
  note: string | null
  defaultAddress: boolean
  createdAt: string
}

export type AddressPayload = Omit<Address, 'id' | 'createdAt'>
