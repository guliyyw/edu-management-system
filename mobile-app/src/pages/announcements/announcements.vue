<template>
  <view class="announcements-container">
    <!-- 公告列表 -->
    <scroll-view class="announcements-list" scroll-y refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
      <view v-if="announcements.length > 0" class="list-content">
        <view
          v-for="item in announcements"
          :key="item.id"
          class="announcement-card"
          :class="{ important: item.important }"
          @click="showDetail(item)"
        >
          <view class="card-header">
            <view class="title-section">
              <view v-if="item.important" class="important-badge">
                <text class="badge-text">重要</text>
              </view>
              <text class="announcement-title">{{ item.title }}</text>
            </view>
            <text class="publish-time">{{ formatDate(item.publishTime) }}</text>
          </view>
          <text class="announcement-content">{{ item.content }}</text>
          <view class="card-footer">
            <text class="publisher">👤 {{ item.publisher }}</text>
            <text class="view-detail">查看详情 ›</text>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon">📢</text>
        <text class="empty-title">暂无公告</text>
        <text class="empty-desc">暂时没有新的公告信息</text>
      </view>
    </scroll-view>

    <!-- 详情弹窗 -->
    <uni-popup ref="detailPopup" type="center">
      <view class="detail-popup">
        <view class="popup-header">
          <view class="header-title">
            <view v-if="selectedItem?.important" class="important-badge">
              <text class="badge-text">重要</text>
            </view>
            <text class="title-text">公告详情</text>
          </view>
          <text class="close-btn" @click="closeDetail">✕</text>
        </view>
        <scroll-view class="popup-content" scroll-y>
          <text class="detail-title">{{ selectedItem?.title }}</text>
          <view class="detail-meta">
            <text class="meta-item">👤 {{ selectedItem?.publisher }}</text>
            <text class="meta-item">📅 {{ formatFullDate(selectedItem?.publishTime) }}</text>
          </view>
          <text class="detail-body">{{ selectedItem?.content }}</text>
        </scroll-view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAnnouncements } from '../../api/announcement'
import type { Announcement } from '../../api/announcement'

const announcements = ref<Announcement[]>([])
const refreshing = ref(false)
const detailPopup = ref()
const selectedItem = ref<Announcement | null>(null)

onMounted(() => {
  loadAnnouncements()
})

const loadAnnouncements = async () => {
  try {
    announcements.value = await getAnnouncements()
  } catch (error) {
    console.error('加载公告失败:', error)
  }
}

const onRefresh = async () => {
  refreshing.value = true
  await loadAnnouncements()
  refreshing.value = false
}

const showDetail = (item: Announcement) => {
  selectedItem.value = item
  detailPopup.value?.open()
}

const closeDetail = () => {
  detailPopup.value?.close()
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return minutes === 0 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
}

const formatFullDate = (dateStr: string | undefined) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}
</script>

<style scoped>
.announcements-container {
  min-height: 100vh;
  background: #f5f6fa;
}

.announcements-list {
  height: 100vh;
  padding: 20rpx;
}

.list-content {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.announcement-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  border-left: 6rpx solid transparent;
}

.announcement-card.important {
  border-left-color: #ff6b6b;
  background: linear-gradient(to right, #fff5f5, #fff);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.title-section {
  display: flex;
  align-items: center;
  flex: 1;
  margin-right: 20rpx;
}

.important-badge {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.badge-text {
  font-size: 22rpx;
  color: #fff;
  font-weight: bold;
}

.announcement-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  line-height: 1.4;
}

.publish-time {
  font-size: 24rpx;
  color: #999;
  flex-shrink: 0;
}

.announcement-content {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 20rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20rpx;
  border-top: 2rpx solid #f0f0f0;
}

.publisher {
  font-size: 26rpx;
  color: #999;
}

.view-detail {
  font-size: 26rpx;
  color: #667eea;
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

.detail-popup {
  background: #fff;
  border-radius: 24rpx;
  width: 640rpx;
  max-height: 800rpx;
  display: flex;
  flex-direction: column;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.header-title {
  display: flex;
  align-items: center;
}

.title-text {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.close-btn {
  font-size: 40rpx;
  color: #999;
  padding: 10rpx;
}

.popup-content {
  padding: 30rpx;
  max-height: 600rpx;
}

.detail-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  gap: 30rpx;
  margin-bottom: 30rpx;
  padding-bottom: 24rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.meta-item {
  font-size: 26rpx;
  color: #999;
}

.detail-body {
  font-size: 30rpx;
  color: #666;
  line-height: 1.8;
}
</style>
