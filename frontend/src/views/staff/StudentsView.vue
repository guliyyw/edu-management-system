<template>
  <div class="students">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生管理</span>
          <div class="header-actions">
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
      
      <el-table :data="students" border v-loading="loading">
        <el-table-column prop="name" label="学生姓名" width="120" />
        <el-table-column prop="parentName" label="家长姓名" width="120" />
        <el-table-column prop="parentPhone" label="家长电话" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editStudent(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteStudent(row.id)">删除</el-button>
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
        <el-form-item label="家长姓名" prop="parentName">
          <el-input v-model="form.parentName" placeholder="请输入家长姓名" />
        </el-form-item>
        <el-form-item label="家长电话" prop="parentPhone">
          <el-input v-model="form.parentPhone" placeholder="请输入家长电话" />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { studentApi } from '@/api/student'
import dayjs from 'dayjs'

const loading = ref(false)
const students = ref([])
const searchKeyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = ref({
  id: null as number | null,
  name: '',
  parentName: '',
  parentPhone: ''
})

const rules = {
  name: [{ required: true, message: '请输入学生姓名', trigger: 'blur' }],
  parentName: [{ required: true, message: '请输入家长姓名', trigger: 'blur' }],
  parentPhone: [{ required: true, message: '请输入家长电话', trigger: 'blur' }]
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
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

const showAddDialog = () => {
  isEdit.value = false
  form.value = {
    id: null,
    name: '',
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

const deleteStudent = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除此学生吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await studentApi.delete(id)
    ElMessage.success('删除成功')
    fetchStudents()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
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
