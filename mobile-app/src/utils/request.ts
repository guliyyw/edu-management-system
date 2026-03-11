import { useConfigStore } from '../stores/config'
import { useAuthStore } from '../stores/auth'

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  data?: any
  headers?: Record<string, string>
  hideLoading?: boolean
}

interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export const request = <T = any>(options: RequestOptions): Promise<T> => {
  return new Promise((resolve, reject) => {
    const configStore = useConfigStore()
    const authStore = useAuthStore()
    
    // 检查是否配置了服务器地址
    if (!configStore.isConfigured) {
      uni.showToast({
        title: '请先配置服务器地址',
        icon: 'none'
      })
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/config/config' })
      }, 1500)
      reject(new Error('未配置服务器地址'))
      return
    }

    const baseUrl = configStore.getApiBaseUrl
    const fullUrl = options.url.startsWith('http') ? options.url : `${baseUrl}${options.url}`

    if (!options.hideLoading) {
      uni.showLoading({ title: '加载中...', mask: true })
    }

    uni.request({
      url: fullUrl,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...(authStore.token ? { 'Authorization': `Bearer ${authStore.token}` } : {}),
        ...options.headers
      },
      success: (res) => {
        const data = res.data as ApiResponse<T>
        
        if (res.statusCode === 200) {
          // 业务逻辑成功
          if (data.code === 200 || data.code === 0) {
            resolve(data.data)
          } else {
            uni.showToast({
              title: data.message || '请求失败',
              icon: 'none'
            })
            reject(new Error(data.message))
          }
        } else if (res.statusCode === 401) {
          // Token 过期
          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none'
          })
          authStore.logout()
          reject(new Error('未授权'))
        } else {
          uni.showToast({
            title: data.message || `请求失败 (${res.statusCode})`,
            icon: 'none'
          })
          reject(new Error(data.message || `HTTP ${res.statusCode}`))
        }
      },
      fail: (err) => {
        console.error('请求失败:', err)
        uni.showToast({
          title: '网络请求失败，请检查网络',
          icon: 'none'
        })
        reject(err)
      },
      complete: () => {
        if (!options.hideLoading) {
          uni.hideLoading()
        }
      }
    })
  })
}

// 便捷方法
export const get = <T = any>(url: string, params?: any) => {
  return request<T>({ url, method: 'GET', data: params })
}

export const post = <T = any>(url: string, data?: any) => {
  return request<T>({ url, method: 'POST', data })
}

export const put = <T = any>(url: string, data?: any) => {
  return request<T>({ url, method: 'PUT', data })
}

export const del = <T = any>(url: string) => {
  return request<T>({ url, method: 'DELETE' })
}
