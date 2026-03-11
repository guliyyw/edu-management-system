import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const TOKEN_KEY = 'auth_token'
const USER_KEY = 'user_info'

export interface UserInfo {
  id: number
  username: string
  realName: string
  role: string
  email?: string
  phone?: string
  teacherId?: number
  staffId?: number
}

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref<string>('')
  const userInfo = ref<UserInfo | null>(null)
  const isLoggedIn = computed(() => !!token.value)

  // Getters
  const isTeacher = computed(() => userInfo.value?.role === 'TEACHER')
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const isStaff = computed(() => userInfo.value?.role === 'STAFF')

  // Actions
  const loadAuth = () => {
    try {
      token.value = uni.getStorageSync(TOKEN_KEY) || ''
      const userStr = uni.getStorageSync(USER_KEY)
      userInfo.value = userStr ? JSON.parse(userStr) : null
    } catch (e) {
      console.error('加载认证信息失败:', e)
    }
  }

  const setAuth = (newToken: string, user: UserInfo) => {
    try {
      uni.setStorageSync(TOKEN_KEY, newToken)
      uni.setStorageSync(USER_KEY, JSON.stringify(user))
      token.value = newToken
      userInfo.value = user
    } catch (e) {
      console.error('保存认证信息失败:', e)
    }
  }

  const clearAuth = () => {
    try {
      uni.removeStorageSync(TOKEN_KEY)
      uni.removeStorageSync(USER_KEY)
      token.value = ''
      userInfo.value = null
    } catch (e) {
      console.error('清除认证信息失败:', e)
    }
  }

  const logout = () => {
    clearAuth()
    uni.reLaunch({
      url: '/pages/login/login'
    })
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isTeacher,
    isAdmin,
    isStaff,
    loadAuth,
    setAuth,
    clearAuth,
    logout
  }
})
