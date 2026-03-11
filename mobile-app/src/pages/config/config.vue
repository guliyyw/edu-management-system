<template>
  <view class="config-container">
    <view class="logo-section">
      <view class="app-icon">
        <text class="icon-text">E</text>
      </view>
      <text class="app-name">教育管理系统</text>
      <text class="app-version">v1.0.0</text>
    </view>

    <view class="config-card">
      <view class="card-title">
        <text class="title-icon">⚙️</text>
        <text class="title-text">服务器配置</text>
      </view>
      
      <view class="input-group">
        <text class="input-label">后端服务器地址</text>
        <input
          v-model="serverUrl"
          class="input-field"
          type="text"
          placeholder="例如: 192.168.1.100:8080"
          confirm-type="done"
        />
        <text class="input-hint">请输入后端服务器的IP地址和端口</text>
      </view>

      <view class="example-section">
        <text class="example-title">示例:</text>
        <view class="example-list">
          <text class="example-item" @click="setExample('192.168.1.100:8080')">
            192.168.1.100:8080
          </text>
          <text class="example-item" @click="setExample('10.0.0.5:8080')">
            10.0.0.5:8080
          </text>
          <text class="example-item" @click="setExample('api.example.com')">
            api.example.com
          </text>
        </view>
      </view>

      <button
        class="save-btn"
        :class="{ 'btn-disabled': !serverUrl }"
        :disabled="!serverUrl"
        @click="saveConfig"
      >
        <text class="btn-text">保存并继续</text>
      </button>

      <button
        v-if="configStore.isConfigured"
        class="skip-btn"
        @click="skipConfig"
      >
        <text class="btn-text">使用已保存的配置</text>
      </button>
    </view>

    <view class="help-section">
      <text class="help-title">💡 如何获取服务器地址?</text>
      <text class="help-text">
        1. 如果您在本地测试，使用电脑的局域网IP地址
        2. 如果是服务器部署，使用服务器的公网IP或域名
        3. 确保手机和服务器在同一网络（本地测试时）
      </text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useConfigStore } from '../../stores/config'
import { useAuthStore } from '../../stores/auth'

const configStore = useConfigStore()
const authStore = useAuthStore()
const serverUrl = ref('')

onMounted(() => {
  // 加载已保存的配置
  configStore.loadServerUrl()
  if (configStore.serverUrl) {
    serverUrl.value = configStore.serverUrl.replace(/^https?:\/\//, '')
  }
})

const setExample = (url: string) => {
  serverUrl.value = url
}

const saveConfig = () => {
  if (!serverUrl.value.trim()) {
    uni.showToast({
      title: '请输入服务器地址',
      icon: 'none'
    })
    return
  }

  const success = configStore.saveServerUrl(serverUrl.value.trim())
  if (success) {
    uni.showToast({
      title: '保存成功',
      icon: 'success'
    })
    
    // 检查是否已登录
    setTimeout(() => {
      authStore.loadAuth()
      if (authStore.isLoggedIn) {
        uni.switchTab({ url: '/pages/home/home' })
      } else {
        uni.redirectTo({ url: '/pages/login/login' })
      }
    }, 1000)
  }
}

const skipConfig = () => {
  authStore.loadAuth()
  if (authStore.isLoggedIn) {
    uni.switchTab({ url: '/pages/home/home' })
  } else {
    uni.redirectTo({ url: '/pages/login/login' })
  }
}
</script>

<style scoped>
.config-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60rpx 40rpx;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;
}

.app-icon {
  width: 160rpx;
  height: 160rpx;
  background: #fff;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.2);
}

.icon-text {
  font-size: 80rpx;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.app-name {
  font-size: 44rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 10rpx;
}

.app-version {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.config-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 50rpx 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.card-title {
  display: flex;
  align-items: center;
  margin-bottom: 40rpx;
}

.title-icon {
  font-size: 40rpx;
  margin-right: 16rpx;
}

.title-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.input-group {
  margin-bottom: 30rpx;
}

.input-label {
  display: block;
  font-size: 30rpx;
  color: #333;
  margin-bottom: 16rpx;
  font-weight: 500;
}

.input-field {
  width: 100%;
  height: 90rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 16rpx;
  padding: 0 30rpx;
  font-size: 32rpx;
  box-sizing: border-box;
  transition: border-color 0.3s;
}

.input-field:focus {
  border-color: #667eea;
  outline: none;
}

.input-hint {
  display: block;
  font-size: 26rpx;
  color: #999;
  margin-top: 12rpx;
}

.example-section {
  background: #f8f9fa;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 40rpx;
}

.example-title {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 16rpx;
  display: block;
}

.example-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.example-item {
  background: #fff;
  padding: 12rpx 24rpx;
  border-radius: 30rpx;
  font-size: 26rpx;
  color: #667eea;
  border: 2rpx solid #e0e0e0;
}

.example-item:active {
  background: #667eea;
  color: #fff;
}

.save-btn {
  width: 100%;
  height: 100rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
  border: none;
}

.save-btn::after {
  border: none;
}

.btn-disabled {
  opacity: 0.6;
}

.btn-text {
  color: #fff;
  font-size: 34rpx;
  font-weight: 500;
}

.skip-btn {
  width: 100%;
  height: 90rpx;
  background: transparent;
  border: 2rpx solid #667eea;
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.skip-btn::after {
  border: none;
}

.skip-btn .btn-text {
  color: #667eea;
}

.help-section {
  margin-top: 50rpx;
  padding: 30rpx;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 16rpx;
}

.help-title {
  display: block;
  font-size: 30rpx;
  color: #fff;
  font-weight: 500;
  margin-bottom: 16rpx;
}

.help-text {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.8;
}
</style>
