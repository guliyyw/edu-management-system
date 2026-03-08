<template>
  <div class="courses">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>课程管理</span>
          <el-button type="primary" size="small" @click="showAddDialog">添加课程</el-button>
        </div>
      </template>
      
      <el-table :data="courses" border v-loading="loading">
        <el-table-column prop="name" label="课程名称" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="unitPrice" label="单价(元/节)" width="120">
          <template #default="{ row }">
            {{ row.unitPrice.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="trialPrice" label="试课价" width="100">
          <template #default="{ row }">
            {{ row.trialPrice.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editCourse(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteCourse(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑课程' : '添加课程'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择课程类型" style="width: 100%;">
            <el-option label="数学" value="MATH" />
            <el-option label="英语" value="ENGLISH" />
            <el-option label="物理" value="PHYSICS" />
            <el-option label="化学" value="CHEMISTRY" />
            <el-option label="语文" value="CHINESE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="试课价" prop="trialPrice">
          <el-input-number v-model="form.trialPrice" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCourse">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { courseApi } from '@/api/course'

const loading = ref(false)
const courses = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = ref({
  id: null as number | null,
  name: '',
  type: 'MATH',
  unitPrice: 0,
  trialPrice: 0
})

const rules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择课程类型', trigger: 'change' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }]
}

const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await courseApi.getAll()
    courses.value = res.data || []
  } catch (error) {
    console.error('获取课程列表失败:', error)
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  isEdit.value = false
  form.value = {
    id: null,
    name: '',
    type: 'MATH',
    unitPrice: 100,
    trialPrice: 0
  }
  dialogVisible.value = true
}

const editCourse = (row: any) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    name: row.name,
    type: row.type,
    unitPrice: row.unitPrice,
    trialPrice: row.trialPrice
  }
  dialogVisible.value = true
}

const saveCourse = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  try {
    if (isEdit.value && form.value.id) {
      await courseApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await courseApi.create(form.value)
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    fetchCourses()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  }
}

const deleteCourse = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除此课程吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await courseApi.delete(id)
    ElMessage.success('删除成功')
    fetchCourses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.courses {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
