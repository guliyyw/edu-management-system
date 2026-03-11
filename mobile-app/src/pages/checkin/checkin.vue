<template>
  <view class="checkin-container">
    <!-- 日期选择 -->
    <view class="date-section">
      <picker mode="date" :value="selectedDate" @change="onDateChange">
        <view class="date-picker">
          <text class="date-text">{{ selectedDate }}</text>
          <text class="date-icon">📅</text>
        </view>
      </picker>
      <text class="lesson-count">共 {{ lessons.length }} 节课</text>
    </view>

    <!-- 课程列表 -->
    <scroll-view class="lesson-list" scroll-y>
      <view v-if="lessons.length > 0" class="lessons">
        <view
          v-for="lesson in lessons"
          :key="lesson.id"
          class="lesson-card"
          @click="expandLesson(lesson)"
        >
          <view class="lesson-header">
            <view class="lesson-time">
              <text class="time">{{ formatTime(lesson.startTime) }} - {{ formatTime(lesson.endTime) }}</text>
            </view>
            <view class="lesson-info">
              <text class="course-name">{{ lesson.classInfo?.course?.name || '未知课程' }}</text>
              <text class="campus-name">{{ lesson.classInfo?.campus?.name }} - {{ lesson.classroom }}</text>
            </view>
            <view class="lesson-status" :class="lesson.status">
              <text class="status-text">{{ getStatusLabel(lesson.status) }}</text>
            </view>
          </view>

          <!-- 学生签到列表 -->
          <view v-if="expandedLessonId === lesson.id" class="student-list">
            <view class="batch-actions">
              <text class="batch-title">批量签到</text>
              <view class="batch-buttons">
                <button class="batch-btn present" @click.stop="batchSign(lesson, 'PRESENT')">全到课</button>
                <button class="batch-btn leave" @click.stop="batchSign(lesson, 'LEAVE')">全请假</button>
                <button class="batch-btn absent" @click.stop="batchSign(lesson, 'ABSENT')">全缺课</button>
              </view>
            </view>

            <view
              v-for="attendance in lesson.attendances"
              :key="attendance.id"
              class="student-item"
            >
              <view class="student-info">
                <text class="student-name">{{ attendance.student?.name }}</text>
                <text class="parent-phone">{{ attendance.student?.parentPhone || '无电话' }}</text>
              </view>
              <view class="status-selector">
                <view
                  v-for="status in attendanceStatuses"
                  :key="status.value"
                  class="status-option"
                  :class="{ active: attendance.status === status.value, [status.value.toLowerCase()]: true }"
                  @click.stop="updateAttendance(lesson.id, attendance, status.value)"
                >
                  <text class="status-label">{{ status.label }}</text>
                </view>
              </view>
            </view>

            <view v-if="lesson.attendances?.length === 0" class="empty-students">
              <text class="empty-text">暂无学生</text>
            </view>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon">📚</text>
        <text class="empty-text">该日期暂无课程</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { getTeacherLessonsByDate, markAttendance, type Lesson, type Attendance } from '../../api/lesson'

const authStore = useAuthStore()
const selectedDate = ref(formatDate(new Date()))
const lessons = ref<Lesson[]>([])
const expandedLessonId = ref<number | null>(null)
const loading = ref(false)

const attendanceStatuses = [
  { value: 'PENDING', label: '待签' },
  { value: 'PRESENT', label: '到课' },
  { value: 'LEAVE', label: '请假' },
  { value: 'ABSENT', label: '缺课' },
  { value: 'TRIAL', label: '试课' }
]

onMounted(() => {
  fetchLessons()
})

function formatDate(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatTime(timeStr: string): string {
  if (!timeStr) return ''
  // 处理 "HH:mm:ss" 格式
  return timeStr.substring(0, 5)
}

function getStatusLabel(status: string): string {
  const labels: Record<string, string> = {
    'SCHEDULED': '已排课',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return labels[status] || status
}

async function fetchLessons() {
  if (!authStore.userInfo?.teacherId) {
    uni.showToast({ title: '非教师账号', icon: 'none' })
    return
  }

  loading.value = true
  try {
    lessons.value = await getTeacherLessonsByDate(authStore.userInfo.teacherId, selectedDate.value)
  } catch (error) {
    console.error('获取课程失败:', error)
    uni.showToast({ title: '获取课程失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function onDateChange(e: any) {
  selectedDate.value = e.detail.value
  expandedLessonId.value = null
  fetchLessons()
}

function expandLesson(lesson: Lesson) {
  expandedLessonId.value = expandedLessonId.value === lesson.id ? null : lesson.id
}

async function updateAttendance(lessonId: number, attendance: Attendance, status: string) {
  if (attendance.status === status) return

  try {
    await markAttendance(lessonId, attendance.student.id, status)
    attendance.status = status
    uni.showToast({ title: '签到成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: '签到失败', icon: 'none' })
  }
}

function batchSign(lesson: Lesson, status: string) {
  uni.showModal({
    title: '批量签到',
    content: `确定将所有学生标记为"${getAttendanceLabel(status)}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          for (const attendance of lesson.attendances || []) {
            if (attendance.status !== status) {
              await markAttendance(lesson.id, attendance.student.id, status)
              attendance.status = status
            }
          }
          uni.showToast({ title: '批量签到成功', icon: 'success' })
        } catch (error) {
          uni.showToast({ title: '批量签到失败', icon: 'none' })
        }
      }
    }
  })
}

function getAttendanceLabel(status: string): string {
  const statusObj = attendanceStatuses.find(s => s.value === status)
  return statusObj?.label || status
}
</script>

<style scoped>
.checkin-container {
  min-height: 100vh;
  background: #f5f6fa;
}

.date-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.date-picker {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  padding: 20rpx 30rpx;
  border-radius: 40rpx;
}

.date-text {
  font-size: 32rpx;
  color: #fff;
  font-weight: 500;
  margin-right: 16rpx;
}

.date-icon {
  font-size: 32rpx;
}

.lesson-count {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

.lesson-list {
  height: calc(100vh - 140rpx);
  padding: 20rpx;
}

.lessons {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.lesson-card {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.lesson-header {
  padding: 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.lesson-time {
  background: #f0f0f0;
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
}

.time {
  font-size: 26rpx;
  font-weight: bold;
  color: #667eea;
}

.lesson-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.course-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.campus-name {
  font-size: 26rpx;
  color: #999;
}

.lesson-status {
  padding: 10rpx 20rpx;
  border-radius: 30rpx;
  background: #f0f0f0;
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
  font-size: 24rpx;
  color: #666;
}

.student-list {
  border-top: 2rpx solid #f0f0f0;
  padding: 20rpx;
}

.batch-actions {
  background: #f8f9fa;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.batch-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
  display: block;
}

.batch-buttons {
  display: flex;
  gap: 16rpx;
}

.batch-btn {
  flex: 1;
  padding: 16rpx 0;
  border-radius: 30rpx;
  font-size: 26rpx;
  border: none;
}

.batch-btn::after {
  border: none;
}

.batch-btn.present {
  background: #e8f5e9;
  color: #4caf50;
}

.batch-btn.leave {
  background: #fff3e0;
  color: #ff9800;
}

.batch-btn.absent {
  background: #ffebee;
  color: #f44336;
}

.student-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  border-bottom: 2rpx solid #f5f5f5;
}

.student-item:last-child {
  border-bottom: none;
}

.student-info {
  display: flex;
  flex-direction: column;
}

.student-name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 8rpx;
}

.parent-phone {
  font-size: 24rpx;
  color: #999;
}

.status-selector {
  display: flex;
  gap: 12rpx;
}

.status-option {
  padding: 12rpx 20rpx;
  border-radius: 24rpx;
  background: #f0f0f0;
  min-width: 80rpx;
  text-align: center;
}

.status-option.active {
  transform: scale(1.05);
}

.status-option.active.present {
  background: #4caf50;
}

.status-option.active.leave {
  background: #ff9800;
}

.status-option.active.absent {
  background: #f44336;
}

.status-option.active.trial {
  background: #2196f3;
}

.status-option.active.pending {
  background: #9e9e9e;
}

.status-option.active .status-label {
  color: #fff;
}

.status-label {
  font-size: 24rpx;
  color: #666;
}

.empty-students {
  padding: 60rpx;
  text-align: center;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 40rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #999;
}
</style>
