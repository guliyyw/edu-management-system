<template>
  <div class="my-schedule">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的课表</span>
          <div class="header-actions">
            <el-radio-group v-model="viewMode" size="small">
              <el-radio-button value="month">月视图</el-radio-button>
              <el-radio-button value="week">周视图</el-radio-button>
              <el-radio-button value="day">日视图</el-radio-button>
            </el-radio-group>
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择日期"
              size="small"
              style="width: 150px; margin-left: 10px;"
              @change="handleDateChange"
            />
          </div>
        </div>
      </template>

      <!-- 月视图 - 与管理员排课中心一致 -->
      <div v-if="viewMode === 'month'" class="month-view">
        <el-calendar v-model="calendarDate" style="height: calc(100vh - 240px);">
          <template #date-cell="{ data }">
            <div class="calendar-cell" @click="handleDateClick(data.day)">
              <p :class="data.isSelected ? 'is-selected' : ''">
                {{ data.day.split('-').slice(1).join('-') }}
              </p>
              <div
                v-for="lesson in getLessonsByDate(data.day)"
                :key="lesson.id"
                class="lesson-tag"
                @click.stop="showLessonDetail(lesson)"
              >
                <div class="lesson-header">
                  <el-tag size="small" :type="getLessonType(lesson)">
                    {{ getCourseTypeLabel(lesson.classInfo?.course?.type) }}
                  </el-tag>
                  <span class="lesson-grade">{{ getGradeLabel(lesson.classInfo?.gradeLevel) }}</span>
                </div>
                <div class="lesson-time-row">{{ lesson.startTime?.substring(0, 5) }} - {{ lesson.endTime?.substring(0, 5) }}</div>
                <div class="lesson-classroom">{{ lesson.classroom }}</div>
              </div>
            </div>
          </template>
        </el-calendar>
      </div>

      <!-- 周视图 -->
      <div v-else-if="viewMode === 'week'" class="week-view">
        <div class="week-header">
          <div class="time-column">时间</div>
          <div
            v-for="day in weekDays"
            :key="day.value"
            class="day-column"
            :class="{ today: isToday(day.value) }"
          >
            <div class="day-name">{{ day.label }}</div>
            <div class="day-date">{{ day.date }}</div>
          </div>
        </div>

        <div class="week-body">
          <div
            v-for="timeSlot in timeSlots"
            :key="timeSlot"
            class="time-row"
          >
            <div class="time-label">{{ timeSlot }}</div>
            <div
              v-for="day in weekDays"
              :key="day.value"
              class="time-cell"
              :class="{ today: isToday(day.value) }"
            >
              <div
                v-for="lesson in getLessonsByDayAndTime(day.value, timeSlot)"
                :key="lesson.id"
                class="lesson-card"
                :style="{ backgroundColor: getCourseColor(lesson.classInfo?.course?.type) }"
                @click="showLessonDetail(lesson)"
              >
                <div class="lesson-name">{{ getCourseTypeLabel(lesson.classInfo?.course?.type) }}</div>
                <div class="lesson-info">{{ lesson.classInfo?.campus?.name }}</div>
                <div class="lesson-info">{{ lesson.classroom }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 日视图 -->
      <div v-else class="day-view">
        <el-timeline>
          <el-timeline-item
            v-for="lesson in dayLessons"
            :key="lesson.id"
            :timestamp="`${lesson.startTime} - ${lesson.endTime}`"
            placement="top"
          >
            <el-card :body-style="{ padding: '15px' }">
              <div class="lesson-detail">
                <div class="lesson-title">
                  <h4>{{ getCourseTypeLabel(lesson.classInfo?.course?.type) }}</h4>
                  <el-tag size="small" :type="getStatusType(lesson.status)">{{ getStatusLabel(lesson.status) }}</el-tag>
                </div>
                <div class="lesson-meta">
                  <p><el-icon><Location /></el-icon> {{ lesson.classInfo?.campus?.name }} - {{ lesson.classroom }}</p>
                  <p><el-icon><User /></el-icon> 学生数: {{ lesson.attendances?.length || 0 }}</p>
                  <p><el-icon><Clock /></el-icon> {{ lesson.startTime }} - {{ lesson.endTime }}</p>
                </div>
                <div class="lesson-actions">
                  <el-button type="primary" size="small" @click="goToAttendance(lesson)">
                    签到
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>

        <el-empty v-if="dayLessons.length === 0" description="今日暂无课程" />
      </div>
    </el-card>

    <!-- 课程详情对话框 -->
    <el-dialog v-model="detailVisible" title="课程详情" width="500px" destroy-on-close>
      <div v-if="selectedLesson" class="lesson-detail-dialog">
        <p><strong>课程：</strong>{{ getCourseTypeLabel(selectedLesson.classInfo?.course?.type) }}</p>
        <p><strong>时间：</strong>{{ selectedLesson.date }} {{ selectedLesson.startTime }} - {{ selectedLesson.endTime }}</p>
        <p><strong>校区：</strong>{{ selectedLesson.classInfo?.campus?.name }}</p>
        <p><strong>教室：</strong>{{ selectedLesson.classroom }}</p>
        <p><strong>状态：</strong><el-tag :type="getStatusType(selectedLesson.status)">{{ getStatusLabel(selectedLesson.status) }}</el-tag></p>
        <p><strong>学生列表：</strong></p>
        <el-table :data="selectedLesson.attendances" size="small" border>
          <el-table-column prop="student.name" label="学生姓名" />
          <el-table-column prop="student.gradeLevel" label="年级">
            <template #default="{ row }">
              {{ getGradeLabel(row.student?.gradeLevel) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getAttendanceType(row.status)" size="small">{{ getAttendanceLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToAttendance(selectedLesson)" v-if="selectedLesson">去签到</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { lessonApi } from '@/api/lesson'
import { Location, User, Clock } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const router = useRouter()
const authStore = useAuthStore()

const viewMode = ref('month')
const selectedDate = ref(new Date())
const calendarDate = ref(new Date())
const monthLessons = ref([])
const weekLessons = ref([])
const dayLessons = ref([])
const detailVisible = ref(false)
const selectedLesson = ref(null)

// 课程类型标签映射
const courseTypeLabels: Record<string, string> = {
  'CHINESE': '语文',
  'MATH': '数学',
  'ENGLISH': '英语',
  'PHYSICS': '物理',
  'CHEMISTRY': '化学',
  'BIOLOGY': '生物',
  'HISTORY': '历史',
  'GEOGRAPHY': '地理',
  'POLITICS': '政治',
  'ART': '美术',
  'MUSIC': '音乐',
  'SPORTS': '体育',
  'OTHER': '其他'
}

const getCourseTypeLabel = (type: string) => {
  return courseTypeLabels[type] || type || '课程'
}

// 年级标签映射
const gradeLabels: Record<string, string> = {
  'PRESCHOOL': '学前',
  'GRADE_1': '一年级',
  'GRADE_2': '二年级',
  'GRADE_3': '三年级',
  'GRADE_4': '四年级',
  'GRADE_5': '五年级',
  'GRADE_6': '六年级',
  'GRADE_7': '初一',
  'GRADE_8': '初二',
  'GRADE_9': '初三',
  'GRADE_10': '高一',
  'GRADE_11': '高二',
  'GRADE_12': '高三',
  'ADULT': '成人'
}

const getGradeLabel = (grade: string) => {
  return gradeLabels[grade] || grade || ''
}

const weekDays = computed(() => {
  const startOfWeek = dayjs(selectedDate.value).startOf('week').add(1, 'day') // 周一开始
  return [
    { label: '周一', value: 1, date: startOfWeek.format('MM-DD') },
    { label: '周二', value: 2, date: startOfWeek.add(1, 'day').format('MM-DD') },
    { label: '周三', value: 3, date: startOfWeek.add(2, 'day').format('MM-DD') },
    { label: '周四', value: 4, date: startOfWeek.add(3, 'day').format('MM-DD') },
    { label: '周五', value: 5, date: startOfWeek.add(4, 'day').format('MM-DD') },
    { label: '周六', value: 6, date: startOfWeek.add(5, 'day').format('MM-DD') },
    { label: '周日', value: 7, date: startOfWeek.add(6, 'day').format('MM-DD') }
  ]
})

const timeSlots = [
  '08:00', '09:00', '10:00', '11:00', '12:00',
  '13:00', '14:00', '15:00', '16:00', '17:00',
  '18:00', '19:00', '20:00'
]

const courseColors: Record<string, string> = {
  'CHINESE': '#409EFF',
  'MATH': '#67C23A',
  'ENGLISH': '#E6A23C',
  'PHYSICS': '#F56C6C',
  'CHEMISTRY': '#909399',
  'BIOLOGY': '#409EFF',
  'HISTORY': '#67C23A',
  'GEOGRAPHY': '#E6A23C',
  'POLITICS': '#F56C6C',
  'ART': '#909399',
  'MUSIC': '#409EFF',
  'SPORTS': '#67C23A',
  'OTHER': '#909399',
  'default': '#409EFF'
}

const isToday = (dayOfWeek: number) => {
  return dayjs().day() === dayOfWeek || (dayjs().day() === 0 && dayOfWeek === 7)
}

const getCourseColor = (courseType: string) => {
  return courseColors[courseType] || courseColors.default
}

const getLessonsByDate = (date: string) => {
  return monthLessons.value.filter((lesson: any) => lesson.date === date)
}

const getLessonsByDayAndTime = (dayOfWeek: number, timeSlot: string) => {
  return weekLessons.value.filter((lesson: any) => {
    const lessonDay = dayjs(lesson.date).day() || 7
    return lessonDay === dayOfWeek && lesson.startTime?.startsWith(timeSlot.split(':')[0])
  })
}

const getLessonType = (lesson: any) => {
  const types: Record<string, string> = {
    'SCHEDULED': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return types[lesson.status] || 'info'
}

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    'SCHEDULED': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'SCHEDULED': '已排课',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return labels[status] || status
}

const getAttendanceType = (status: string) => {
  const types: Record<string, string> = {
    'PENDING': 'info',
    'PRESENT': 'success',
    'ABSENT': 'danger',
    'LEAVE': 'warning',
    'TRIAL': 'primary'
  }
  return types[status] || 'info'
}

const getAttendanceLabel = (status: string) => {
  const labels: Record<string, string> = {
    'PENDING': '待签到',
    'PRESENT': '到课',
    'ABSENT': '缺课',
    'LEAVE': '请假',
    'TRIAL': '试课'
  }
  return labels[status] || status
}

const fetchMonthLessons = async () => {
  if (!authStore.user?.teacherId) return

  const startDate = dayjs(calendarDate.value).startOf('month').format('YYYY-MM-DD')
  const endDate = dayjs(calendarDate.value).endOf('month').format('YYYY-MM-DD')

  try {
    const res = await lessonApi.getByTeacherAndRange(authStore.user.teacherId, startDate, endDate)
    monthLessons.value = res.data || []
  } catch (error) {
    console.error('获取月课表失败:', error)
  }
}

const fetchWeekLessons = async () => {
  if (!authStore.user?.teacherId) return

  const startDate = dayjs(selectedDate.value).startOf('week').add(1, 'day').format('YYYY-MM-DD')
  const endDate = dayjs(selectedDate.value).startOf('week').add(7, 'day').format('YYYY-MM-DD')

  try {
    const res = await lessonApi.getByTeacherAndRange(authStore.user.teacherId, startDate, endDate)
    weekLessons.value = res.data || []
  } catch (error) {
    console.error('获取周课表失败:', error)
  }
}

const fetchDayLessons = async () => {
  if (!authStore.user?.teacherId) return

  const date = dayjs(selectedDate.value).format('YYYY-MM-DD')

  try {
    const res = await lessonApi.getByTeacherAndDate(authStore.user.teacherId, date)
    dayLessons.value = res.data || []
  } catch (error) {
    console.error('获取日课表失败:', error)
  }
}

const handleDateChange = () => {
  if (viewMode.value === 'month') {
    calendarDate.value = selectedDate.value
    fetchMonthLessons()
  } else if (viewMode.value === 'week') {
    fetchWeekLessons()
  } else {
    fetchDayLessons()
  }
}

const handleDateClick = (date: string) => {
  selectedDate.value = new Date(date)
  viewMode.value = 'day'
  fetchDayLessons()
}

const showLessonDetail = (lesson: any) => {
  selectedLesson.value = lesson
  detailVisible.value = true
}

const goToAttendance = (lesson: any) => {
  if (lesson?.id) {
    router.push(`/my-attendance?lessonId=${lesson.id}`)
  }
}

watch(viewMode, () => {
  handleDateChange()
})

watch(calendarDate, () => {
  fetchMonthLessons()
})

onMounted(() => {
  fetchMonthLessons()
  fetchWeekLessons()
  fetchDayLessons()
})
</script>

<style scoped>
.my-schedule {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

/* 月视图 */
.calendar-cell {
  min-height: 80px;
  padding: 4px;
  cursor: pointer;
  position: relative;
}

.calendar-cell:hover {
  background-color: #f5f7fa;
}

.is-selected {
  color: #1989fa;
}

.lesson-tag {
  margin-top: 5px;
  padding: 2px;
  background-color: #f0f9ff;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.lesson-tag:hover {
  background-color: #e0f2fe;
}

.lesson-header {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: nowrap;
  overflow: hidden;
}

.lesson-header .el-tag {
  flex-shrink: 0;
}

.lesson-grade {
  color: #67C23A;
  background-color: #f0f9eb;
  padding: 0 4px;
  border-radius: 2px;
  font-size: 10px;
  flex-shrink: 0;
}

.lesson-time {
  font-size: 11px;
  color: #666;
  margin-left: auto;
}

.lesson-time-row {
  font-size: 11px;
  color: #409EFF;
  font-weight: 500;
  margin-top: 3px;
}

.lesson-classroom {
  font-size: 10px;
  color: #999;
  margin-top: 2px;
}

/* 周视图 */
.week-view {
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.week-header {
  display: flex;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.time-column {
  width: 80px;
  padding: 10px;
  text-align: center;
  font-weight: bold;
  border-right: 1px solid #ebeef5;
}

.day-column {
  flex: 1;
  padding: 10px;
  text-align: center;
  border-right: 1px solid #ebeef5;
}

.day-column.today {
  background-color: #ecf5ff;
}

.day-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.day-date {
  font-size: 12px;
  color: #909399;
}

.week-body {
  max-height: 600px;
  overflow-y: auto;
}

.time-row {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  min-height: 80px;
}

.time-label {
  width: 80px;
  padding: 10px;
  text-align: center;
  font-size: 12px;
  color: #909399;
  border-right: 1px solid #ebeef5;
  background-color: #fafafa;
}

.time-cell {
  flex: 1;
  padding: 5px;
  border-right: 1px solid #ebeef5;
  position: relative;
}

.time-cell.today {
  background-color: #f5f7fa;
}

.lesson-card {
  padding: 8px;
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
  cursor: pointer;
  margin-bottom: 5px;
}

.lesson-card:hover {
  opacity: 0.9;
}

.lesson-name {
  font-weight: bold;
  margin-bottom: 3px;
}

.lesson-info {
  font-size: 11px;
  opacity: 0.9;
}

/* 日视图 */
.day-view {
  padding: 20px;
}

.lesson-detail {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.lesson-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.lesson-title h4 {
  margin: 0;
}

.lesson-meta {
  color: #606266;
  font-size: 14px;
}

.lesson-meta p {
  margin: 5px 0;
  display: flex;
  align-items: center;
  gap: 5px;
}

.lesson-detail-dialog p {
  margin: 10px 0;
}

:deep(.el-calendar-day) {
  min-height: 100px;
  height: auto;
}
</style>
