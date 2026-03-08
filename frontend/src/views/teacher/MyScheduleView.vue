<template>
  <div class="my-schedule">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的课表</span>
          <div class="header-actions">
            <el-radio-group v-model="viewMode" size="small">
              <el-radio-button label="week">周视图</el-radio-button>
              <el-radio-button label="day">日视图</el-radio-button>
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
      
      <!-- 周视图 -->
      <div v-if="viewMode === 'week'" class="week-view">
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
                :style="{ backgroundColor: getCourseColor(lesson.classInfo.course.name) }"
                @click="showLessonDetail(lesson)"
              >
                <div class="lesson-name">{{ lesson.classInfo.course.name }}</div>
                <div class="lesson-info">{{ lesson.classInfo.campus.name }}</div>
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
                  <h4>{{ lesson.classInfo.course.name }}</h4>
                  <el-tag size="small">{{ lesson.status }}</el-tag>
                </div>
                <div class="lesson-meta">
                  <p><el-icon><Location /></el-icon> {{ lesson.classInfo.campus.name }} - {{ lesson.classroom }}</p>
                  <p><el-icon><User /></el-icon> 学生数: {{ lesson.attendances?.length || 0 }}</p>
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
    <el-dialog v-model="detailVisible" title="课程详情" width="500px">
      <div v-if="selectedLesson" class="lesson-detail-dialog">
        <p><strong>课程：</strong>{{ selectedLesson.classInfo.course.name }}</p>
        <p><strong>时间：</strong>{{ selectedLesson.date }} {{ selectedLesson.startTime }} - {{ selectedLesson.endTime }}</p>
        <p><strong>校区：</strong>{{ selectedLesson.classInfo.campus.name }}</p>
        <p><strong>教室：</strong>{{ selectedLesson.classroom }}</p>
        <p><strong>学生列表：</strong></p>
        <el-table :data="selectedLesson.attendances" size="small">
          <el-table-column prop="student.name" label="学生姓名" />
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { lessonApi } from '@/api/lesson'
import dayjs from 'dayjs'

const router = useRouter()
const authStore = useAuthStore()

const viewMode = ref('week')
const selectedDate = ref(new Date())
const weekLessons = ref([])
const dayLessons = ref([])
const detailVisible = ref(false)
const selectedLesson = ref(null)

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

const courseColors = {
  '小学数学': '#409EFF',
  '小学英语': '#67C23A',
  '初中物理': '#E6A23C',
  '初中化学': '#F56C6C',
  'default': '#909399'
}

const isToday = (dayOfWeek: number) => {
  return dayjs().day() === dayOfWeek || (dayjs().day() === 0 && dayOfWeek === 7)
}

const getCourseColor = (courseName: string) => {
  return courseColors[courseName] || courseColors.default
}

const getLessonsByDayAndTime = (dayOfWeek: number, timeSlot: string) => {
  return weekLessons.value.filter(lesson => {
    const lessonDay = dayjs(lesson.date).day() || 7
    return lessonDay === dayOfWeek && lesson.startTime.startsWith(timeSlot.split(':')[0])
  })
}

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    'PENDING': 'info',
    'PRESENT': 'success',
    'ABSENT': 'danger',
    'LEAVE': 'warning',
    'TRIAL': 'primary'
  }
  return types[status] || 'info'
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
  if (viewMode.value === 'week') {
    fetchWeekLessons()
  } else {
    fetchDayLessons()
  }
}

const showLessonDetail = (lesson: any) => {
  selectedLesson.value = lesson
  detailVisible.value = true
}

const goToAttendance = (lesson: any) => {
  router.push(`/my-attendance?lessonId=${lesson.id}`)
}

watch(viewMode, () => {
  handleDateChange()
})

onMounted(() => {
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
</style>
