<template>
  <div class="classes">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>班级管理</span>
          <el-button type="primary" size="small" @click="showAddDialog">添加班级</el-button>
        </div>
      </template>
      
      <el-table :data="classes" border v-loading="loading">
        <el-table-column prop="course.name" label="课程" />
        <el-table-column prop="teacher.name" label="老师" />
        <el-table-column prop="campus.name" label="校区" />
        <el-table-column prop="classroom" label="教室" width="100" />
        <el-table-column label="上课时间" width="150">
          <template #default="{ row }">
            周{{ ['一', '二', '三', '四', '五', '六', '日'][row.dayOfWeek - 1] }} 
            {{ row.startTime }} - {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="manageStudents(row)">学生</el-button>
            <el-button type="warning" size="small" @click="editClass(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑班级' : '添加班级'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="选择课程" style="width: 100%;">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="老师" prop="teacherId">
          <el-select v-model="form.teacherId" placeholder="选择老师" style="width: 100%;">
            <el-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="teacher.name"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="校区" prop="campusId">
          <el-select v-model="form.campusId" placeholder="选择校区" style="width: 100%;">
            <el-option
              v-for="campus in campuses"
              :key="campus.id"
              :label="campus.name"
              :value="campus.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="教室" prop="classroom">
          <el-input v-model="form.classroom" placeholder="请输入教室" />
        </el-form-item>
        <el-form-item label="星期" prop="dayOfWeek">
          <el-select v-model="form.dayOfWeek" placeholder="选择星期" style="width: 100%;">
            <el-option label="周一" :value="1" />
            <el-option label="周二" :value="2" />
            <el-option label="周三" :value="3" />
            <el-option label="周四" :value="4" />
            <el-option label="周五" :value="5" />
            <el-option label="周六" :value="6" />
            <el-option label="周日" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker v-model="form.startTime" format="HH:mm" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker v-model="form.endTime" format="HH:mm" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveClass">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 学生管理对话框 -->
    <el-dialog v-model="studentDialogVisible" title="班级学生管理" width="700px">
      <div class="student-manage">
        <div class="add-student">
          <el-select v-model="selectedStudent" placeholder="选择学生" style="width: 200px;">
            <el-option
              v-for="student in availableStudents"
              :key="student.id"
              :label="student.name"
              :value="student.id"
            />
          </el-select>
          <el-checkbox v-model="isTrial" style="margin: 0 10px;">试课</el-checkbox>
          <el-button type="primary" @click="addStudent">添加</el-button>
        </div>
        
        <el-table :data="currentClassStudents" border style="margin-top: 20px;">
          <el-table-column prop="student.name" label="学生姓名" />
          <el-table-column prop="student.parentPhone" label="家长电话" />
          <el-table-column label="是否试课" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.isTrial" type="warning">试课</el-tag>
              <span v-else>正式</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button v-if="row.isTrial" type="success" size="small" @click="convertTrial(row)">转正</el-button>
              <el-button type="danger" size="small" @click="removeStudent(row)">移除</el-button>
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
const dialogVisible = ref(false)
const studentDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const currentClassId = ref<number | null>(null)
const currentClassStudents = ref([])
const selectedStudent = ref<number | null>(null)
const isTrial = ref(false)

const form = ref({
  id: null as number | null,
  courseId: null as number | null,
  teacherId: null as number | null,
  campusId: null as number | null,
  classroom: '',
  dayOfWeek: 1,
  startTime: new Date(2024, 0, 1, 9, 0),
  endTime: new Date(2024, 0, 1, 10, 30)
})

const rules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择老师', trigger: 'change' }],
  campusId: [{ required: true, message: '请选择校区', trigger: 'change' }],
  classroom: [{ required: true, message: '请输入教室', trigger: 'blur' }],
  dayOfWeek: [{ required: true, message: '请选择星期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const availableStudents = computed(() => {
  const studentIds = currentClassStudents.value.map((s: any) => s.student.id)
  return allStudents.value.filter((s: any) => !studentIds.includes(s.id))
})

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
    courseId: null,
    teacherId: null,
    campusId: null,
    classroom: '',
    dayOfWeek: 1,
    startTime: new Date(2024, 0, 1, 9, 0),
    endTime: new Date(2024, 0, 1, 10, 30)
  }
  dialogVisible.value = true
}

const editClass = (row: any) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    courseId: row.course.id,
    teacherId: row.teacher.id,
    campusId: row.campus.id,
    classroom: row.classroom,
    dayOfWeek: row.dayOfWeek,
    startTime: dayjs(`2024-01-01 ${row.startTime}`).toDate(),
    endTime: dayjs(`2024-01-01 ${row.endTime}`).toDate()
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
      classroom: form.value.classroom,
      dayOfWeek: form.value.dayOfWeek,
      startTime: dayjs(form.value.startTime).format('HH:mm'),
      endTime: dayjs(form.value.endTime).format('HH:mm')
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
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  }
}

const manageStudents = async (row: any) => {
  currentClassId.value = row.id
  currentClassStudents.value = row.students || []
  studentDialogVisible.value = true
}

const addStudent = async () => {
  if (!selectedStudent.value || !currentClassId.value) return
  
  try {
    await classApi.addStudent(currentClassId.value, selectedStudent.value, isTrial.value)
    ElMessage.success('添加成功')
    selectedStudent.value = null
    isTrial.value = false
    fetchClasses()
    // 刷新当前班级学生列表
    const res = await classApi.getById(currentClassId.value)
    currentClassStudents.value = res.data?.students || []
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const removeStudent = async (row: any) => {
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

const convertTrial = async (row: any) => {
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

.student-manage {
  padding: 20px;
}

.add-student {
  display: flex;
  align-items: center;
}
</style>
