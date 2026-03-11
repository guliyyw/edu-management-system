<template>
  <view class="home-container">
    <!-- 顶部用户信息 -->
    <view class="header-section">
      <view class="user-info">
        <view class="avatar">
          <text class="avatar-text">{{ userInitial }}</text>
        </view>
        <view class="user-details">
          <text class="user-name">{{ authStore.userInfo?.realName || '用户' }}</text>
          <text class="user-role">{{ roleText }}</text>
        </view>
      </view>
      <view class="settings-btn" @click="showSettings">
        <text class="settings-icon">⚙️</text>
      </view>
    </view>

    <!-- 今日统计 -->
    <view class="stats-section">
      <view class="stats-card">
        <view class="stat-item">
          <text class="stat-number">{{ todayLessons.length }}</text>
          <text class="stat-label">今日课程</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-number">{{ attendanceRate }}%</text>
          <text class="stat-label">出勤率</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-number">{{ announcements.length }}</text>
          <text class="stat-label">新公告</text>
        </view>
      </view>
    </view>

    <!-- 教师功能入口 -->
    <view v-if="authStore.isTeacher" class="teacher-section">
      <text class="section-title">👨‍🏫 教师功能</text>
      <view class="function-grid">
        <view class="function-item" @click="goToCheckin">
          <view class="function-icon checkin">
            <text class="icon-text">✅</text>
          </view>
          <text class="function-name">签到管理</text>
        </view>
        <view class="function-item" @click="goToTravel">
          <view class="function-icon travel">
            <text class="icon-text">🚗</text>
          </view>
          <text class="function-name">路程配置</text>
        </view>
        <view class="function-item" @click="goToStats">
          <view class="function-icon stats">
            <text class="icon-text">📊</text>
          </view>
          <text class="function-name">个人统计</text>
        </view>
        <view class="function-item" @click="goToSchedule">
          <view class="function-icon schedule">
            <text class="icon-text">📅</text>
          </view>
          <text class="function-name">我的课表</text>
        </view>
      </view>
    </view>

    <!-- 今日课程 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">📚 今日课程</text>
        <text class="section-more" @click="goToSchedule">查看更多</text>
      </view>
      
      <view v-if="todayLessons.length > 0" class="lesson-list">
        <view
          v-for="lesson in todayLessons.slice(0, 3)"
          :key="lesson.id"
          class="lesson-item"
        >
          <view class="lesson-time">
            <text class="time-text">{{ formatTime(lesson.startTime) }}</text>
          </view>
          <view class="lesson-info">
            <text class="lesson-name">{{ lesson.courseName }}</text>
            <text class="lesson-detail">{{ lesson.classroom }} | {{ lesson.teacherName }}</text>
          </view>
          <view class="lesson-status" :class="lesson.status">
            <text class="status-text">{{ statusText(lesson.status) }}</text>
          </view>
        </view>
      </view>
      
      <view v-else class="empty-state">
        <text class="empty-icon">📖</text>
        <text class="empty-text">今日暂无课程</text>
      </view>
    </view>

    <!-- 最新公告 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">📢 最新公告</text>
        <text class="section-more" @click="goToAnnouncements">查看更多</text>
      </view>
      
      <view v-if="announcements.length > 0" class="announcement-list">
        <view
          v-for="item in announcements.slice(0, 3)"
          :key="item.id"
          class="announcement-item"
          :class="{ important: item.important }"
        >
          <view class="announcement-header">
            <text v-if="item.important" class="important-tag">重要</text>
            <text class="announcement-title">{{ item.title }}</text>
          </view>
          <text class="announcement-content">{{ item.content }}</text>
          <text class="announcement-time">{{ formatDate(item.publishTime) }}</text>
        </view>
      </view>
      
      <view v-else class="empty-state">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无公告</text>
      </view>
    </view>

    <!-- 设置弹窗 -->
    <uni-popup ref="settingsPopup" type="bottom">
      <view class="settings-popup">
        <view class="popup-header">
          <text class="popup-title">设置</text>
          <text class="popup-close" @click="closeSettings">✕</text>
        </view>
        <view class="settings-list">
          <view class="settings-item" @click="goToConfig">
            <text class="item-icon">🔗</text>
            <text class="item-text">修改服务器地址</text>
            <text class="item-arrow">›</text>
          </view>
          <view class="settings-item" @click="logout">
            <text class="item-icon">🚪</text>
            <text class="item-text">退出登录</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { getTodayLessons } from '../../api/lesson'
import { getAnnouncements } from '../../api/announcement'
import type { Lesson } from '../../api/lesson'
import type { Announcement } from '../../api/announcement'

const authStore = useAuthStore()
const todayLessons = ref<Lesson[]>([])
const announcements = ref<Announcement[]>([])
const attendanceRate = ref(100)
const settingsPopup = ref()

const userInitial = computed(() => {
  const name = authStore.userInfo?.realName || authStore.userInfo?.username || 'U'
  return name.charAt(0).toUpperCase()
})

const roleText = computed(() => {
  const roleMap: Record<string, string> = {
    'ADMIN': '管理员',
    'TEACHER': '教师',
    'STAFF': '教务人员',
    'STUDENT': '学生'
  }
  return roleMap[authStore.userInfo?.role || ''] || '用户'
})

onMounted(() => {
  loadData()
})

const loadData = async () => {
  try {
    const [lessons, annos] = await Promise.all([
      getTodayLessons(),
      getAnnouncements()
    ])
    todayLessons.value = lessons
    announcements.value = annos
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const statusText = (status: string) => {
  const map: Record<string, string> = {
    'SCHEDULED': '未开始',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已结束',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const goToSchedule = () => {
  uni.switchTab({ url: '/pages/schedule/schedule' })
}

const goToAnnouncements = () => {
  uni.switchTab({ url: '/pages/announcements/announcements' })
}

const goToCheckin = () => {
  uni.navigateTo({ url: '/pages/checkin/checkin' })
}

const goToTravel = () => {
  uni.navigateTo({ url: '/pages/travel/travel' })
}

const goToStats = () => {
  uni.navigateTo({ url: '/pages/stats/stats' })
}

const showSettings = () => {
  settingsPopup.value?.open()
}

const closeSettings = () => {
  settingsPopup.value?.close()
}

const goToConfig = () => {
  closeSettings()
  uni.redirectTo({ url: '/pages/config/config' })
}

const logout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        authStore.logout()
      }
    }
  })
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 40rpx;
}

.header-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 80rpx 40rpx 100rpx;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.avatar-text {
  font-size: 44rpx;
  font-weight: bold;
  color: #fff;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 8rpx;
}

.user-role {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.settings-btn {
  width: 70rpx;
  height: 70rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.settings-icon {
  font-size: 36rpx;
}

.stats-section {
  margin: -50rpx 30rpx 30rpx;
}

.stats-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  display: flex;
  justify-content: space-around;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-number {
  font-size: 48rpx;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 10rpx;
}

.stat-label {
  font-size: 26rpx;
  color: #999;
}

.stat-divider {
  width: 2rpx;
  height: 80rpx;
  background: #f0f0f0;
}

.teacher-section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.teacher-section .section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
  display: block;
}

.function-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.function-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 0;
}

.function-icon {
  width: 100rpx;
  height: 100rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}

.function-icon.checkin {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.function-icon.travel {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.function-icon.stats {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.function-icon.schedule {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.icon-text {
  font-size: 48rpx;
}

.function-name {
  font-size: 26rpx;
  color: #333;
}

.section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.section-more {
  font-size: 26rpx;
  color: #667eea;
}

.lesson-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.lesson-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
}

.lesson-time {
  width: 100rpx;
  margin-right: 24rpx;
}

.time-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #667eea;
}

.lesson-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.lesson-name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 8rpx;
}

.lesson-detail {
  font-size: 24rpx;
  color: #999;
}

.lesson-status {
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  background: #e8e8e8;
}

.lesson-status.SCHEDULED {
  background: #e3f2fd;
}

.lesson-status.IN_PROGRESS {
  background: #e8f5e9;
}

.lesson-status.COMPLETED {
  background: #f5f5f5;
}

.status-text {
  font-size: 22rpx;
  color: #666;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.announcement-item {
  padding: 24rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  border-left: 6rpx solid transparent;
}

.announcement-item.important {
  background: #fff3f3;
  border-left-color: #ff6b6b;
}

.announcement-header {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.important-tag {
  background: #ff6b6b;
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  margin-right: 12rpx;
}

.announcement-title {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
}

.announcement-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.announcement-time {
  font-size: 22rpx;
  color: #999;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.settings-popup {
  background: #fff;
  border-radius: 30rpx 30rpx 0 0;
  padding: 30rpx;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
  padding-bottom: 20rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.popup-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.popup-close {
  font-size: 40rpx;
  color: #999;
  padding: 10rpx;
}

.settings-list {
  display: flex;
  flex-direction: column;
}

.settings-item {
  display: flex;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 2rpx solid #f5f5f5;
}

.item-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}

.item-text {
  flex: 1;
  font-size: 30rpx;
  color: #333;
}

.item-arrow {
  font-size: 36rpx;
  color: #ccc;
}
</style>
