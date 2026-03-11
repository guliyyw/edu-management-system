<template>
  <view class="stats-container">
    <!-- 日期范围选择 -->
    <view class="date-range-section">
      <view class="date-picker-group">
        <picker mode="date" :value="startDate" @change="onStartDateChange">
          <view class="date-picker">
            <text class="picker-label">开始</text>
            <text class="picker-value">{{ startDate }}</text>
          </view>
        </picker>
        <text class="date-separator">至</text>
        <picker mode="date" :value="endDate" @change="onEndDateChange">
          <view class="date-picker">
            <text class="picker-label">结束</text>
            <text class="picker-value">{{ endDate }}</text>
          </view>
        </picker>
      </view>
      <button class="query-btn" @click="fetchStats">查询</button>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-cards">
      <view class="stat-card blue">
        <text class="stat-value">{{ stats.totalLessons }}</text>
        <text class="stat-label">课时数量</text>
      </view>
      <view class="stat-card green">
        <text class="stat-value">{{ stats.totalStudents }}</text>
        <text class="stat-label">学生人次</text>
      </view>
      <view class="stat-card orange">
        <text class="stat-value">{{ stats.totalHours.toFixed(1) }}</text>
        <text class="stat-label">总课时(小时)</text>
      </view>
      <view class="stat-card purple">
        <text class="stat-value">¥{{ stats.totalAmount.toFixed(2) }}</text>
        <text class="stat-label">授课金额</text>
      </view>
    </view>

    <!-- 课程明细列表 -->
    <view class="lesson-detail-section">
      <view class="section-header">
        <text class="section-title">📊 课程明细</text>
        <text class="section-count">共 {{ lessonStats.length }} 条</text>
      </view>

      <scroll-view class="lesson-list" scroll-y>
        <view v-if="lessonStats.length > 0" class="lesson-items">
          <view
            v-for="(item, index) in lessonStats"
            :key="index"
            class="lesson-item"
          >
            <view class="lesson-header">
              <text class="lesson-date">{{ item.date }}</text>
              <text class="lesson-course">{{ item.courseName }}</text>
            </view>
            <view class="lesson-info">
              <text class="info-item">📍 {{ item.campusName }}</text>
              <text class="info-item">👥 {{ item.studentCount }}人</text>
              <text class="info-item">⏱ {{ item.duration }}小时</text>
            </view>
            <view class="attendance-stats">
              <view class="attendance-item present">
                <text class="attendance-num">{{ item.presentCount }}</text>
                <text class="attendance-label">到课</text>
              </view>
              <view class="attendance-item leave">
                <text class="attendance-num">{{ item.leaveCount }}</text>
                <text class="attendance-label">请假</text>
              </view>
              <view class="attendance-item absent">
                <text class="attendance-num">{{ item.absentCount }}</text>
                <text class="attendance-label">缺课</text>
              </view>
            </view>
            <view class="lesson-footer">
              <text class="teacher-fee">课时费: ¥{{ item.teacherFee }}</text>
              <text class="lesson-amount">金额: ¥{{ item.lessonAmount.toFixed(2) }}</text>
            </view>
          </view>
        </view>

        <view v-else class="empty-state">
          <text class="empty-icon">📈</text>
          <text class="empty-text">暂无统计数据</text>
          <text class="empty-desc">请选择日期范围查询</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { getTeacherLessonsByRange, type Lesson } from '../../api/lesson'

const authStore = useAuthStore()

// 默认本月
const now = new Date()
const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
const endOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0)

const startDate = ref(formatDate(startOfMonth))
const endDate = ref(formatDate(endOfMonth))

const stats = ref({
  totalLessons: 0,
  totalStudents: 0,
  totalHours: 0,
  totalAmount: 0
})

const lessonStats = ref<any[]>([])

onMounted(() => {
  fetchStats()
})

function formatDate(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function calculateDuration(startTime: string, endTime: string): number {
  if (!startTime || !endTime) return 0

  const extractTime = (timeStr: string): string => {
    if (!timeStr) return ''
    if (timeStr.includes('T')) {
      return timeStr.split('T')[1].substring(0, 5)
    }
    return timeStr.substring(0, 5)
  }

  const startStr = extractTime(startTime)
  const endStr = extractTime(endTime)

  if (!startStr || !endStr) return 0

  const [startHour, startMin] = startStr.split(':').map(Number)
  const [endHour, endMin] = endStr.split(':').map(Number)

  const startMinutes = startHour * 60 + startMin
  const endMinutes = endHour * 60 + endMin

  const diffMinutes = endMinutes - startMinutes
  return diffMinutes / 60
}

async function fetchStats() {
  if (!authStore.userInfo?.teacherId) {
    uni.showToast({ title: '非教师账号', icon: 'none' })
    return
  }

  try {
    const lessons = await getTeacherLessonsByRange(
      authStore.userInfo.teacherId,
      startDate.value,
      endDate.value
    )

    let totalStudents = 0
    let totalHours = 0
    let totalAmount = 0

    stats.value.totalLessons = lessons.length

    lessonStats.value = lessons.map((lesson: Lesson) => {
      const studentCount = lesson.attendances?.length || 0
      const presentCount = lesson.attendances?.filter((a: any) => a.status === 'PRESENT').length || 0
      const leaveCount = lesson.attendances?.filter((a: any) => a.status === 'LEAVE').length || 0
      const absentCount = lesson.attendances?.filter((a: any) => a.status === 'ABSENT').length || 0

      const duration = calculateDuration(lesson.startTime, lesson.endTime)
      const teacherFee = lesson.classInfo?.teacherFee || 0
      const lessonAmount = presentCount > 0 ? teacherFee * presentCount : 0

      totalStudents += studentCount
      totalHours += duration
      totalAmount += lessonAmount

      return {
        date: lesson.date,
        courseName: lesson.classInfo?.course?.name || '未知课程',
        campusName: lesson.classInfo?.campus?.name || '未知校区',
        studentCount,
        presentCount,
        leaveCount,
        absentCount,
        duration: duration.toFixed(1),
        teacherFee,
        lessonAmount
      }
    })

    stats.value.totalStudents = totalStudents
    stats.value.totalHours = parseFloat(totalHours.toFixed(1))
    stats.value.totalAmount = totalAmount
  } catch (error) {
    console.error('获取统计失败:', error)
    uni.showToast({ title: '获取统计失败', icon: 'none' })
  }
}

function onStartDateChange(e: any) {
  startDate.value = e.detail.value
}

function onEndDateChange(e: any) {
  endDate.value = e.detail.value
}
</script>

<style scoped>
.stats-container {
  min-height: 100vh;
  background: #f5f6fa;
}

.date-range-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30rpx;
}

.date-picker-group {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.date-picker {
  flex: 1;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
}

.picker-label {
  display: block;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8rpx;
}

.picker-value {
  font-size: 30rpx;
  color: #fff;
  font-weight: 500;
}

.date-separator {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

.query-btn {
  width: 100%;
  height: 90rpx;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 45rpx;
  font-size: 32rpx;
  color: #fff;
  font-weight: 500;
  border: none;
}

.query-btn::after {
  border: none;
}

.stats-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
  padding: 30rpx;
  margin-top: -20rpx;
}

.stat-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx 30rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
  border-top: 6rpx solid transparent;
}

.stat-card.blue {
  border-top-color: #667eea;
}

.stat-card.green {
  border-top-color: #11998e;
}

.stat-card.orange {
  border-top-color: #f093fb;
}

.stat-card.purple {
  border-top-color: #764ba2;
}

.stat-value {
  font-size: 44rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 12rpx;
}

.stat-label {
  font-size: 26rpx;
  color: #999;
}

.lesson-detail-section {
  padding: 0 30rpx 30rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.section-count {
  font-size: 26rpx;
  color: #999;
}

.lesson-list {
  height: calc(100vh - 600rpx);
}

.lesson-items {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.lesson-item {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.lesson-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.lesson-date {
  font-size: 28rpx;
  color: #667eea;
  font-weight: 500;
}

.lesson-course {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.lesson-info {
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
  flex-wrap: wrap;
}

.info-item {
  font-size: 26rpx;
  color: #666;
  background: #f5f6fa;
  padding: 10rpx 20rpx;
  border-radius: 30rpx;
}

.attendance-stats {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.attendance-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #f5f6fa;
}

.attendance-item.present {
  background: #e8f5e9;
}

.attendance-item.leave {
  background: #fff3e0;
}

.attendance-item.absent {
  background: #ffebee;
}

.attendance-num {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.attendance-item.present .attendance-num {
  color: #4caf50;
}

.attendance-item.leave .attendance-num {
  color: #ff9800;
}

.attendance-item.absent .attendance-num {
  color: #f44336;
}

.attendance-label {
  font-size: 24rpx;
  color: #666;
}

.lesson-footer {
  display: flex;
  justify-content: space-between;
  padding-top: 20rpx;
  border-top: 2rpx solid #f0f0f0;
}

.teacher-fee {
  font-size: 26rpx;
  color: #999;
}

.lesson-amount {
  font-size: 30rpx;
  font-weight: bold;
  color: #667eea;
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

.empty-text {
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
