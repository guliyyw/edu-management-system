<template>
  <div class="reports">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报表中心</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="学生上课情况" name="student">
          <el-form :inline="true" class="search-form">
            <el-form-item label="学生">
              <el-select v-model="studentForm.studentId" placeholder="选择学生" style="width: 200px;" clearable>
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
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="queryStudentReport">查询</el-button>
              <el-button @click="exportStudentReport">导出Excel</el-button>
            </el-form-item>
          </el-form>

          <el-statistic v-if="studentReportData.length > 0" :value="studentReportData.length" title="总课时" class="stat-item" />

          <el-table :data="studentReportData" border v-loading="loading">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="courseName" label="课程" />
            <el-table-column prop="teacherName" label="老师" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" />
          </el-table>

          <el-empty v-if="!loading && studentReportData.length === 0" description="暂无数据" />
        </el-tab-pane>

        <el-tab-pane label="老师授课统计" name="teacher">
          <el-form :inline="true" class="search-form">
            <el-form-item label="老师">
              <el-select v-model="teacherForm.teacherId" placeholder="选择老师" style="width: 200px;" clearable>
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
                value-format="YYYY-MM-DD"
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

          <el-empty v-if="!loading && teacherReportData.length === 0" description="暂无数据" />
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
  dateRange: [dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().endOf('month').format('YYYY-MM-DD')] as [string, string]
})

const teacherForm = ref({
  teacherId: null as number | null,
  dateRange: [dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().endOf('month').format('YYYY-MM-DD')] as [string, string]
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

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'PENDING': '待签到',
    'PRESENT': '到课',
    'ABSENT': '缺课',
    'LEAVE': '请假',
    'TRIAL': '试课'
  }
  return labels[status] || status
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
    // 获取该学生的所有班级
    const classesRes = await studentApi.getClasses(studentForm.value.studentId)
    const classes = classesRes.data || []

    if (classes.length === 0) {
      studentReportData.value = []
      loading.value = false
      return
    }

    // 获取所有相关课节
    const allLessons: any[] = []
    for (const cls of classes) {
      const lessonsRes = await lessonApi.getByClass(cls.id)
      const lessons = lessonsRes.data || []
      allLessons.push(...lessons)
    }

    // 筛选日期范围内的课节，并提取该学生的签到记录
    const startDate = studentForm.value.dateRange[0]
    const endDate = studentForm.value.dateRange[1]

    const filteredLessons = allLessons.filter((lesson: any) => {
      return lesson.date >= startDate && lesson.date <= endDate
    })

    // 提取学生的签到记录
    const reportData: any[] = []
    for (const lesson of filteredLessons) {
      const attendance = lesson.attendances?.find((a: any) => a.student?.id === studentForm.value.studentId)
      if (attendance) {
        reportData.push({
          date: lesson.date,
          courseName: lesson.classInfo?.course?.name || '未知课程',
          teacherName: lesson.classInfo?.teacher?.name || '未知老师',
          status: attendance.status,
          remark: attendance.remark || '-'
        })
      }
    }

    // 按日期排序
    studentReportData.value = reportData.sort((a, b) => a.date.localeCompare(b.date))
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
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
    const startDate = teacherForm.value.dateRange[0]
    const endDate = teacherForm.value.dateRange[1]

    const res = await lessonApi.getByTeacherAndRange(teacherForm.value.teacherId, startDate, endDate)
    teacherReportData.value = (res.data || []).map((lesson: any) => ({
      date: lesson.date,
      courseName: lesson.classInfo?.course?.name || '未知课程',
      studentCount: lesson.attendances?.length || 0,
      presentCount: lesson.attendances?.filter((a: any) => a.status === 'PRESENT').length || 0,
      leaveCount: lesson.attendances?.filter((a: any) => a.status === 'LEAVE').length || 0,
      absentCount: lesson.attendances?.filter((a: any) => a.status === 'ABSENT').length || 0
    }))
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const exportStudentReport = () => {
  if (studentReportData.value.length === 0) {
    ElMessage.warning('没有数据可导出')
    return
  }
  ElMessage.info('导出功能开发中...')
}

const exportTeacherReport = () => {
  if (teacherReportData.value.length === 0) {
    ElMessage.warning('没有数据可导出')
    return
  }
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

.stat-item {
  margin-bottom: 20px;
}
</style>
