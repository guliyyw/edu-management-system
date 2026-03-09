<template>
  <div class="reports">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报表中心</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 学生上课统计 -->
        <el-tab-pane label="学生上课统计" name="student">
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

          <!-- 统计卡片 -->
          <el-row :gutter="20" v-if="studentStats.totalLessons > 0" class="stats-row">
            <el-col :span="6">
              <el-statistic :value="studentStats.totalLessons" title="总课时" />
            </el-col>
            <el-col :span="6">
              <el-statistic :value="studentStats.presentCount" title="到课次数" />
            </el-col>
            <el-col :span="6">
              <el-statistic :value="studentStats.leaveCount" title="请假次数" />
            </el-col>
            <el-col :span="6">
              <el-statistic :value="studentStats.totalAmount" title="总费用(元)" :precision="2" />
            </el-col>
          </el-row>

          <el-table :data="studentReportData" border v-loading="loading" height="calc(100vh - 340px)" style="width: 100%">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="startTime" label="开始时间" width="100" />
            <el-table-column prop="endTime" label="结束时间" width="100" />
            <el-table-column prop="subjectName" label="科目" />
            <el-table-column prop="teacherName" label="老师" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="unitPrice" label="单价(元)" width="100">
              <template #default="{ row }">
                {{ row.unitPrice.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="费用(元)" width="100">
              <template #default="{ row }">
                {{ row.amount.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          </el-table>

          <el-empty v-if="!loading && studentReportData.length === 0" description="暂无数据" />
        </el-tab-pane>

        <!-- 老师授课统计 -->
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

          <!-- 统计卡片 -->
          <el-row :gutter="20" v-if="teacherStats.totalLessons > 0" class="stats-row">
            <el-col :span="6">
              <el-statistic :value="teacherStats.totalLessons" title="总课时" />
            </el-col>
            <el-col :span="6">
              <el-statistic :value="teacherStats.totalStudents" title="总学生人次" />
            </el-col>
            <el-col :span="6">
              <el-statistic :value="teacherStats.totalHours" title="总授课时长(小时)" :precision="1" />
            </el-col>
            <el-col :span="6">
              <el-statistic :value="teacherStats.totalAmount" title="总授课金额(元)" :precision="2" />
            </el-col>
          </el-row>

          <el-table :data="teacherReportData" border v-loading="loading" height="calc(100vh - 340px)" style="width: 100%">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="startTime" label="开始时间" width="100" />
            <el-table-column prop="endTime" label="结束时间" width="100" />
            <el-table-column prop="className" label="班级名称" show-overflow-tooltip />
            <el-table-column prop="studentCount" label="学生数" width="100" />
            <el-table-column prop="presentCount" label="到课" width="80" />
            <el-table-column prop="leaveCount" label="请假" width="80" />
            <el-table-column prop="absentCount" label="缺课" width="80" />
            <el-table-column prop="duration" label="时长(小时)" width="100" />
            <el-table-column prop="lessonAmount" label="课时金额(元)" width="120">
              <template #default="{ row }">
                {{ row.lessonAmount.toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!loading && teacherReportData.length === 0" description="暂无数据" />
        </el-tab-pane>

        <!-- 科目统计 -->
        <el-tab-pane label="科目统计" name="subject">
          <el-form :inline="true" class="search-form">
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="subjectForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="querySubjectReport">查询</el-button>
              <el-button @click="exportSubjectReport">导出Excel</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="subjectReportData" border v-loading="loading" height="calc(100vh - 340px)" style="width: 100%">
            <el-table-column prop="subjectName" label="科目名称" show-overflow-tooltip />
            <el-table-column prop="teacherName" label="授课老师" />
            <el-table-column prop="lessonCount" label="上课次数" width="100" />
            <el-table-column prop="studentCount" label="学生人次" width="100" />
            <el-table-column prop="totalHours" label="总时长(小时)" width="120" />
            <el-table-column prop="totalAmount" label="总金额(元)" width="120">
              <template #default="{ row }">
                {{ row.totalAmount.toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!loading && subjectReportData.length === 0" description="暂无数据" />
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
const subjectReportData = ref([])

// 学生统计
const studentStats = ref({
  totalLessons: 0,
  presentCount: 0,
  leaveCount: 0,
  totalAmount: 0
})

// 老师统计
const teacherStats = ref({
  totalLessons: 0,
  totalStudents: 0,
  totalHours: 0,
  totalAmount: 0
})

const studentForm = ref({
  studentId: null as number | null,
  dateRange: [dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().endOf('month').format('YYYY-MM-DD')] as [string, string]
})

const teacherForm = ref({
  teacherId: null as number | null,
  dateRange: [dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().endOf('month').format('YYYY-MM-DD')] as [string, string]
})

const subjectForm = ref({
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
  return courseTypeLabels[type] || type
}

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
    const classesRes = await studentApi.getClasses(studentForm.value.studentId)
    const classes = classesRes.data || []

    if (classes.length === 0) {
      studentReportData.value = []
      studentStats.value = { totalLessons: 0, presentCount: 0, leaveCount: 0, totalAmount: 0 }
      loading.value = false
      return
    }

    const allLessons: any[] = []
    for (const cls of classes) {
      const lessonsRes = await lessonApi.getByClass(cls.id)
      const lessons = lessonsRes.data || []
      allLessons.push(...lessons.map((l: any) => ({ ...l, classInfo: cls })))
    }

    const startDate = studentForm.value.dateRange[0]
    const endDate = studentForm.value.dateRange[1]

    const filteredLessons = allLessons.filter((lesson: any) => {
      return lesson.date >= startDate && lesson.date <= endDate
    })

    const reportData: any[] = []
    let totalAmount = 0
    let presentCount = 0
    let leaveCount = 0

    for (const lesson of filteredLessons) {
      const attendance = lesson.attendances?.find((a: any) => a.student?.id === studentForm.value.studentId)
      if (attendance) {
        // 使用班级单价（优先使用班级自定义单价，否则使用课程单价）
        const unitPrice = lesson.classInfo?.unitPrice || lesson.classInfo?.course?.unitPrice || 0
        const isTrial = attendance.isTrial || false
        // 试课价格：优先使用班级试课价，否则使用课程试课价，最后使用班级单价
        const trialPrice = lesson.classInfo?.trialPrice || lesson.classInfo?.course?.trialPrice || unitPrice
        const amount = isTrial ? trialPrice : unitPrice

        if (attendance.status === 'PRESENT') {
          presentCount++
          totalAmount += amount
        } else if (attendance.status === 'LEAVE') {
          leaveCount++
        }

        reportData.push({
          date: lesson.date,
          startTime: lesson.startTime,
          endTime: lesson.endTime,
          subjectName: getCourseTypeLabel(lesson.classInfo?.course?.type) || '未知科目',
          teacherName: lesson.classInfo?.teacher?.name || '未知老师',
          status: attendance.status,
          unitPrice: unitPrice,
          amount: amount,
          remark: attendance.remark || '-'
        })
      }
    }

    studentReportData.value = reportData.sort((a, b) => a.date.localeCompare(b.date))
    studentStats.value = {
      totalLessons: reportData.length,
      presentCount,
      leaveCount,
      totalAmount
    }
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
    const lessons = res.data || []

    let totalStudents = 0
    let totalHours = 0
    let totalAmount = 0

    teacherReportData.value = lessons.map((lesson: any) => {
      const studentCount = lesson.attendances?.length || 0
      const presentCount = lesson.attendances?.filter((a: any) => a.status === 'PRESENT').length || 0
      const leaveCount = lesson.attendances?.filter((a: any) => a.status === 'LEAVE').length || 0
      const absentCount = lesson.attendances?.filter((a: any) => a.status === 'ABSENT').length || 0

      const duration = calculateDuration(lesson.startTime, lesson.endTime)
      // 使用老师课时费计算老师授课金额（按到课人数计算，每人一节课时费）
      const teacherFee = lesson.classInfo?.teacherFee || 0
      const lessonAmount = presentCount > 0 ? teacherFee * presentCount : 0

      totalStudents += studentCount
      totalHours += duration
      totalAmount += lessonAmount

      return {
        date: lesson.date,
        startTime: lesson.startTime,
        endTime: lesson.endTime,
        className: lesson.classInfo?.className || '未知班级',
        studentCount,
        presentCount,
        leaveCount,
        absentCount,
        duration: duration.toFixed(1),
        lessonAmount
      }
    })

    teacherStats.value = {
      totalLessons: lessons.length,
      totalStudents,
      totalHours: parseFloat(totalHours.toFixed(1)),
      totalAmount
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const querySubjectReport = async () => {
  loading.value = true
  try {
    const startDate = subjectForm.value.dateRange[0]
    const endDate = subjectForm.value.dateRange[1]

    // 获取所有课节
    const lessonsRes = await lessonApi.getAll()
    const lessons = (lessonsRes.data || []).filter((lesson: any) => {
      return lesson.date >= startDate && lesson.date <= endDate
    })

    // 按科目和老师分组统计
    const subjectMap = new Map()

    for (const lesson of lessons) {
      const subjectName = getCourseTypeLabel(lesson.classInfo?.course?.type) || '未知科目'
      const teacherName = lesson.classInfo?.teacher?.name || '未知老师'
      const key = `${subjectName}-${teacherName}`

      const presentCount = lesson.attendances?.filter((a: any) => a.status === 'PRESENT').length || 0
      const studentCount = lesson.attendances?.length || 0
      const duration = calculateDuration(lesson.startTime, lesson.endTime)
      // 使用班级单价（优先使用班级自定义单价，否则使用课程单价）
      const unitPrice = lesson.classInfo?.unitPrice || lesson.classInfo?.course?.unitPrice || 0
      const lessonAmount = presentCount * unitPrice

      if (!subjectMap.has(key)) {
        subjectMap.set(key, {
          subjectName,
          teacherName,
          lessonCount: 0,
          studentCount: 0,
          totalHours: 0,
          totalAmount: 0
        })
      }

      const stat = subjectMap.get(key)
      stat.lessonCount++
      stat.studentCount += studentCount
      stat.totalHours += duration
      stat.totalAmount += lessonAmount
    }

    subjectReportData.value = Array.from(subjectMap.values()).map(item => ({
      ...item,
      totalHours: parseFloat(item.totalHours.toFixed(1))
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

const exportSubjectReport = () => {
  if (subjectReportData.value.length === 0) {
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

.stats-row {
  margin-bottom: 20px;
}

:deep(.el-statistic__content) {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}
</style>
