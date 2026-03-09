<template>
  <div class="students">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生管理</span>
          <div class="header-actions">
            <el-select
              v-model="gradeFilter"
              placeholder="年级筛选"
              style="width: 120px; margin-right: 10px;"
              size="small"
              clearable
              @change="handleFilterChange"
            >
              <el-option label="全部年级" value="" />
              <el-option
                v-for="item in gradeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-select
              v-model="statusFilter"
              placeholder="状态筛选"
              style="width: 120px; margin-right: 10px;"
              size="small"
              clearable
              @change="handleFilterChange"
            >
              <el-option label="全部状态" value="" />
              <el-option label="在读" value="ACTIVE" />
              <el-option label="停用" value="INACTIVE" />
            </el-select>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索学生姓名或家长电话"
              style="width: 250px; margin-right: 10px;"
              size="small"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
            <el-button type="primary" size="small" @click="showAddDialog">添加学生</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="filteredStudents" border v-loading="loading" height="calc(100vh - 240px)" style="width: 100%">
        <el-table-column prop="name" label="学生姓名" width="120" />
        <el-table-column prop="gradeLevel" label="年级" width="100">
          <template #default="{ row }">
            <el-tag :type="getGradeType(row.gradeLevel)" size="small">{{ getGradeLabel(row.gradeLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="parentName" label="家长姓名" width="120" />
        <el-table-column prop="parentPhone" label="家长电话" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editStudent(row)">编辑</el-button>
            <el-button
              :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
              size="small"
              @click="toggleStudentStatus(row)"
            >
              {{ row.status === 'ACTIVE' ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑学生' : '添加学生'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="学生姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入学生姓名" />
        </el-form-item>
        <el-form-item label="年级" prop="gradeLevel">
          <el-select v-model="form.gradeLevel" placeholder="请选择年级" style="width: 100%">
            <el-option
              v-for="item in gradeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="家长姓名">
          <el-input v-model="form.parentName" placeholder="请输入家长姓名（选填）" />
        </el-form-item>
        <el-form-item label="家长电话">
          <el-input v-model="form.parentPhone" placeholder="请输入家长电话（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveStudent">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { studentApi } from '@/api/student'
import dayjs from 'dayjs'

const loading = ref(false)
const students = ref([])
const searchKeyword = ref('')
const statusFilter = ref('')
const gradeFilter = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = ref({
  id: null as number | null,
  name: '',
  gradeLevel: 'GRADE_1',
  parentName: '',
  parentPhone: ''
})

const gradeOptions = [
  { value: 'PRESCHOOL', label: '学前' },
  { value: 'GRADE_1', label: '一年级' },
  { value: 'GRADE_2', label: '二年级' },
  { value: 'GRADE_3', label: '三年级' },
  { value: 'GRADE_4', label: '四年级' },
  { value: 'GRADE_5', label: '五年级' },
  { value: 'GRADE_6', label: '六年级' },
  { value: 'GRADE_7', label: '七年级' },
  { value: 'GRADE_8', label: '八年级' },
  { value: 'GRADE_9', label: '九年级' },
  { value: 'GRADE_10', label: '高一' },
  { value: 'GRADE_11', label: '高二' },
  { value: 'GRADE_12', label: '高三' },
  { value: 'ADULT', label: '成人' }
]

const getGradeLabel = (grade: string) => {
  const option = gradeOptions.find(g => g.value === grade)
  return option?.label || grade
}

const getGradeType = (grade: string) => {
  const types: Record<string, string> = {
    'PRESCHOOL': 'warning',
    'GRADE_1': 'info',
    'GRADE_2': 'info',
    'GRADE_3': 'info',
    'GRADE_4': 'info',
    'GRADE_5': 'info',
    'GRADE_6': 'success',
    'GRADE_7': 'info',
    'GRADE_8': 'info',
    'GRADE_9': 'info',
    'GRADE_10': 'primary',
    'GRADE_11': 'primary',
    'GRADE_12': 'primary',
    'ADULT': 'danger'
  }
  return types[grade] || 'info'
}

const rules = {
  name: [{ required: true, message: '请输入学生姓名', trigger: 'blur' }],
  gradeLevel: [{ required: true, message: '请选择年级', trigger: 'change' }]
}

const filteredStudents = computed(() => {
  let result = students.value

  // 年级筛选
  if (gradeFilter.value) {
    result = result.filter((s: any) => s.gradeLevel === gradeFilter.value)
  }

  // 状态筛选
  if (statusFilter.value) {
    result = result.filter((s: any) => s.status === statusFilter.value)
  }

  // 默认排序：在读在前，然后按创建时间倒序
  return result.sort((a: any, b: any) => {
    // 在读状态优先
    if (a.status === 'ACTIVE' && b.status !== 'ACTIVE') return -1
    if (a.status !== 'ACTIVE' && b.status === 'ACTIVE') return 1
    // 然后按创建时间倒序
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  })
})

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    'ACTIVE': 'success',
    'INACTIVE': 'info'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'ACTIVE': '在读',
    'INACTIVE': '停用'
  }
  return labels[status] || status
}

const fetchStudents = async () => {
  loading.value = true
  try {
    const res = await studentApi.getAll()
    students.value = res.data || []
  } catch (error) {
    console.error('获取学生列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value) {
    fetchStudents()
    return
  }

  try {
    const res = await studentApi.search(searchKeyword.value)
    students.value = res.data || []
  } catch (error) {
    console.error('搜索失败:', error)
  }
}

const handleFilterChange = () => {
  // 筛选通过计算属性自动处理
}

const showAddDialog = () => {
  isEdit.value = false
  form.value = {
    id: null,
    name: '',
    gradeLevel: 'GRADE_1',
    parentName: '',
    parentPhone: ''
  }
  dialogVisible.value = true
}

const editStudent = (row: any) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    name: row.name,
    gradeLevel: row.gradeLevel || 'GRADE_1',
    parentName: row.parentName,
    parentPhone: row.parentPhone
  }
  dialogVisible.value = true
}

const saveStudent = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  try {
    if (isEdit.value && form.value.id) {
      await studentApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await studentApi.create(form.value)
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    fetchStudents()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  }
}

const toggleStudentStatus = async (row: any) => {
  const newStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const actionText = newStatus === 'ACTIVE' ? '启用' : '停用'

  try {
    await ElMessageBox.confirm(`确定要${actionText}学生 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await studentApi.update(row.id, { ...row, status: newStatus })
    ElMessage.success(`已${actionText}`)
    fetchStudents()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  fetchStudents()
})
</script>

<style scoped>
.students {
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
</style>
