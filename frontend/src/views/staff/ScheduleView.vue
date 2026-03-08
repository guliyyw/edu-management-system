<template>
  <div class="schedule">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>排课中心</span>
          <div class="header-actions">
            <el-select
              v-model="selectedClass"
              placeholder="筛选班级（可选）"
              style="width: 250px; margin-right: 10px;"
              @change="handleClassChange"
              clearable
            >
              <el-option
                v-for="cls in classes"
                :key="cls.id"
                :label="`${cls.className || cls.course?.name} - ${cls.teacher?.name || '未知老师'}`"
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

      <el-calendar v-model="calendarDate">
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
              <el-tag size="small" :type="getLessonType(lesson)">
                {{ lesson.classInfo?.course?.name || '课程' }}
              </el-tag>
              <div class="lesson-time">{{ lesson.startTime }} - {{ lesson.endTime }}</div>
              <div class="lesson-class">{{ lesson.classInfo?.className || '' }}</div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { classApi } from '@/api/class'
import { lessonApi } from '@/api/lesson'
import dayjs from 'dayjs'

const classes = ref([])
const selectedClass = ref<number | null>(null)
const dateRange = ref<[string, string] | null>(null)
const calendarDate = ref(new Date())
const lessons = ref([])
const conflictInfo = ref('')
const editDialogVisible = ref(false)
const addDialogVisible = ref(false)
const editFormRef = ref()
const addFormRef = ref()

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

const addFormRules = {
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const selectedClassInfo = computed(() => {
  if (!selectedClass.value) return null
  return classes.value.find((c: any) => c.id === selectedClass.value)
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

const fetchClasses = async () => {
  try {
    const res = await classApi.getAll()
    classes.value = res.data || []
  } catch (error) {
    console.error('获取班级失败:', error)
  }
}

const fetchLessons = async () => {
  try {
    if (selectedClass.value) {
      const res = await lessonApi.getByClass(selectedClass.value)
      lessons.value = res.data || []
    } else {
      const res = await lessonApi.getAll()
      lessons.value = res.data || []
    }
  } catch (error) {
    console.error('获取课节失败:', error)
  }
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
</style>
