<template>
  <div class="schedule">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>排课中心</span>
          <div class="header-actions">
            <el-select v-model="selectedClass" placeholder="选择班级" style="width: 200px; margin-right: 10px;">
              <el-option
                v-for="cls in classes"
                :key="cls.id"
                :label="`${cls.course.name} - ${cls.teacher.name}`"
                :value="cls.id"
              />
            </el-select>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="margin-right: 10px;"
            />
            <el-button type="primary" @click="generateLessons">批量排课</el-button>
          </div>
        </div>
      </template>
      
      <el-alert
        v-if="conflictInfo"
        :title="conflictInfo"
        type="warning"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />
      
      <el-calendar v-model="calendarDate">
        <template #date-cell="{ data }">
          <div class="calendar-cell">
            <p :class="data.isSelected ? 'is-selected' : ''">
              {{ data.day.split('-').slice(1).join('-') }}
            </p>
            <div v-for="lesson in getLessonsByDate(data.day)" :key="lesson.id" class="lesson-tag">
              <el-tag size="small" :type="getLessonType(lesson)">
                {{ lesson.classInfo.course.name }}
              </el-tag>
            </div>
          </div>
        </template>
      </el-calendar>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { classApi } from '@/api/class'
import { lessonApi } from '@/api/lesson'
import dayjs from 'dayjs'

const classes = ref([])
const selectedClass = ref<number | null>(null)
const dateRange = ref([new Date(), dayjs().add(1, 'month').toDate()])
const calendarDate = ref(new Date())
const lessons = ref([])
const conflictInfo = ref('')

const getLessonsByDate = (date: string) => {
  return lessons.value.filter(lesson => lesson.date === date)
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

const fetchClasses = async () => {
  try {
    const res = await classApi.getAll()
    classes.value = res.data || []
  } catch (error) {
    console.error('获取班级失败:', error)
  }
}

const fetchLessons = async () => {
  if (!selectedClass.value) return
  
  try {
    const res = await lessonApi.getByClass(selectedClass.value)
    lessons.value = res.data || []
  } catch (error) {
    console.error('获取课节失败:', error)
  }
}

const generateLessons = async () => {
  if (!selectedClass.value || !dateRange.value[0] || !dateRange.value[1]) {
    ElMessage.warning('请选择班级和日期范围')
    return
  }
  
  try {
    const startDate = dayjs(dateRange.value[0]).format('YYYY-MM-DD')
    const endDate = dayjs(dateRange.value[1]).format('YYYY-MM-DD')
    
    await lessonApi.generate(selectedClass.value, startDate, endDate)
    ElMessage.success('排课成功')
    fetchLessons()
  } catch (error: any) {
    conflictInfo.value = error.response?.data?.message || '排课失败'
    ElMessage.error('排课失败')
  }
}

onMounted(() => {
  fetchClasses()
})
</script>

<style scoped>
.schedule {
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

.calendar-cell {
  min-height: 80px;
}

.is-selected {
  color: #1989fa;
}

.lesson-tag {
  margin-top: 5px;
}
</style>
