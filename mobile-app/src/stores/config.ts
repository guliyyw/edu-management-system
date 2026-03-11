import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY = 'server_url'
const DEFAULT_URL = 'http://192.168.1.100:8080'

export const useConfigStore = defineStore('config', () => {
  // State
  const serverUrl = ref<string>('')
  const isConfigured = computed(() => !!serverUrl.value)

  // Actions
  const loadServerUrl = () => {
    try {
      const saved = uni.getStorageSync(STORAGE_KEY)
      serverUrl.value = saved || ''
    } catch (e) {
      console.error('加载服务器地址失败:', e)
      serverUrl.value = ''
    }
  }

  const saveServerUrl = (url: string) => {
    try {
      // 验证 URL 格式
      if (!url) {
        throw new Error('服务器地址不能为空')
      }
      
      // 自动添加 http:// 前缀
      if (!url.startsWith('http://') && !url.startsWith('https://')) {
        url = 'http://' + url
      }
      
      // 移除末尾的斜杠
      url = url.replace(/\/$/, '')
      
      uni.setStorageSync(STORAGE_KEY, url)
      serverUrl.value = url
      return true
    } catch (e: any) {
      uni.showToast({
        title: e.message || '保存失败',
        icon: 'none'
      })
      return false
    }
  }

  const clearServerUrl = () => {
    try {
      uni.removeStorageSync(STORAGE_KEY)
      serverUrl.value = ''
    } catch (e) {
      console.error('清除服务器地址失败:', e)
    }
  }

  const getApiBaseUrl = computed(() => {
    return serverUrl.value ? `${serverUrl.value}/api` : ''
  })

  return {
    serverUrl,
    isConfigured,
    loadServerUrl,
    saveServerUrl,
    clearServerUrl,
    getApiBaseUrl
  }
})
