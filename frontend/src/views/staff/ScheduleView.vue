<template>
  <div class="schedule">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>排课中心</span>
          <div class="header-actions">
            <el-select
              v-model="selectedCampus"
              placeholder="筛选校区"
              style="width: 150px; margin-right: 10px;"
              @change="handleCampusChange"
              clearable
            >
              <el-option
                v-for="campus in campuses"
                :key="campus.id"
                :label="campus.name"
                :value="campus.id"
              />
            </el-select>
            <el-select
              v-model="selectedTeacher"
              placeholder="筛选老师"
              style="width: 150px; margin-right: 10px;"
              @change="handleTeacherChange"
              clearable
            >
              <el-option
                v-for="teacher in teachers"
                :key="teacher.id"
                :label="teacher.name"
                :value="teacher.id"
              />
            </el-select>
            <el-select
              v-model="selectedClass"
              placeholder="筛选班级（可选）"
              style="width: 250px; margin-right: 10px;"
              @change="handleClassChange"
              clearable
            >
              <el-option
                v-for="cls in filteredClasses"
                :key="cls.id"
                :label="cls.className"
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
              value-format="YYYY-MM-DD"
            />
            <el-button type="primary" @click="generateLessons" :disabled="!selectedClass">批量排课</el-button>
            <el-button type="success" @click="showScheduleDialog" style="margin-left: 10px;">生成课表</el-button>
          </div>
        </div>
      </template>

      <el-alert
        v-if="conflictInfo"
        :title="conflictInfo"
        type="warning"
        show-icon
        closable
        @close="conflictInfo = ''"
        style="margin-bottom: 20px;"
      />

      <div v-if="selectedClassInfo" class="class-info">
        <el-descriptions :column="4" border size="small">
          <el-descriptions-item label="班级名称">{{ selectedClassInfo.className }}</el-descriptions-item>
          <el-descriptions-item label="课程">{{ selectedClassInfo.course?.name }}</el-descriptions-item>
          <el-descriptions-item label="老师">{{ selectedClassInfo.teacher?.name }}</el-descriptions-item>
          <el-descriptions-item label="校区">{{ selectedClassInfo.campus?.name }}</el-descriptions-item>
          <el-descriptions-item label="教室">{{ selectedClassInfo.classroom }}</el-descriptions-item>
          <el-descriptions-item label="学生数">{{ selectedClassInfo.students?.length || 0 }} 人</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-calendar v-model="calendarDate" style="height: calc(100vh - 300px);">
        <template #date-cell="{ data }">
          <div class="calendar-cell" @click="handleDateClick(data.day)">
            <p :class="data.isSelected ? 'is-selected' : ''">
              {{ data.day.split('-').slice(1).join('-') }}
            </p>
            <div
              v-for="lesson in getLessonsByDate(data.day)"
              :key="lesson.id"
              class="lesson-tag"
              @click.stop="handleLessonClick(lesson)"
            >
              <div class="lesson-header">
                <el-tag size="small" :type="getLessonType(lesson)">
                  {{ getCourseTypeLabel(lesson.classInfo?.course?.type) }}
                </el-tag>
                <span class="lesson-grade">{{ getGradeLabel(lesson.classInfo?.gradeLevel) }}</span>
                <span class="lesson-teacher">{{ lesson.classInfo?.teacher?.name }}</span>
              </div>
              <div class="lesson-time">{{ lesson.startTime }} - {{ lesson.endTime }}</div>
            </div>
            <!-- 添加课程按钮 -->
            <div v-if="selectedClass" class="add-lesson-hint" @click.stop="handleDateClick(data.day)">
              <el-icon><Plus /></el-icon>
            </div>
          </div>
        </template>
      </el-calendar>
    </el-card>

    <!-- 编辑课节对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑课节" width="500px">
      <el-form :model="editForm" ref="editFormRef" label-width="100px">
        <el-form-item label="班级">
          <el-input :value="`${editForm.classInfo?.className || editForm.classInfo?.course?.name} - ${editForm.classInfo?.teacher?.name}`" disabled />
        </el-form-item>
        <el-form-item label="日期" prop="date">
          <el-date-picker
            v-model="editForm.date"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker
            v-model="editForm.startTime"
            placeholder="选择开始时间"
            format="HH:mm"
            value-format="HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker
            v-model="editForm.endTime"
            placeholder="选择结束时间"
            format="HH:mm"
            value-format="HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="教室" prop="classroom">
          <el-input v-model="editForm.classroom" placeholder="请输入教室" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="editForm.status" placeholder="选择状态" style="width: 100%">
            <el-option label="已排课" value="SCHEDULED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="deleteLesson">删除</el-button>
        <el-button type="primary" @click="saveLesson">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加课节对话框 -->
    <el-dialog v-model="addDialogVisible" title="添加课节" width="500px">
      <el-form :model="addForm" ref="addFormRef" label-width="100px" :rules="addFormRules">
        <el-form-item label="班级" prop="classId">
          <el-select v-model="addForm.classId" placeholder="选择班级" style="width: 100%" @change="handleAddClassChange">
            <el-option
              v-for="cls in classes"
              :key="cls.id"
              :label="`${cls.className || cls.course?.name} - ${cls.teacher?.name || '未知老师'}`"
              :value="cls.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="date">
          <el-date-picker
            v-model="addForm.date"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            disabled
          />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker
            v-model="addForm.startTime"
            placeholder="选择开始时间"
            format="HH:mm"
            value-format="HH:mm:ss"
            style="width: 100%"
            @change="handleAddStartTimeChange"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker
            v-model="addForm.endTime"
            placeholder="选择结束时间"
            format="HH:mm"
            value-format="HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="教室" prop="classroom">
          <el-input v-model="addForm.classroom" placeholder="请输入教室" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createLesson">保存</el-button>
      </template>
    </el-dialog>

    <!-- 生成课表对话框 -->
    <el-dialog v-model="scheduleDialogVisible" title="生成课表" width="700px">
      <el-form :model="scheduleForm" ref="scheduleFormRef" label-width="100px">
        <el-form-item label="选择班级" prop="classIds">
          <el-select
            v-model="scheduleForm.classIds"
            multiple
            placeholder="请选择要生成课表的班级"
            style="width: 100%;"
          >
            <el-option
              v-for="cls in filteredClasses"
              :key="cls.id"
              :label="cls.className"
              :value="cls.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="scheduleForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="generateSchedule" :loading="generatingSchedule">生成课表</el-button>
      </template>
    </el-dialog>

    <!-- 课表预览对话框 -->
    <el-dialog v-model="schedulePreviewVisible" title="课表预览" width="95%" top="5vh">
      <div class="schedule-preview">
        <!-- 日历表格形式课表 -->
        <div class="calendar-schedule">
          <table class="schedule-table" border="1">
            <thead>
              <tr>
                <th class="time-header">时间</th>
                <th v-for="day in weekDays" :key="day.key" class="day-header">
                  {{ day.label }}
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="timeSlot in timeSlots" :key="timeSlot">
                <td class="time-cell">{{ timeSlot }}</td>
                <td v-for="day in weekDays" :key="day.key" class="day-cell">
                  <div v-for="lesson in getScheduleLessons(day.key, timeSlot)" :key="lesson.id" class="schedule-lesson">
                    <div class="lesson-course">{{ lesson.course }}</div>
                    <div class="lesson-class">{{ lesson.className }}</div>
                    <div class="lesson-teacher">{{ lesson.teacher }}</div>
                    <div class="lesson-room">{{ lesson.classroom }}</div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <!-- 列表形式课表 -->
        <el-divider content-position="left">列表视图</el-divider>
        <el-table :data="scheduleData" border height="300">
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="weekDay" label="星期" width="80" />
          <el-table-column prop="time" label="时间" width="120" />
          <el-table-column prop="className" label="班级" width="150" />
          <el-table-column prop="course" label="科目" width="100" />
          <el-table-column prop="teacher" label="老师" width="100" />
          <el-table-column prop="campus" label="校区" width="100" />
          <el-table-column prop="classroom" label="教室" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="schedulePreviewVisible = false">关闭</el-button>
        <el-button type="primary" @click="printSchedule">打印课表</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { classApi } from '@/api/class'
import { lessonApi } from '@/api/lesson'
import { campusApi } from '@/api/campus'
import { teacherApi } from '@/api/teacher'
import dayjs from 'dayjs'

const classes = ref([])
const campuses = ref([])
const teachers = ref([])
const selectedCampus = ref<number | null>(null)
const selectedTeacher = ref<number | null>(null)
const selectedClass = ref<number | null>(null)
const dateRange = ref<[string, string] | null>(null)
const calendarDate = ref(new Date())
const lessons = ref([])
const conflictInfo = ref('')
const editDialogVisible = ref(false)
const addDialogVisible = ref(false)
const scheduleDialogVisible = ref(false)
const schedulePreviewVisible = ref(false)
const generatingSchedule = ref(false)
const editFormRef = ref()
const addFormRef = ref()
const scheduleFormRef = ref()
const scheduleData = ref<any[]>([])

const editForm = ref({
  id: null as number | null,
  classInfo: null as any,
  date: '',
  startTime: '',
  endTime: '',
  classroom: '',
  status: 'SCHEDULED'
})

const addForm = ref({
  classId: null as number | null,
  date: '',
  startTime: '',
  endTime: '',
  classroom: ''
})

const scheduleForm = ref({
  classIds: [] as number[],
  dateRange: null as [string, string] | null
})

// 星期定义
const weekDays = [
  { key: 1, label: '周一' },
  { key: 2, label: '周二' },
  { key: 3, label: '周三' },
  { key: 4, label: '周四' },
  { key: 5, label: '周五' },
  { key: 6, label: '周六' },
  { key: 7, label: '周日' }
]

// 时间段定义（可以根据实际需要调整）
const timeSlots = [
  '08:00-09:30',
  '09:45-11:15',
  '13:30-15:00',
  '15:15-16:45',
  '17:00-18:30',
  '18:45-20:15'
]

// 获取指定星期和时间段的课程
const getScheduleLessons = (weekDay: number, timeSlot: string) => {
  const [startTime] = timeSlot.split('-')
  return scheduleData.value.filter((lesson: any) => {
    return lesson.weekDay === weekDay && lesson.startTime <= startTime && lesson.endTime > startTime
  })
}

const addFormRules = {
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const selectedClassInfo = computed(() => {
  if (!selectedClass.value) return null
  return classes.value.find((c: any) => c.id === selectedClass.value)
})

// 根据校区和老师筛选班级
const filteredClasses = computed(() => {
  let result = classes.value

  // 按校区筛选
  if (selectedCampus.value) {
    result = result.filter((c: any) => c.campus?.id === selectedCampus.value)
  }

  // 按老师筛选
  if (selectedTeacher.value) {
    result = result.filter((c: any) => c.teacher?.id === selectedTeacher.value)
  }

  return result
})

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
  'GRADE_12': '高三'
}

const getGradeLabel = (grade: string) => {
  return gradeLabels[grade] || grade || ''
}

const fetchClasses = async () => {
  try {
    const res = await classApi.getAll()
    classes.value = res.data || []
  } catch (error) {
    console.error('获取班级失败:', error)
  }
}

const fetchCampuses = async () => {
  try {
    const res = await campusApi.getActive()
    campuses.value = res.data || []
  } catch (error) {
    console.error('获取校区失败:', error)
  }
}

const fetchTeachers = async () => {
  try {
    const res = await teacherApi.getActive()
    teachers.value = res.data || []
  } catch (error) {
    console.error('获取老师失败:', error)
  }
}

const fetchLessons = async () => {
  try {
    if (selectedClass.value) {
      const res = await lessonApi.getByClass(selectedClass.value)
      lessons.value = res.data || []
    } else if (selectedCampus.value) {
      // 获取该校区所有班级的课节
      const campusClassIds = filteredClasses.value.map((c: any) => c.id)
      const allLessons = await lessonApi.getAll()
      lessons.value = allLessons.data?.filter((l: any) => campusClassIds.includes(l.classInfo?.id)) || []
    } else {
      const res = await lessonApi.getAll()
      lessons.value = res.data || []
    }
  } catch (error) {
    console.error('获取课节失败:', error)
  }
}

const handleCampusChange = async () => {
  // 切换校区时，清除已选择的班级
  selectedClass.value = null
  conflictInfo.value = ''
  await fetchLessons()
}

const handleTeacherChange = async () => {
  // 切换老师时，清除已选择的班级
  selectedClass.value = null
  conflictInfo.value = ''
  await fetchLessons()
}

const handleClassChange = async () => {
  conflictInfo.value = ''
  await fetchLessons()
}

// 点击日期添加课节
const handleDateClick = (date: string) => {
  if (!selectedClass.value) {
    ElMessage.warning('请先选择一个班级，或点击课程进行编辑')
    return
  }

  const cls = classes.value.find((c: any) => c.id === selectedClass.value)
  if (!cls) return

  addForm.value = {
    classId: selectedClass.value,
    date: date,
    startTime: cls.defaultStartTime || '',
    endTime: cls.defaultEndTime || '',
    classroom: cls.classroom || ''
  }
  addDialogVisible.value = true
}

// 添加课节时选择班级变化
const handleAddClassChange = (classId: number) => {
  const cls = classes.value.find((c: any) => c.id === classId)
  if (cls) {
    addForm.value.classroom = cls.classroom || ''
    if (cls.defaultStartTime && !addForm.value.startTime) {
      addForm.value.startTime = cls.defaultStartTime
    }
    if (cls.defaultEndTime && !addForm.value.endTime) {
      addForm.value.endTime = cls.defaultEndTime
    }
  }
}

// 添加课节开始时间变化
const handleAddStartTimeChange = (val: string | null) => {
  if (val && !addForm.value.endTime) {
    const time = dayjs(`2024-01-01 ${val}`)
    const endTime = time.add(2, 'hour').format('HH:mm:ss')
    addForm.value.endTime = endTime
  }
}

// 创建课节
const createLesson = async () => {
  const valid = await addFormRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    await lessonApi.create({
      classInfo: { id: addForm.value.classId },
      date: addForm.value.date,
      startTime: addForm.value.startTime,
      endTime: addForm.value.endTime,
      classroom: addForm.value.classroom
    })
    ElMessage.success('添加课节成功')
    addDialogVisible.value = false
    fetchLessons()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '添加失败')
  }
}

// 显示生成课表对话框
const showScheduleDialog = () => {
  scheduleForm.value = {
    classIds: selectedClass.value ? [selectedClass.value] : [],
    dateRange: dateRange.value
  }
  scheduleDialogVisible.value = true
}

// 生成课表
const generateSchedule = async () => {
  const valid = await scheduleFormRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!scheduleForm.value.classIds.length) {
    ElMessage.warning('请至少选择一个班级')
    return
  }

  if (!scheduleForm.value.dateRange || !scheduleForm.value.dateRange[0] || !scheduleForm.value.dateRange[1]) {
    ElMessage.warning('请选择日期范围')
    return
  }

  generatingSchedule.value = true
  try {
    // 获取所有选中班级的课节
    const allLessons: any[] = []
    for (const classId of scheduleForm.value.classIds) {
      const res = await lessonApi.getByClass(classId)
      if (res.data) {
        allLessons.push(...res.data)
      }
    }

    // 筛选日期范围内的课节
    const startDate = scheduleForm.value.dateRange[0]
    const endDate = scheduleForm.value.dateRange[1]
    const filteredLessons = allLessons.filter((lesson: any) => {
      return lesson.date >= startDate && lesson.date <= endDate
    })

    // 转换为课表数据格式
    scheduleData.value = filteredLessons.map((lesson: any) => {
      const date = new Date(lesson.date)
      const weekDay = date.getDay() || 7 // 0是周日，转为7
      return {
        id: lesson.id,
        date: lesson.date,
        weekDay: weekDay,
        startTime: lesson.startTime?.substring(0, 5),
        endTime: lesson.endTime?.substring(0, 5),
        time: `${lesson.startTime?.substring(0, 5)} - ${lesson.endTime?.substring(0, 5)}`,
        className: lesson.classInfo?.className,
        course: getCourseTypeLabel(lesson.classInfo?.course?.type),
        teacher: lesson.classInfo?.teacher?.name,
        campus: lesson.classInfo?.campus?.name,
        classroom: lesson.classroom
      }
    }).sort((a: any, b: any) => {
      // 按日期和时间排序
      if (a.date !== b.date) {
        return a.date.localeCompare(b.date)
      }
      return a.time.localeCompare(b.time)
    })

    scheduleDialogVisible.value = false
    schedulePreviewVisible.value = true
    ElMessage.success(`课表生成完成，共 ${scheduleData.value.length} 节课`)
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '生成课表失败')
  } finally {
    generatingSchedule.value = false
  }
}

// 打印课表
const printSchedule = () => {
  const printWindow = window.open('', '_blank')
  if (!printWindow) return

  const tableHtml = `
    <html>
      <head>
        <title>课表</title>
        <style>
          table { border-collapse: collapse; width: 100%; }
          th, td { border: 1px solid #333; padding: 8px; text-align: left; }
          th { background-color: #f0f0f0; }
          h2 { text-align: center; }
        </style>
      </head>
      <body>
        <h2>课表</h2>
        <table>
          <thead>
            <tr>
              <th>日期</th>
              <th>时间</th>
              <th>班级</th>
              <th>科目</th>
              <th>老师</th>
              <th>校区</th>
              <th>教室</th>
            </tr>
          </thead>
          <tbody>
            ${scheduleData.value.map(row => `
              <tr>
                <td>${row.date}</td>
                <td>${row.time}</td>
                <td>${row.className}</td>
                <td>${row.course}</td>
                <td>${row.teacher}</td>
                <td>${row.campus}</td>
                <td>${row.classroom}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </body>
    </html>
  `

  printWindow.document.write(tableHtml)
  printWindow.document.close()
  printWindow.print()
}

const handleLessonClick = (lesson: any) => {
  editForm.value = {
    id: lesson.id,
    classInfo: lesson.classInfo,
    date: lesson.date,
    startTime: lesson.startTime,
    endTime: lesson.endTime,
    classroom: lesson.classroom,
    status: lesson.status
  }
  editDialogVisible.value = true
}

const saveLesson = async () => {
  if (!editForm.value.id) return

  try {
    await lessonApi.update(editForm.value.id, {
      date: editForm.value.date,
      startTime: editForm.value.startTime,
      endTime: editForm.value.endTime,
      classroom: editForm.value.classroom,
      status: editForm.value.status
    })
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    fetchLessons()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '更新失败')
  }
}

const deleteLesson = async () => {
  if (!editForm.value.id) return

  try {
    await ElMessageBox.confirm('确定要删除此课节吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await lessonApi.delete(editForm.value.id)
    ElMessage.success('删除成功')
    editDialogVisible.value = false
    fetchLessons()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const generateLessons = async () => {
  if (!selectedClass.value || !dateRange.value || !dateRange.value[0] || !dateRange.value[1]) {
    ElMessage.warning('请选择班级和日期范围')
    return
  }

  try {
    const startDate = dateRange.value[0]
    const endDate = dateRange.value[1]

    const res = await lessonApi.generate(selectedClass.value, startDate, endDate)
    ElMessage.success(`排课成功，共生成 ${res.data?.length || 0} 节课`)
    conflictInfo.value = ''
    fetchLessons()
  } catch (error: any) {
    conflictInfo.value = error.response?.data?.message || '排课失败'
    ElMessage.error('排课失败')
  }
}

onMounted(() => {
  fetchClasses()
  fetchCampuses()
  fetchTeachers()
  fetchLessons()
  dateRange.value = [
    dayjs().format('YYYY-MM-DD'),
    dayjs().add(1, 'month').format('YYYY-MM-DD')
  ]
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

.class-info {
  margin-bottom: 20px;
}

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

.lesson-time {
  font-size: 11px;
  color: #666;
  margin-top: 2px;
}

.lesson-class {
  font-size: 10px;
  color: #999;
  margin-top: 1px;
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

.lesson-teacher {
  color: #409EFF;
  font-weight: 500;
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.lesson-grade {
  color: #67C23A;
  background-color: #f0f9eb;
  padding: 0 4px;
  border-radius: 2px;
  font-size: 10px;
  flex-shrink: 0;
}

.add-lesson-hint {
  display: none;
  position: absolute;
  bottom: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  background-color: #409eff;
  border-radius: 50%;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
}

.calendar-cell:hover .add-lesson-hint {
  display: flex;
}

:deep(.el-calendar-day) {
  min-height: 100px;
  height: auto;
}

/* 课表日历表格样式 */
.calendar-schedule {
  margin-bottom: 20px;
  overflow-x: auto;
}

.schedule-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.schedule-table th,
.schedule-table td {
  border: 1px solid #dcdfe6;
  padding: 4px;
  text-align: center;
  vertical-align: top;
}

.schedule-table .time-header {
  width: 100px;
  background-color: #f5f7fa;
  font-weight: bold;
}

.schedule-table .day-header {
  background-color: #f5f7fa;
  font-weight: bold;
}

.schedule-table .time-cell {
  background-color: #f5f7fa;
  font-weight: bold;
  font-size: 12px;
  vertical-align: middle;
}

.schedule-table .day-cell {
  min-height: 80px;
  height: 80px;
  overflow-y: auto;
}

.schedule-lesson {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
  padding: 4px;
  margin-bottom: 4px;
  font-size: 11px;
}

.schedule-lesson .lesson-course {
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 2px;
}

.schedule-lesson .lesson-class {
  color: #333;
  margin-bottom: 1px;
}

.schedule-lesson .lesson-teacher {
  color: #666;
  margin-bottom: 1px;
}

.schedule-lesson .lesson-room {
  color: #999;
  font-size: 10px;
}
</style>
