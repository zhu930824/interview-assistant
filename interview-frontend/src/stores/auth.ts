import { ref, computed } from 'vue'
import { authApi } from '../api/auth'
import type { UserInfo, LoginRequest, RegisterRequest } from '../types/auth'

const TOKEN_KEY = 'auth_token'
const USER_KEY = 'auth_user'

const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
const user = ref<UserInfo | null>(null)

// Initialize user from localStorage
const storedUser = localStorage.getItem(USER_KEY)
if (storedUser) {
  try {
    user.value = JSON.parse(storedUser)
  } catch {
    localStorage.removeItem(USER_KEY)
  }
}

export function useAuth() {
  const isAuthenticated = computed(() => !!token.value)

  const login = async (credentials: LoginRequest) => {
    const res = await authApi.login(credentials)
    const data = res.data.data
    token.value = data.token
    user.value = { id: 0, username: data.username, role: data.role }
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(USER_KEY, JSON.stringify(user.value))
    return data
  }

  const register = async (credentials: RegisterRequest) => {
    await authApi.register(credentials)
  }

  const logout = () => {
    token.value = null
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  const fetchUser = async () => {
    if (!token.value) return null
    try {
      const res = await authApi.me()
      user.value = res.data.data
      localStorage.setItem(USER_KEY, JSON.stringify(user.value))
      return user.value
    } catch {
      logout()
      return null
    }
  }

  const getToken = () => token.value

  return {
    token,
    user,
    isAuthenticated,
    login,
    register,
    logout,
    fetchUser,
    getToken,
  }
}
