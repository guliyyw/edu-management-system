import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

interface LoginResponse {
  token: string
  username: string
  realName: string
  role: 'ADMIN' | 'STAFF' | 'TEACHER'
  teacherId?: number
  campusId?: number
}

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<LoginResponse | null>(null)

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const userRole = computed(() => user.value?.role || null)
  const userName = computed(() => user.value?.realName || '')
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isStaff = computed(() => user.value?.role === 'STAFF')
  const isTeacher = computed(() => user.value?.role === 'TEACHER')

  // Actions
  const login = async (username: string, password: string) => {
    try {
      const response = await axios.post('/api/auth/login', { username, password })
      const data = response.data

      if (data.code === 200) {
        token.value = data.data.token
        user.value = data.data
        localStorage.setItem('token', data.data.token)
        localStorage.setItem('user', JSON.stringify(data.data))

        // 设置axios默认header
        axios.defaults.headers.common['Authorization'] = `Bearer ${data.data.token}`

        return { success: true }
      }
      return { success: false, message: data.message }
    } catch (error: any) {
      return { success: false, message: error.response?.data?.message || '登录失败' }
    }
  }

  const logout = async () => {
    try {
      await axios.post('/api/auth/logout')
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      token.value = null
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      delete axios.defaults.headers.common['Authorization']
      // 使用 window.location 跳转，避免在 store 中依赖 router
      window.location.href = '/login'
    }
  }

  const initAuth = () => {
    const savedToken = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')

    if (savedToken && savedUser) {
      token.value = savedToken
      user.value = JSON.parse(savedUser)
      axios.defaults.headers.common['Authorization'] = `Bearer ${savedToken}`
    }
  }

  return {
    token,
    user,
    isLoggedIn,
    userRole,
    userName,
    isAdmin,
    isStaff,
    isTeacher,
    login,
    logout,
    initAuth
  }
})
