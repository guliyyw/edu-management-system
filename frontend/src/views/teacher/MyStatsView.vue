<template>
  <div class="my-stats">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalLessons }}</div>
          <div class="stat-label">本月课时</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalStudents }}</div>
          <div class="stat-label">学生人次</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalHours }}</div>
          <div class="stat-label">总课时数</div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card class="mt-20">
      <template #header>
        <div class="card-header">
          <span>课时统计</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            size="small"
            @change="fetchStats"
          />
        </div>
      </template>
      
      <el-table :data="lessonStats" border>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="courseName" label="课程" />
        <el-table-column prop="campusName" label="校区" />
        <el-table-column prop="studentCount" label="学生数" width="100" />
        <el-table-column prop="presentCount" label="到课" width="80" />
        <el-table-column prop="leaveCount" label="请假" width="80" />
        <el-table-column prop="absentCount" label="缺课" width="80" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { lessonApi } from '@/api/lesson'
import dayjs from 'dayjs'

const authStore = useAuthStore()

const stats = ref({
  totalLessons: 0,
  totalStudents: 0,
  totalHours: 0
})
const lessonStats = ref([])
const dateRange = ref([
  dayjs().startOf('month').toDate(),
  dayjs().endOf('month').toDate()
])

const fetchStats = async () => {
  if (!authStore.user?.teacherId) return
  
  const startDate = dayjs(dateRange.value[0]).format('YYYY-MM-DD')
  const endDate = dayjs(dateRange.value[1]).format('YYYY-MM-DD')
  
  try {
    const res = await lessonApi.getByTeacherAndRange(authStore.user.teacherId, startDate, endDate)
    const lessons = res.data || []
    
    // 统计课时
    stats.value.totalLessons = lessons.length
    stats.value.totalStudents = lessons.reduce((sum: number, lesson: any) => 
      sum + (lesson.attendances?.length || 0), 0)
    stats.value.totalHours = lessons.length
    
    // 格式化表格数据
    lessonStats.value = lessons.map((lesson: any) => ({
      date: lesson.date,
      courseName: lesson.classInfo.course.name,
      campusName: lesson.classInfo.campus.name,
      studentCount: lesson.attendances?.length || 0,
      presentCount: lesson.attendances?.filter((a: any) => a.status === 'PRESENT').length || 0,
      leaveCount: lesson.attendances?.filter((a: any) => a.status === 'LEAVE').length || 0,
      absentCount: lesson.attendances?.filter((a: any) => a.status === 'ABSENT').length || 0
    }))
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.my-stats {
  padding: 20px;
}

.stat-card {
  text-align: center;
  padding: 20px;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.mt-20 {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
