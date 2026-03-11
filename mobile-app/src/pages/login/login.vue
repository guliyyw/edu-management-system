<template>
  <view class="login-container">
    <view class="header-section">
      <view class="back-btn" @click="goToConfig">
        <text class="back-icon">⚙️</text>
        <text class="back-text">配置</text>
      </view>
    </view>

    <view class="logo-section">
      <view class="app-icon">
        <text class="icon-text">E</text>
      </view>
      <text class="welcome-text">欢迎回来</text>
      <text class="sub-text">请登录您的账号</text>
    </view>

    <view class="login-card">
      <view class="input-group">
        <text class="input-label">用户名</text>
        <input
          v-model="form.username"
          class="input-field"
          type="text"
          placeholder="请输入用户名"
          confirm-type="next"
        />
      </view>

      <view class="input-group">
        <text class="input-label">密码</text>
        <input
          v-model="form.password"
          class="input-field"
          type="password"
          placeholder="请输入密码"
          confirm-type="done"
          @confirm="handleLogin"
        />
      </view>

      <button
        class="login-btn"
        :class="{ 'btn-disabled': !isValid }"
        :disabled="!isValid || loading"
        @click="handleLogin"
      >
        <text v-if="!loading" class="btn-text">登录</text>
        <text v-else class="btn-text">登录中...</text>
      </button>

      <view class="server-info">
        <text class="server-label">当前服务器:</text>
        <text class="server-url">{{ configStore.serverUrl }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useConfigStore } from '../../stores/config'
import { useAuthStore } from '../../stores/auth'
import { login } from '../../api/auth'

const configStore = useConfigStore()
const authStore = useAuthStore()

const form = ref({
  username: '',
  password: ''
})
const loading = ref(false)

const isValid = computed(() => {
  return form.value.username.trim() && form.value.password.trim()
})

const goToConfig = () => {
  uni.redirectTo({ url: '/pages/config/config' })
}

const handleLogin = async () => {
  if (!isValid.value) return

  loading.value = true
  try {
    const res = await login({
      username: form.value.username.trim(),
      password: form.value.password.trim()
    })
    
    authStore.setAuth(res.token, res.user)
    
    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })
    
    setTimeout(() => {
      uni.switchTab({ url: '/pages/home/home' })
    }, 1000)
  } catch (error: any) {
    uni.showToast({
      title: error.message || '登录失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx;
}

.header-section {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20rpx;
}

.back-btn {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  padding: 16rpx 30rpx;
  border-radius: 40rpx;
}

.back-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.back-text {
  font-size: 28rpx;
  color: #fff;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;
}

.app-icon {
  width: 140rpx;
  height: 140rpx;
  background: #fff;
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.2);
}

.icon-text {
  font-size: 70rpx;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.welcome-text {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 16rpx;
}

.sub-text {
  font-size: 30rpx;
  color: rgba(255, 255, 255, 0.8);
}

.login-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 50rpx 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.input-group {
  margin-bottom: 40rpx;
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
  height: 100rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 16rpx;
  padding: 0 30rpx;
  font-size: 32rpx;
  box-sizing: border-box;
}

.input-field:focus {
  border-color: #667eea;
}

.login-btn {
  width: 100%;
  height: 100rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 50rpx;
  border: none;
}

.login-btn::after {
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

.server-info {
  margin-top: 40rpx;
  padding-top: 30rpx;
  border-top: 2rpx solid #f0f0f0;
  text-align: center;
}

.server-label {
  font-size: 26rpx;
  color: #999;
  margin-right: 10rpx;
}

.server-url {
  font-size: 26rpx;
  color: #667eea;
}
</style>
