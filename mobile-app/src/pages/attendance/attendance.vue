<template>
  <view class="attendance-container">
    <!-- 统计卡片 -->
    <view class="stats-section">
      <view class="stats-row">
        <view class="stat-card total">
          <text class="stat-value">{{ stats.total }}</text>
          <text class="stat-label">总课时</text>
        </view>
        <view class="stat-card present">
          <text class="stat-value">{{ stats.present }}</text>
          <text class="stat-label">已出勤</text>
        </view>
      </view>
      <view class="stats-row">
        <view class="stat-card absent">
          <text class="stat-value">{{ stats.absent }}</text>
          <text class="stat-label">缺勤</text>
        </view>
        <view class="stat-card late">
          <text class="stat-value">{{ stats.late }}</text>
          <text class="stat-label">迟到</text>
        </view>
      </view>
    </view>

    <!-- 考勤记录列表 -->
    <view class="records-section">
      <view class="section-header">
        <text class="section-title">考勤记录</text>
        <view class="filter-bar">
          <picker mode="date" :value="startDate" @change="onStartDateChange">
            <view class="date-picker">
              <text class="picker-text">{{ startDate || '开始日期' }}</text>
              <text class="picker-icon">▼</text>
            </view>
          </picker>
          <text class="date-separator">至</text>
          <picker mode="date" :value="endDate" @change="onEndDateChange">
            <view class="date-picker">
              <text class="picker-text">{{ endDate || '结束日期' }}</text>
              <text class="picker-icon">▼</text>
            </view>
          </picker>
        </view>
      </view>

      <scroll-view class="records-list" scroll-y>
        <view v-if="records.length > 0" class="record-items">
          <view
            v-for="record in records"
            :key="record.id"
            class="record-card"
          >
            <view class="record-header">
              <text class="course-name">{{ record.courseName }}</text>
              <view class="status-tag" :class="record.status">
                <text class="tag-text">{{ statusText(record.status) }}</text>
              </view>
            </view>
            <view class="record-info">
              <text class="info-text">学生: {{ record.studentName }}</text>
              <text v-if="record.checkInTime" class="info-text">
                签到时间: {{ formatTime(record.checkInTime) }}
              </text>
            </view>
            <text v-if="record.remark" class="record-remark">
              备注: {{ record.remark }}
            </text>
          </view>
        </view>

        <view v-else class="empty-state">
          <text class="empty-icon">📋</text>
          <text class="empty-text">暂无考勤记录</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { getMyAttendance } from '../../api/attendance'
import type { AttendanceRecord } from '../../api/attendance'

const records = ref<AttendanceRecord[]>([])
const startDate = ref('')
const endDate = ref('')

const stats = computed(() => {
  return {
    total: records.value.length,
    present: records.value.filter(r => r.status === 'PRESENT').length,
    absent: records.value.filter(r => r.status === 'ABSENT').length,
    late: records.value.filter(r => r.status === 'LATE').length
  }
})

onMounted(() => {
  // 默认查询最近30天
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 30)
  
  endDate.value = formatDateString(end)
  startDate.value = formatDateString(start)
  
  loadRecords()
})

watch([startDate, endDate], () => {
  loadRecords()
})

const loadRecords = async () => {
  try {
    records.value = await getMyAttendance({
      startDate: startDate.value,
      endDate: endDate.value
    })
  } catch (error) {
    console.error('加载考勤记录失败:', error)
  }
}

const onStartDateChange = (e: any) => {
  startDate.value = e.detail.value
}

const onEndDateChange = (e: any) => {
  endDate.value = e.detail.value
}

const formatDateString = (date: Date) => {
  return date.toISOString().split('T')[0]
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const statusText = (status: string) => {
  const map: Record<string, string> = {
    'PRESENT': '已出勤',
    'ABSENT': '缺勤',
    'LATE': '迟到',
    'LEAVE': '请假'
  }
  return map[status] || status
}
</script>

<style scoped>
.attendance-container {
  min-height: 100vh;
  background: #f5f6fa;
  padding: 20rpx;
}

.stats-section {
  margin-bottom: 20rpx;
}

.stats-row {
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.stat-card {
  flex: 1;
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.stat-card.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.present {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-card.absent {
  background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
}

.stat-card.late {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-value {
  font-size: 56rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 10rpx;
}

.stat-label {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

.records-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.section-header {
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.date-picker {
  display: flex;
  align-items: center;
  background: #f5f6fa;
  padding: 16rpx 24rpx;
  border-radius: 12rpx;
}

.picker-text {
  font-size: 26rpx;
  color: #666;
  margin-right: 10rpx;
}

.picker-icon {
  font-size: 20rpx;
  color: #999;
}

.date-separator {
  font-size: 26rpx;
  color: #999;
}

.records-list {
  max-height: 600rpx;
}

.record-items {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.record-card {
  background: #f8f9fa;
  border-radius: 16rpx;
  padding: 24rpx;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.course-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.status-tag {
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  background: #e0e0e0;
}

.status-tag.PRESENT {
  background: #e8f5e9;
}

.status-tag.ABSENT {
  background: #ffebee;
}

.status-tag.LATE {
  background: #fff3e0;
}

.status-tag.LEAVE {
  background: #e3f2fd;
}

.tag-text {
  font-size: 24rpx;
  color: #666;
}

.status-tag.PRESENT .tag-text {
  color: #4caf50;
}

.status-tag.ABSENT .tag-text {
  color: #f44336;
}

.status-tag.LATE .tag-text {
  color: #ff9800;
}

.status-tag.LEAVE .tag-text {
  color: #2196f3;
}

.record-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.info-text {
  font-size: 26rpx;
  color: #666;
}

.record-remark {
  font-size: 24rpx;
  color: #999;
  margin-top: 12rpx;
  padding-top: 12rpx;
  border-top: 2rpx solid #e0e0e0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>
