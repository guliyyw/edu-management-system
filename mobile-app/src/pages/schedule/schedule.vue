<template>
  <view class="schedule-container">
    <!-- 日期选择器 -->
    <view class="date-section">
      <view class="date-nav">
        <text class="nav-btn" @click="changeDate(-1)">‹</text>
        <view class="current-date">
          <text class="date-text">{{ currentDateText }}</text>
          <text class="week-text">{{ currentWeekText }}</text>
        </view>
        <text class="nav-btn" @click="changeDate(1)">›</text>
      </view>
      <view class="week-calendar">
        <view
          v-for="(day, index) in weekDays"
          :key="index"
          class="day-item"
          :class="{ active: isSameDay(day.date, currentDate) }"
          @click="selectDate(day.date)"
        >
          <text class="day-name">{{ day.name }}</text>
          <text class="day-number">{{ day.number }}</text>
        </view>
      </view>
    </view>

    <!-- 课程列表 -->
    <scroll-view class="schedule-content" scroll-y>
      <view v-if="lessons.length > 0" class="lesson-list">
        <view
          v-for="lesson in lessons"
          :key="lesson.id"
          class="lesson-card"
          :class="lesson.status"
        >
          <view class="time-slot">
            <text class="start-time">{{ formatTime(lesson.startTime) }}</text>
            <view class="time-line"></view>
            <text class="end-time">{{ formatTime(lesson.endTime) }}</text>
          </view>
          <view class="lesson-content">
            <view class="lesson-header">
              <text class="course-name">{{ lesson.courseName }}</text>
              <view class="status-badge" :class="lesson.status">
                <text class="badge-text">{{ statusText(lesson.status) }}</text>
              </view>
            </view>
            <view class="lesson-details">
              <view class="detail-item">
                <text class="detail-icon">👤</text>
                <text class="detail-text">{{ lesson.teacherName }}</text>
              </view>
              <view class="detail-item">
                <text class="detail-icon">📍</text>
                <text class="detail-text">{{ lesson.classroom }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon">📅</text>
        <text class="empty-title">暂无课程</text>
        <text class="empty-desc">这一天没有安排课程</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { getMySchedule } from '../../api/lesson'
import type { Lesson } from '../../api/lesson'

const currentDate = ref(new Date())
const lessons = ref<Lesson[]>([])

const currentDateText = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth() + 1
  return `${year}年${month}月`
})

const currentWeekText = computed(() => {
  const weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weeks[currentDate.value.getDay()]
})

const weekDays = computed(() => {
  const days = []
  const today = new Date(currentDate.value)
  const currentDay = today.getDay()
  const diff = today.getDate() - currentDay
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(today)
    date.setDate(diff + i)
    days.push({
      name: ['日', '一', '二', '三', '四', '五', '六'][i],
      number: date.getDate(),
      date: date
    })
  }
  return days
})

onMounted(() => {
  loadSchedule()
})

watch(currentDate, () => {
  loadSchedule()
})

const loadSchedule = async () => {
  try {
    const dateStr = formatDateString(currentDate.value)
    lessons.value = await getMySchedule(dateStr)
  } catch (error) {
    console.error('加载课程表失败:', error)
  }
}

const changeDate = (delta: number) => {
  const newDate = new Date(currentDate.value)
  newDate.setDate(newDate.getDate() + delta)
  currentDate.value = newDate
}

const selectDate = (date: Date) => {
  currentDate.value = new Date(date)
}

const isSameDay = (d1: Date, d2: Date) => {
  return d1.getFullYear() === d2.getFullYear() &&
         d1.getMonth() === d2.getMonth() &&
         d1.getDate() === d2.getDate()
}

const formatDateString = (date: Date) => {
  return date.toISOString().split('T')[0]
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
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
</script>

<style scoped>
.schedule-container {
  min-height: 100vh;
  background: #f5f6fa;
}

.date-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30rpx;
}

.date-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.nav-btn {
  font-size: 48rpx;
  color: #fff;
  padding: 10rpx 30rpx;
}

.current-date {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.date-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
}

.week-text {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 6rpx;
}

.week-calendar {
  display: flex;
  justify-content: space-around;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20rpx;
  padding: 20rpx 10rpx;
}

.day-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 20rpx;
  border-radius: 16rpx;
}

.day-item.active {
  background: #fff;
}

.day-name {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8rpx;
}

.day-item.active .day-name {
  color: #667eea;
}

.day-number {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
}

.day-item.active .day-number {
  color: #667eea;
}

.schedule-content {
  height: calc(100vh - 280rpx);
  padding: 20rpx;
}

.lesson-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.lesson-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  display: flex;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.time-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 30rpx;
  padding-right: 30rpx;
  border-right: 2rpx solid #f0f0f0;
}

.start-time, .end-time {
  font-size: 28rpx;
  font-weight: bold;
  color: #667eea;
}

.time-line {
  width: 2rpx;
  height: 40rpx;
  background: #e0e0e0;
  margin: 10rpx 0;
}

.lesson-content {
  flex: 1;
}

.lesson-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.course-name {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.status-badge {
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  background: #f0f0f0;
}

.status-badge.SCHEDULED {
  background: #e3f2fd;
}

.status-badge.IN_PROGRESS {
  background: #e8f5e9;
}

.status-badge.COMPLETED {
  background: #f5f5f5;
}

.badge-text {
  font-size: 22rpx;
  color: #666;
}

.lesson-details {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.detail-item {
  display: flex;
  align-items: center;
}

.detail-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
}

.detail-text {
  font-size: 28rpx;
  color: #666;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.empty-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.empty-desc {
  font-size: 28rpx;
  color: #999;
}
</style>
