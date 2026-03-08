<template>
  <div class="reports">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报表中心</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="学生课时消耗" name="student">
          <el-form :inline="true" class="search-form">
            <el-form-item label="学生">
              <el-select v-model="studentForm.studentId" placeholder="选择学生" style="width: 200px;">
                <el-option
                  v-for="student in students"
                  :key="student.id"
                  :label="student.name"
                  :value="student.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="studentForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="queryStudentReport">查询</el-button>
              <el-button @click="exportStudentReport">导出Excel</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="studentReportData" border v-loading="loading">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="courseName" label="课程" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" />
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="老师授课统计" name="teacher">
          <el-form :inline="true" class="search-form">
            <el-form-item label="老师">
              <el-select v-model="teacherForm.teacherId" placeholder="选择老师" style="width: 200px;">
                <el-option
                  v-for="teacher in teachers"
                  :key="teacher.id"
                  :label="teacher.name"
                  :value="teacher.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="teacherForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="queryTeacherReport">查询</el-button>
              <el-button @click="exportTeacherReport">导出Excel</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="teacherReportData" border v-loading="loading">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="courseName" label="课程" />
            <el-table-column prop="studentCount" label="学生数" width="100" />
            <el-table-column prop="presentCount" label="到课" width="80" />
            <el-table-column prop="leaveCount" label="请假" width="80" />
            <el-table-column prop="absentCount" label="缺课" width="80" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { studentApi } from '@/api/student'
import { teacherApi } from '@/api/teacher'
import { lessonApi } from '@/api/lesson'
import dayjs from 'dayjs'

const activeTab = ref('student')
const loading = ref(false)
const students = ref([])
const teachers = ref([])
const studentReportData = ref([])
const teacherReportData = ref([])

const studentForm = ref({
  studentId: null as number | null,
  dateRange: [dayjs().startOf('month').toDate(), dayjs().endOf('month').toDate()]
})

const teacherForm = ref({
  teacherId: null as number | null,
  dateRange: [dayjs().startOf('month').toDate(), dayjs().endOf('month').toDate()]
})

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

const fetchOptions = async () => {
  try {
    const [studentsRes, teachersRes] = await Promise.all([
      studentApi.getAll(),
      teacherApi.getAll()
    ])
    students.value = studentsRes.data || []
    teachers.value = teachersRes.data || []
  } catch (error) {
    console.error('获取选项失败:', error)
  }
}

const queryStudentReport = async () => {
  if (!studentForm.value.studentId) {
    ElMessage.warning('请选择学生')
    return
  }
  
  loading.value = true
  try {
    // TODO: 实现学生课时消耗报表查询
    ElMessage.info('功能开发中...')
  } finally {
    loading.value = false
  }
}

const queryTeacherReport = async () => {
  if (!teacherForm.value.teacherId) {
    ElMessage.warning('请选择老师')
    return
  }
  
  loading.value = true
  try {
    const startDate = dayjs(teacherForm.value.dateRange[0]).format('YYYY-MM-DD')
    const endDate = dayjs(teacherForm.value.dateRange[1]).format('YYYY-MM-DD')
    
    const res = await lessonApi.getByTeacherAndRange(teacherForm.value.teacherId, startDate, endDate)
    teacherReportData.value = (res.data || []).map((lesson: any) => ({
      date: lesson.date,
      courseName: lesson.classInfo.course.name,
      studentCount: lesson.attendances?.length || 0,
      presentCount: lesson.attendances?.filter((a: any) => a.status === 'PRESENT').length || 0,
      leaveCount: lesson.attendances?.filter((a: any) => a.status === 'LEAVE').length || 0,
      absentCount: lesson.attendances?.filter((a: any) => a.status === 'ABSENT').length || 0
    }))
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    loading.value = false
  }
}

const exportStudentReport = () => {
  ElMessage.info('导出功能开发中...')
}

const exportTeacherReport = () => {
  ElMessage.info('导出功能开发中...')
}

onMounted(() => {
  fetchOptions()
})
</script>

<style scoped>
.reports {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>
