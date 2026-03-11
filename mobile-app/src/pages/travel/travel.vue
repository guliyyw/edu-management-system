<template>
  <view class="travel-container">
    <!-- 提示信息 -->
    <view class="tips-section">
      <text class="tips-text">💡 设置校区之间的路程时间，系统将自动检测跨校区排课冲突</text>
    </view>

    <!-- 添加按钮 -->
    <view class="add-section">
      <button class="add-btn" @click="showAddModal">
        <text class="add-icon">+</text>
        <text class="add-text">添加路程配置</text>
      </button>
    </view>

    <!-- 路程配置列表 -->
    <scroll-view class="travel-list" scroll-y>
      <view v-if="travelTimes.length > 0" class="travel-items">
        <view
          v-for="item in travelTimes"
          :key="item.id"
          class="travel-card"
        >
          <view class="travel-route">
            <view class="campus from">
              <text class="campus-label">起点</text>
              <text class="campus-name">{{ item.fromCampus?.name }}</text>
            </view>
            <view class="route-arrow">
              <text class="arrow-icon">→</text>
              <text class="travel-time">{{ item.travelMinutes }}分钟</text>
            </view>
            <view class="campus to">
              <text class="campus-label">终点</text>
              <text class="campus-name">{{ item.toCampus?.name }}</text>
            </view>
          </view>
          <view class="travel-footer">
            <text class="effective-date">生效日期: {{ item.effectiveDate }}</text>
            <view class="actions">
              <text class="action-btn edit" @click="editTravelTime(item)">编辑</text>
              <text class="action-btn delete" @click="deleteTravelTime(item.id)">删除</text>
            </view>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon">🚗</text>
        <text class="empty-text">暂无路程配置</text>
        <text class="empty-desc">点击上方按钮添加配置</text>
      </view>
    </scroll-view>

    <!-- 添加/编辑弹窗 -->
    <uni-popup ref="modalRef" type="center">
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">{{ isEdit ? '编辑路程配置' : '添加路程配置' }}</text>
          <text class="modal-close" @click="closeModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">起点校区</text>
            <picker @change="onFromCampusChange" :value="fromCampusIndex" :range="campusNames">
              <view class="picker-value" :class="{ placeholder: !form.fromCampusId }">
                {{ form.fromCampusId ? campusNames[fromCampusIndex] : '请选择起点校区' }}
                <text class="picker-arrow">▼</text>
              </view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">终点校区</text>
            <picker @change="onToCampusChange" :value="toCampusIndex" :range="campusNames">
              <view class="picker-value" :class="{ placeholder: !form.toCampusId }">
                {{ form.toCampusId ? campusNames[toCampusIndex] : '请选择终点校区' }}
                <text class="picker-arrow">▼</text>
              </view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">路程时间 (分钟)</text>
            <slider :value="form.travelMinutes" :min="5" :max="180" :step="5" show-value @change="onTravelMinutesChange" />
          </view>
          <view class="form-item">
            <text class="form-label">生效日期</text>
            <picker mode="date" :value="form.effectiveDate" @change="onDateChange">
              <view class="picker-value">
                {{ form.effectiveDate }}
                <text class="picker-arrow">▼</text>
              </view>
            </picker>
          </view>
        </view>
        <view class="modal-footer">
          <button class="btn-cancel" @click="closeModal">取消</button>
          <button class="btn-confirm" @click="saveTravelTime">保存</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { getTeacherTravelTimes, createTravelTime, updateTravelTime, deleteTravelTime, type TravelTime, type TravelTimeForm } from '../../api/travelTime'
import { getActiveCampuses, type Campus } from '../../api/campus'

const authStore = useAuthStore()
const travelTimes = ref<TravelTime[]>([])
const campuses = ref<Campus[]>([])
const modalRef = ref()
const isEdit = ref(false)
const editingId = ref<number | null>(null)

const form = ref<TravelTimeForm>({
  fromCampusId: 0,
  toCampusId: 0,
  travelMinutes: 30,
  effectiveDate: formatDate(new Date())
})

const campusNames = computed(() => campuses.value.map(c => c.name))
const fromCampusIndex = computed(() => campuses.value.findIndex(c => c.id === form.value.fromCampusId))
const toCampusIndex = computed(() => campuses.value.findIndex(c => c.id === form.value.toCampusId))

onMounted(() => {
  fetchTravelTimes()
  fetchCampuses()
})

function formatDate(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

async function fetchTravelTimes() {
  if (!authStore.userInfo?.teacherId) {
    uni.showToast({ title: '非教师账号', icon: 'none' })
    return
  }

  try {
    travelTimes.value = await getTeacherTravelTimes(authStore.userInfo.teacherId)
  } catch (error) {
    console.error('获取路程配置失败:', error)
    uni.showToast({ title: '获取配置失败', icon: 'none' })
  }
}

async function fetchCampuses() {
  try {
    campuses.value = await getActiveCampuses()
  } catch (error) {
    console.error('获取校区失败:', error)
  }
}

function showAddModal() {
  isEdit.value = false
  editingId.value = null
  form.value = {
    fromCampusId: 0,
    toCampusId: 0,
    travelMinutes: 30,
    effectiveDate: formatDate(new Date())
  }
  modalRef.value?.open()
}

function editTravelTime(item: TravelTime) {
  isEdit.value = true
  editingId.value = item.id
  form.value = {
    fromCampusId: item.fromCampus?.id || 0,
    toCampusId: item.toCampus?.id || 0,
    travelMinutes: item.travelMinutes,
    effectiveDate: item.effectiveDate
  }
  modalRef.value?.open()
}

function closeModal() {
  modalRef.value?.close()
}

function onFromCampusChange(e: any) {
  form.value.fromCampusId = campuses.value[e.detail.value]?.id || 0
}

function onToCampusChange(e: any) {
  form.value.toCampusId = campuses.value[e.detail.value]?.id || 0
}

function onTravelMinutesChange(e: any) {
  form.value.travelMinutes = e.detail.value
}

function onDateChange(e: any) {
  form.value.effectiveDate = e.detail.value
}

async function saveTravelTime() {
  if (!form.value.fromCampusId || !form.value.toCampusId) {
    uni.showToast({ title: '请选择校区', icon: 'none' })
    return
  }

  if (form.value.fromCampusId === form.value.toCampusId) {
    uni.showToast({ title: '起点和终点不能相同', icon: 'none' })
    return
  }

  try {
    const data = {
      ...form.value,
      teacherId: authStore.userInfo?.teacherId
    }

    if (isEdit.value && editingId.value) {
      await updateTravelTime(editingId.value, data)
      uni.showToast({ title: '更新成功', icon: 'success' })
    } else {
      await createTravelTime(data)
      uni.showToast({ title: '添加成功', icon: 'success' })
    }
    closeModal()
    fetchTravelTimes()
  } catch (error) {
    uni.showToast({ title: isEdit.value ? '更新失败' : '添加失败', icon: 'none' })
  }
}

function deleteTravelTime(id: number) {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除此路程配置吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteTravelTime(id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          fetchTravelTimes()
        } catch (error) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}
</script>

<style scoped>
.travel-container {
  min-height: 100vh;
  background: #f5f6fa;
}

.tips-section {
  background: #e3f2fd;
  padding: 24rpx 30rpx;
  border-bottom: 2rpx solid #bbdefb;
}

.tips-text {
  font-size: 26rpx;
  color: #1976d2;
  line-height: 1.6;
}

.add-section {
  padding: 30rpx;
}

.add-btn {
  width: 100%;
  height: 100rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
}

.add-btn::after {
  border: none;
}

.add-icon {
  font-size: 40rpx;
  color: #fff;
  margin-right: 12rpx;
}

.add-text {
  font-size: 32rpx;
  color: #fff;
  font-weight: 500;
}

.travel-list {
  height: calc(100vh - 280rpx);
  padding: 0 30rpx 30rpx;
}

.travel-items {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.travel-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.travel-route {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.campus {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.campus-label {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.campus-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.route-arrow {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 30rpx;
}

.arrow-icon {
  font-size: 48rpx;
  color: #667eea;
}

.travel-time {
  font-size: 28rpx;
  color: #667eea;
  font-weight: bold;
  background: #f0f0f0;
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  margin-top: 8rpx;
}

.travel-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20rpx;
  border-top: 2rpx solid #f0f0f0;
}

.effective-date {
  font-size: 26rpx;
  color: #999;
}

.actions {
  display: flex;
  gap: 24rpx;
}

.action-btn {
  font-size: 28rpx;
  padding: 8rpx 16rpx;
}

.action-btn.edit {
  color: #667eea;
}

.action-btn.delete {
  color: #f44336;
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
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.empty-desc {
  font-size: 28rpx;
  color: #999;
}

.modal-content {
  background: #fff;
  border-radius: 24rpx;
  width: 640rpx;
  max-height: 900rpx;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.modal-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.modal-close {
  font-size: 40rpx;
  color: #999;
  padding: 10rpx;
}

.modal-body {
  padding: 30rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
  font-weight: 500;
}

.picker-value {
  height: 90rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 30rpx;
  color: #333;
}

.picker-value.placeholder {
  color: #999;
}

.picker-arrow {
  font-size: 24rpx;
  color: #999;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx 30rpx;
  border-top: 2rpx solid #f0f0f0;
}

.btn-cancel, .btn-confirm {
  flex: 1;
  height: 90rpx;
  border-radius: 45rpx;
  font-size: 32rpx;
  border: none;
}

.btn-cancel::after, .btn-confirm::after {
  border: none;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-confirm {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}
</style>
