<template>
  <div class="classes">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>班级管理</span>
          <div class="header-actions">
            <el-select
              v-model="filterCampus"
              placeholder="筛选校区"
              size="small"
              style="width: 150px; margin-right: 10px;"
              @change="applyFilter"
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
              v-model="filterTeacher"
              placeholder="筛选老师"
              size="small"
              style="width: 150px; margin-right: 10px;"
              @change="applyFilter"
              clearable
            >
              <el-option
                v-for="teacher in teachers"
                :key="teacher.id"
                :label="teacher.name"
                :value="teacher.id"
              />
            </el-select>
            <el-button type="primary" size="small" @click="showAddDialog">添加班级</el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredClasses" border v-loading="loading" row-key="id" height="calc(100vh - 240px)" style="width: 100%">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="student-list">
              <h4>班级学生列表 ({{ row.students?.length || 0 }}人)</h4>
              <el-table :data="row.students" border size="small" v-if="row.students?.length > 0">
                <el-table-column prop="student.name" label="学生姓名" width="120" />
                <el-table-column prop="student.gradeLevel" label="年级" width="100">
                  <template #default="{ row: studentRow }">
                    {{ getGradeLabel(studentRow.student.gradeLevel) }}
                  </template>
                </el-table-column>
                <el-table-column prop="student.parentPhone" label="家长电话" width="150" />
                <el-table-column label="是否试课" width="100">
                  <template #default="{ row: studentRow }">
                    <el-tag v-if="studentRow.isTrial" type="warning">试课</el-tag>
                    <span v-else>正式</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                  <template #default="{ row: studentRow }">
                    <el-button v-if="studentRow.isTrial" type="success" size="small" @click="convertTrial(row, studentRow)">转正</el-button>
                    <el-button type="danger" size="small" @click="removeStudent(row, studentRow)">移除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-else description="暂无学生" />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级名称" width="150" />
        <el-table-column prop="gradeLevel" label="年级" width="100">
          <template #default="{ row }">
            {{ getGradeLabel(row.gradeLevel) }}
          </template>
        </el-table-column>
        <el-table-column prop="course.type" label="课程类型" width="120">
          <template #default="{ row }">
            {{ getCourseTypeLabel(row.course?.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="teacher.name" label="老师" />
        <el-table-column prop="campus.name" label="校区" />
        <el-table-column prop="teacherFee" label="老师课时费(元/人)" width="140">
          <template #default="{ row }">
            {{ row.teacherFee || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="默认时间" width="150">
          <template #default="{ row }">
            <span v-if="row.defaultDayOfWeek">
              周{{ ['一', '二', '三', '四', '五', '六', '日'][row.defaultDayOfWeek - 1] }}
              {{ row.defaultStartTime }} - {{ row.defaultEndTime }}
            </span>
            <span v-else class="text-gray">不固定</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="学生数" width="100">
          <template #default="{ row }">
            {{ row.students?.length || 0 }} 人
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="manageStudents(row)">管理学生</el-button>
            <el-button type="warning" size="small" @click="editClass(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteClass(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑班级' : '添加班级'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="form.className" placeholder="如：六年级英语A班">
            <template #append>
              <el-button @click="generateClassName">自动生成</el-button>
            </template>
          </el-input>
          <div class="form-tip">根据年级、课程、老师、校区和星期自动生成</div>
        </el-form-item>
        <el-form-item label="年级" prop="gradeLevel">
          <el-select v-model="form.gradeLevel" placeholder="选择年级" style="width: 100%;" @change="autoGenerateClassName">
            <el-option v-for="grade in gradeOptions" :key="grade.value" :label="grade.label" :value="grade.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程类型" prop="courseId">
          <el-select v-model="form.courseId" placeholder="选择课程类型" style="width: 100%;" @change="autoGenerateClassName">
            <el-option v-for="course in courses" :key="course.id" :label="getCourseTypeLabel(course.type)" :value="course.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="老师" prop="teacherId">
          <el-select v-model="form.teacherId" placeholder="选择老师" style="width: 100%;" @change="autoGenerateClassName">
            <el-option v-for="teacher in teachers" :key="teacher.id" :label="teacher.name" :value="teacher.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="校区" prop="campusId">
          <el-select v-model="form.campusId" placeholder="选择校区" style="width: 100%;" @change="autoGenerateClassName">
            <el-option v-for="campus in campuses" :key="campus.id" :label="campus.name" :value="campus.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教室">
          <el-input v-model="form.classroom" placeholder="请输入教室" />
        </el-form-item>
        <el-form-item label="老师课时费(元/人)">
          <el-input-number v-model="form.teacherFee" :min="0" :precision="2" placeholder="请输入老师课时费" style="width: 100%;" />
          <div class="form-tip">按到课学生人数计算，每人每节课的课时费</div>
        </el-form-item>
        <el-divider>默认上课时间（可选）</el-divider>
        <el-form-item label="星期">
          <el-select v-model="form.defaultDayOfWeek" placeholder="选择星期（可选）" style="width: 100%;" clearable @change="autoGenerateClassName">
            <el-option label="周一" :value="1" />
            <el-option label="周二" :value="2" />
            <el-option label="周三" :value="3" />
            <el-option label="周四" :value="4" />
            <el-option label="周五" :value="5" />
            <el-option label="周六" :value="6" />
            <el-option label="周日" :value="7" />
          </el-select>
          <div class="form-tip">不选表示上课时间灵活安排</div>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-picker
            v-model="form.defaultStartTime"
            format="HH:mm"
            style="width: 100%;"
            clearable
            placeholder="选择默认开始时间"
            @change="handleStartTimeChange"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-picker
            v-model="form.defaultEndTime"
            format="HH:mm"
            style="width: 100%;"
            clearable
            placeholder="选择默认结束时间"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveClass">保存</el-button>
      </template>
    </el-dialog>

    <!-- 学生管理对话框 -->
    <el-dialog v-model="studentDialogVisible" title="班级学生管理" width="900px">
      <div class="student-manage">
        <div class="class-info-bar">
          <el-tag>班级：{{ currentClassInfo?.className }}</el-tag>
          <el-tag type="info">年级：{{ getGradeLabel(currentClassInfo?.gradeLevel) }}</el-tag>
          <el-tag type="success">课程：{{ currentClassInfo?.course?.name }}</el-tag>
        </div>

        <el-tabs v-model="activeTab">
          <el-tab-pane label="添加已有学生" name="existing">
            <div class="student-table-section">
              <div class="table-header">
                <el-select
                  v-model="studentGradeFilter"
                  placeholder="选择年级筛选"
                  style="width: 150px; margin-right: 10px;"
                  size="small"
                  clearable
                >
                  <el-option label="全部年级" value="" />
                  <el-option
                    v-for="grade in gradeOptions"
                    :key="grade.value"
                    :label="grade.label"
                    :value="grade.value"
                  />
                </el-select>
                <span style="color: #909399; font-size: 14px;">
                  共 {{ filteredAvailableStudents.length }} 名学生
                  <span v-if="studentGradeFilter" style="color: #409EFF;">（已筛选）</span>
                </span>
                <el-checkbox v-model="isTrial" style="margin-left: 20px;">试课</el-checkbox>
                <el-button type="primary" @click="addStudents" :disabled="selectedStudents.length === 0" style="margin-left: 20px;" size="small">
                  批量添加 ({{ selectedStudents.length }}人)
                </el-button>
              </div>
              <el-table
                :data="sortedAvailableStudents"
                border
                height="400"
                @selection-change="handleSelectionChange"
                ref="studentTableRef"
              >
                <el-table-column type="selection" width="55" />
                <el-table-column prop="name" label="学生姓名" width="120" />
                <el-table-column prop="gradeLevel" label="年级" width="100" sortable>
                  <template #default="{ row }">
                    <el-tag :type="row.gradeLevel === currentClassInfo?.gradeLevel ? 'success' : 'info'">
                      {{ getGradeLabel(row.gradeLevel) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="parentName" label="家长姓名" width="120" />
                <el-table-column prop="parentPhone" label="家长电话" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          <el-tab-pane label="添加新学生" name="new">
            <div class="add-new-student">
              <el-form :model="newStudentForm" :rules="newStudentRules" ref="newStudentFormRef" label-width="100px">
                <el-form-item label="学生姓名" prop="name">
                  <el-input v-model="newStudentForm.name" placeholder="请输入学生姓名" />
                </el-form-item>
                <el-form-item label="年级">
                  <el-select v-model="newStudentForm.gradeLevel" placeholder="选择年级" style="width: 100%;">
                    <el-option v-for="grade in gradeOptions" :key="grade.value" :label="grade.label" :value="grade.value" />
                  </el-select>
                </el-form-item>
                <el-form-item label="家长姓名">
                  <el-input v-model="newStudentForm.parentName" placeholder="请输入家长姓名（选填）" />
                </el-form-item>
                <el-form-item label="家长电话">
                  <el-input v-model="newStudentForm.parentPhone" placeholder="请输入家长电话（选填）" />
                </el-form-item>
                <el-form-item>
                  <el-checkbox v-model="newStudentIsTrial">试课</el-checkbox>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="addNewStudent">添加并加入班级</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>

        <el-divider />

        <h4>当前班级学生 ({{ currentClassStudents.length }}人)</h4>
        <el-table :data="currentClassStudents" border style="margin-top: 10px;">
          <el-table-column prop="student.name" label="学生姓名" />
          <el-table-column prop="student.gradeLevel" label="年级" width="100">
            <template #default="{ row }">
              {{ getGradeLabel(row.student.gradeLevel) }}
            </template>
          </el-table-column>
          <el-table-column prop="student.parentPhone" label="家长电话" />
          <el-table-column label="是否试课" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.isTrial" type="warning">试课</el-tag>
              <span v-else>正式</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button v-if="row.isTrial" type="success" size="small" @click="convertTrialInline(row)">转正</el-button>
              <el-button type="danger" size="small" @click="removeStudentInline(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { classApi } from '@/api/class'
import { courseApi } from '@/api/course'
import { teacherApi } from '@/api/teacher'
import { campusApi } from '@/api/campus'
import { studentApi } from '@/api/student'
import dayjs from 'dayjs'

const loading = ref(false)
const classes = ref([])
const courses = ref([])
const teachers = ref([])
const campuses = ref([])
const allStudents = ref([])
const filterCampus = ref<number | null>(null)
const filterTeacher = ref<number | null>(null)
const dialogVisible = ref(false)
const studentDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const newStudentFormRef = ref()
const studentTableRef = ref()
const currentClassId = ref<number | null>(null)
const currentClassInfo = ref<any>(null)
const currentClassStudents = ref<any[]>([])
const selectedStudents = ref<number[]>([])
const isTrial = ref(false)
const activeTab = ref('existing')
const studentGradeFilter = ref('')

const gradeOptions = [
  { value: 'PRESCHOOL', label: '学前' },
  { value: 'GRADE_1', label: '一年级' },
  { value: 'GRADE_2', label: '二年级' },
  { value: 'GRADE_3', label: '三年级' },
  { value: 'GRADE_4', label: '四年级' },
  { value: 'GRADE_5', label: '五年级' },
  { value: 'GRADE_6', label: '六年级' },
  { value: 'GRADE_7', label: '初一' },
  { value: 'GRADE_8', label: '初二' },
  { value: 'GRADE_9', label: '初三' },
  { value: 'GRADE_10', label: '高一' },
  { value: 'GRADE_11', label: '高二' },
  { value: 'GRADE_12', label: '高三' },
  { value: 'ADULT', label: '成人' }
]

const form = ref({
  id: null as number | null,
  className: '',
  gradeLevel: 'GRADE_1',
  courseId: null as number | null,
  teacherId: null as number | null,
  campusId: null as number | null,
  classroom: '',
  unitPrice: null as number | null,
  teacherFee: null as number | null,
  defaultDayOfWeek: null as number | null,
  defaultStartTime: null as Date | null,
  defaultEndTime: null as Date | null
})

const newStudentForm = ref({
  name: '',
  gradeLevel: 'GRADE_1',
  parentName: '',
  parentPhone: ''
})

const newStudentIsTrial = ref(false)

const rules = {
  className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  gradeLevel: [{ required: true, message: '请选择年级', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择老师', trigger: 'change' }],
  campusId: [{ required: true, message: '请选择校区', trigger: 'change' }]
}

const newStudentRules = {
  name: [{ required: true, message: '请输入学生姓名', trigger: 'blur' }]
}

const availableStudents = computed(() => {
  const studentIds = currentClassStudents.value.map((s: any) => s.student.id)
  return allStudents.value.filter((s: any) => !studentIds.includes(s.id) && s.status !== 'DELETED')
})

// 根据年级筛选学生
const filteredAvailableStudents = computed(() => {
  if (!studentGradeFilter.value) {
    return availableStudents.value
  }
  return availableStudents.value.filter((s: any) => s.gradeLevel === studentGradeFilter.value)
})

// 按年级排序，同年级在前
const sortedAvailableStudents = computed(() => {
  const classGrade = currentClassInfo.value?.gradeLevel
  return [...filteredAvailableStudents.value].sort((a: any, b: any) => {
    const aMatch = a.gradeLevel === classGrade ? 0 : 1
    const bMatch = b.gradeLevel === classGrade ? 0 : 1
    if (aMatch !== bMatch) return aMatch - bMatch
    return a.name.localeCompare(b.name)
  })
})

const getCourseDefaultPrice = () => {
  const course = courses.value.find((c: any) => c.id === form.value.courseId)
  return course?.unitPrice || '-'
}

const getGradeLabel = (grade: string) => {
  const gradeMap: Record<string, string> = {
    'PRESCHOOL': '学前',
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
    'GRADE_12': '高三',
    'ADULT': '成人'
  }
  return gradeMap[grade] || grade
}

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    'ACTIVE': 'success',
    'INACTIVE': 'info',
    'GRADUATED': 'primary',
    'DELETED': 'danger'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'ACTIVE': '在读',
    'INACTIVE': '停用',
    'GRADUATED': '已毕业',
    'DELETED': '已删除'
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

// 开始时间变化时，自动设置结束时间为开始时间+2小时
const handleStartTimeChange = (val: Date | null) => {
  if (val && !form.value.defaultEndTime) {
    const endTime = new Date(val.getTime() + 2 * 60 * 60 * 1000)
    form.value.defaultEndTime = endTime
  }
}

const fetchClasses = async () => {
  loading.value = true
  try {
    const res = await classApi.getAll()
    classes.value = res.data || []
  } catch (error) {
    console.error('获取班级列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchOptions = async () => {
  try {
    const [coursesRes, teachersRes, campusesRes, studentsRes] = await Promise.all([
      courseApi.getActive(),
      teacherApi.getActive(),
      campusApi.getActive(),
      studentApi.getAll()
    ])
    courses.value = coursesRes.data || []
    teachers.value = teachersRes.data || []
    campuses.value = campusesRes.data || []
    allStudents.value = studentsRes.data || []
  } catch (error) {
    console.error('获取选项失败:', error)
  }
}

const showAddDialog = () => {
  isEdit.value = false
  form.value = {
    id: null,
    className: '',
    gradeLevel: 'GRADE_1',
    courseId: null,
    teacherId: null,
    campusId: null,
    classroom: '',
    unitPrice: null,
    teacherFee: null,
    defaultDayOfWeek: null,
    defaultStartTime: null,
    defaultEndTime: null
  }
  dialogVisible.value = true
}

const editClass = (row: any) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    className: row.className,
    gradeLevel: row.gradeLevel,
    courseId: row.course?.id,
    teacherId: row.teacher?.id,
    campusId: row.campus?.id,
    classroom: row.classroom,
    unitPrice: row.unitPrice,
    teacherFee: row.teacherFee,
    defaultDayOfWeek: row.defaultDayOfWeek,
    defaultStartTime: row.defaultStartTime ? dayjs(`2024-01-01 ${row.defaultStartTime}`).toDate() : null,
    defaultEndTime: row.defaultEndTime ? dayjs(`2024-01-01 ${row.defaultEndTime}`).toDate() : null
  }
  dialogVisible.value = true
}

const saveClass = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    const data = {
      course: { id: form.value.courseId },
      teacher: { id: form.value.teacherId },
      campus: { id: form.value.campusId },
      className: form.value.className,
      gradeLevel: form.value.gradeLevel,
      classroom: form.value.classroom,
      unitPrice: form.value.unitPrice,
      teacherFee: form.value.teacherFee,
      defaultDayOfWeek: form.value.defaultDayOfWeek,
      defaultStartTime: form.value.defaultStartTime ? dayjs(form.value.defaultStartTime).format('HH:mm') : null,
      defaultEndTime: form.value.defaultEndTime ? dayjs(form.value.defaultEndTime).format('HH:mm') : null
    }

    if (isEdit.value && form.value.id) {
      await classApi.update(form.value.id, data)
      ElMessage.success('更新成功')
    } else {
      await classApi.create(data)
      ElMessage.success('添加成功')
    }

    dialogVisible.value = false
    fetchClasses()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || (isEdit.value ? '更新失败' : '添加失败'))
  }
}

const manageStudents = async (row: any) => {
  currentClassId.value = row.id
  currentClassInfo.value = row
  currentClassStudents.value = row.students || []
  selectedStudents.value = []
  isTrial.value = false
  activeTab.value = 'existing'
  // 默认筛选为班级同年级
  studentGradeFilter.value = row.gradeLevel
  newStudentForm.value = { name: '', gradeLevel: row.gradeLevel, parentName: '', parentPhone: '' }
  newStudentIsTrial.value = false
  studentDialogVisible.value = true
}

const handleSelectionChange = (selection: any[]) => {
  selectedStudents.value = selection.map(s => s.id)
}

const addStudents = async () => {
  if (!currentClassId.value || selectedStudents.value.length === 0) return

  console.log('[添加学生] 开始添加学生，班级ID:', currentClassId.value, '学生IDs:', selectedStudents.value)

  try {
    // 获取当前已添加的学生ID列表，用于过滤
    const existingStudentIds = currentClassStudents.value.map((s: any) => s.student.id)
    console.log('[添加学生] 当前已存在的学生IDs:', existingStudentIds)
    let addedCount = 0
    let skippedCount = 0

    for (const studentId of selectedStudents.value) {
      // 检查学生是否已经在班级中
      if (existingStudentIds.includes(studentId)) {
        console.log('[添加学生] 学生已在班级中，跳过:', studentId)
        skippedCount++
        continue
      }
      try {
        console.log('[添加学生] 调用API添加学生:', studentId)
        const addRes = await classApi.addStudent(currentClassId.value, studentId, isTrial.value)
        console.log('[添加学生] API返回:', addRes)
        addedCount++
      } catch (err: any) {
        console.error('[添加学生] 添加失败:', err)
        if (err.response?.data?.message?.includes('已在班级中')) {
          skippedCount++
        } else {
          throw err
        }
      }
    }

    if (addedCount > 0) {
      ElMessage.success(`成功添加 ${addedCount} 名学生`)
    }
    if (skippedCount > 0) {
      ElMessage.warning(`${skippedCount} 名学生已在班级中，已跳过`)
    }

    selectedStudents.value = []
    isTrial.value = false

    // 先刷新班级列表
    console.log('[添加学生] 开始刷新班级列表')
    await fetchClasses()
    console.log('[添加学生] 班级列表刷新完成，classes:', classes.value)

    // 再刷新当前班级学生列表
    console.log('[添加学生] 开始获取班级详情，ID:', currentClassId.value)
    const res = await classApi.getById(currentClassId.value)
    console.log('[添加学生] 班级详情API返回:', res)
    console.log('[添加学生] res.data:', res.data)
    console.log('[添加学生] res.data?.students:', res.data?.students)

    currentClassStudents.value = res.data?.students || []
    currentClassInfo.value = res.data
    console.log('[添加学生] 更新后的currentClassStudents:', currentClassStudents.value)

    // 刷新可选学生列表
    await fetchOptions()
    console.log('[添加学生] 可选学生列表刷新完成')
  } catch (error: any) {
    console.error('[添加学生] 整体流程出错:', error)
    ElMessage.error(error.response?.data?.message || '添加失败')
  }
}

const addNewStudent = async () => {
  const valid = await newStudentFormRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!currentClassId.value) return

  try {
    // 1. 创建新学生
    const studentRes = await studentApi.create(newStudentForm.value)
    const newStudentId = studentRes.data.id

    if (!newStudentId) {
      ElMessage.error('创建学生失败')
      return
    }

    // 2. 添加到班级
    await classApi.addStudent(currentClassId.value, newStudentId, newStudentIsTrial.value)

    ElMessage.success('新学生已创建并加入班级')
    newStudentForm.value = { name: '', gradeLevel: currentClassInfo.value?.gradeLevel || 'GRADE_1', parentName: '', parentPhone: '' }
    newStudentIsTrial.value = false

    // 刷新数据
    await fetchClasses()
    await fetchOptions()
    const res = await classApi.getById(currentClassId.value)
    currentClassStudents.value = res.data?.students || []
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '添加失败')
  }
}

const removeStudent = async (classRow: any, studentRow: any) => {
  try {
    await ElMessageBox.confirm('确定要移除此学生吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await classApi.removeStudent(classRow.id, studentRow.student.id)
    ElMessage.success('移除成功')
    fetchClasses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
    }
  }
}

const removeStudentInline = async (row: any) => {
  if (!currentClassId.value) return

  try {
    await ElMessageBox.confirm('确定要移除此学生吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await classApi.removeStudent(currentClassId.value, row.student.id)
    ElMessage.success('移除成功')
    fetchClasses()
    const res = await classApi.getById(currentClassId.value)
    currentClassStudents.value = res.data?.students || []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
    }
  }
}

const convertTrial = async (classRow: any, studentRow: any) => {
  try {
    await classApi.convertTrial(classRow.id, studentRow.student.id)
    ElMessage.success('转正成功')
    fetchClasses()
  } catch (error) {
    ElMessage.error('转正失败')
  }
}

const convertTrialInline = async (row: any) => {
  if (!currentClassId.value) return

  try {
    await classApi.convertTrial(currentClassId.value, row.student.id)
    ElMessage.success('转正成功')
    fetchClasses()
    const res = await classApi.getById(currentClassId.value)
    currentClassStudents.value = res.data?.students || []
  } catch (error) {
    ElMessage.error('转正失败')
  }
}

const deleteClass = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要彻底删除班级 "${row.className}" 吗？\n警告：此操作将永久删除该班级及其所有课节、签到记录和学生关联数据，不可恢复！`,
      '危险操作',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    await classApi.delete(row.id)
    ElMessage.success('班级已彻底删除')
    fetchClasses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 筛选后的班级列表
const filteredClasses = computed(() => {
  let result = classes.value

  // 按校区筛选
  if (filterCampus.value) {
    result = result.filter((c: any) => c.campus?.id === filterCampus.value)
  }

  // 按老师筛选
  if (filterTeacher.value) {
    result = result.filter((c: any) => c.teacher?.id === filterTeacher.value)
  }

  return result
})

// 应用筛选
const applyFilter = () => {
  // 筛选通过计算属性自动应用
}

// 生成班级名称
const generateClassName = () => {
  const parts: string[] = []

  // 年级
  if (form.value.gradeLevel) {
    const gradeLabel = getGradeLabel(form.value.gradeLevel)
    parts.push(gradeLabel)
  }

  // 课程类型
  if (form.value.courseId) {
    const course = courses.value.find((c: any) => c.id === form.value.courseId)
    if (course) {
      parts.push(getCourseTypeLabel(course.type))
    }
  }

  // 老师
  if (form.value.teacherId) {
    const teacher = teachers.value.find((t: any) => t.id === form.value.teacherId)
    if (teacher) {
      parts.push(teacher.name)
    }
  }

  // 校区
  if (form.value.campusId) {
    const campus = campuses.value.find((c: any) => c.id === form.value.campusId)
    if (campus) {
      parts.push(campus.name)
    }
  }

  // 星期
  if (form.value.defaultDayOfWeek) {
    const weekDays = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
    parts.push(weekDays[form.value.defaultDayOfWeek])
  }

  if (parts.length > 0) {
    form.value.className = parts.join('-')
  }
}

// 自动生成年級名称（仅在添加模式下且名称为空时）
const autoGenerateClassName = () => {
  if (!isEdit.value && !form.value.className) {
    generateClassName()
  }
}

onMounted(() => {
  fetchClasses()
  fetchOptions()
})
</script>

<style scoped>
.classes {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.student-list {
  padding: 20px;
  background-color: #f5f7fa;
}

.student-list h4 {
  margin-bottom: 15px;
  color: #606266;
}

.student-manage {
  padding: 10px;
}

.class-info-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.student-table-section {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.add-new-student {
  max-width: 500px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.text-gray {
  color: #909399;
}
</style>
