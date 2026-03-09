<template>
  <div class="my-stats">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalLessons }}</div>
          <div class="stat-label">本月课时</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalStudents }}</div>
          <div class="stat-label">学生人次</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalHours }}</div>
          <div class="stat-label">总课时数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalAmount.toFixed(2) }}</div>
          <div class="stat-label">总授课金额(元)</div>
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

      <el-table :data="lessonStats" border height="calc(100vh - 340px)" style="width: 100%">
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="courseName" label="课程" />
        <el-table-column prop="campusName" label="校区" />
        <el-table-column prop="studentCount" label="学生数" width="100" />
        <el-table-column prop="presentCount" label="到课" width="80" />
        <el-table-column prop="leaveCount" label="请假" width="80" />
        <el-table-column prop="absentCount" label="缺课" width="80" />
        <el-table-column prop="duration" label="时长(小时)" width="100" />
        <el-table-column prop="teacherFee" label="课时费(元)" width="100">
          <template #default="{ row }">
            {{ row.teacherFee.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="lessonAmount" label="授课金额(元)" width="120">
          <template #default="{ row }">
            {{ row.lessonAmount.toFixed(2) }}
          </template>
        </el-table-column>
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
  totalHours: 0,
  totalAmount: 0
})
const lessonStats = ref([])
const dateRange = ref([
  dayjs().startOf('month').toDate(),
  dayjs().endOf('month').toDate()
])

// 计算课程时长（小时）
const calculateDuration = (startTime: string, endTime: string): number => {
  if (!startTime || !endTime) return 0

  // 提取时间部分（格式可能是 "HH:mm:ss" 或 "HH:mm"）
  const extractTime = (timeStr: string): string => {
    if (!timeStr) return ''
    // 如果包含日期部分，只取时间部分
    if (timeStr.includes('T')) {
      return timeStr.split('T')[1].substring(0, 5)
    }
    // 如果已经是时间格式，取前5位 (HH:mm)
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

const fetchStats = async () => {
  if (!authStore.user?.teacherId) return

  const startDate = dayjs(dateRange.value[0]).format('YYYY-MM-DD')
  const endDate = dayjs(dateRange.value[1]).format('YYYY-MM-DD')

  try {
    const res = await lessonApi.getByTeacherAndRange(authStore.user.teacherId, startDate, endDate)
    const lessons = res.data || []

    let totalStudents = 0
    let totalHours = 0
    let totalAmount = 0

    // 统计课时
    stats.value.totalLessons = lessons.length

    // 格式化表格数据
    lessonStats.value = lessons.map((lesson: any) => {
      const studentCount = lesson.attendances?.length || 0
      const presentCount = lesson.attendances?.filter((a: any) => a.status === 'PRESENT').length || 0
      const leaveCount = lesson.attendances?.filter((a: any) => a.status === 'LEAVE').length || 0
      const absentCount = lesson.attendances?.filter((a: any) => a.status === 'ABSENT').length || 0

      const duration = calculateDuration(lesson.startTime, lesson.endTime)

      // 使用老师课时费计算老师授课金额（按到课人数计算，每人一节课时费）
      // 仿照管理员报表中心授课统计的计费逻辑
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

    // 更新统计数据
    stats.value.totalStudents = totalStudents
    stats.value.totalHours = parseFloat(totalHours.toFixed(1))
    stats.value.totalAmount = totalAmount
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
  font-size: 32px;
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
