<template>
  <div class="global-reports">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>全局报表</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="6">
          <el-statistic title="本月总课时" :value="stats.totalLessons" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="本月到课人次" :value="stats.presentCount" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="本月请假人次" :value="stats.leaveCount" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="本月缺课人次" :value="stats.absentCount" />
        </el-col>
      </el-row>
      
      <el-divider />
      
      <h4>各校区课时统计</h4>
      <el-table :data="campusStats" border style="margin-top: 20px;">
        <el-table-column prop="campusName" label="校区" />
        <el-table-column prop="lessonCount" label="课时数" />
        <el-table-column prop="studentCount" label="学生人次" />
        <el-table-column prop="teacherCount" label="授课老师" />
      </el-table>
      
      <el-divider />
      
      <h4>各老师课时统计</h4>
      <el-table :data="teacherStats" border style="margin-top: 20px;">
        <el-table-column prop="teacherName" label="老师" />
        <el-table-column prop="lessonCount" label="课时数" />
        <el-table-column prop="studentCount" label="学生人次" />
        <el-table-column prop="campusCount" label="授课校区" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { lessonApi } from '@/api/lesson'
import { campusApi } from '@/api/campus'
import { teacherApi } from '@/api/teacher'
import dayjs from 'dayjs'

const stats = ref({
  totalLessons: 0,
  presentCount: 0,
  leaveCount: 0,
  absentCount: 0
})

const campusStats = ref([])
const teacherStats = ref([])

const fetchStats = async () => {
  try {
    const startDate = dayjs().startOf('month').format('YYYY-MM-DD')
    const endDate = dayjs().endOf('month').format('YYYY-MM-DD')
    
    // 获取所有课节
    const [campusesRes, teachersRes] = await Promise.all([
      campusApi.getAll(),
      teacherApi.getAll()
    ])
    
    const campuses = campusesRes.data || []
    const teachers = teachersRes.data || []
    
    // 统计各校区数据
    campusStats.value = campuses.map((campus: any) => ({
      campusName: campus.name,
      lessonCount: 0,
      studentCount: 0,
      teacherCount: 0
    }))
    
    // 统计各老师数据
    teacherStats.value = teachers.map((teacher: any) => ({
      teacherName: teacher.name,
      lessonCount: 0,
      studentCount: 0,
      campusCount: teacher.campuses?.length || 0
    }))
    
    // TODO: 获取实际课时数据并统计
    stats.value = {
      totalLessons: 120,
      presentCount: 980,
      leaveCount: 45,
      absentCount: 12
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.global-reports {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
