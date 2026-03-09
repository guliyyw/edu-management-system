<template>
  <div class="courses">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>科目管理</span>
          <el-button type="primary" size="small" @click="showAddDialog">添加科目</el-button>
        </div>
      </template>

      <el-table :data="courses" border v-loading="loading" height="calc(100vh - 220px)" style="width: 100%">
        <el-table-column prop="type" label="科目类型" width="150">
          <template #default="{ row }">
            {{ getCourseTypeLabel(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="deleteCourse(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加对话框 -->
    <el-dialog v-model="dialogVisible" title="添加科目" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="科目类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择科目类型" style="width: 100%;">
            <el-option label="语文" value="CHINESE" />
            <el-option label="数学" value="MATH" />
            <el-option label="英语" value="ENGLISH" />
            <el-option label="物理" value="PHYSICS" />
            <el-option label="化学" value="CHEMISTRY" />
            <el-option label="生物" value="BIOLOGY" />
            <el-option label="历史" value="HISTORY" />
            <el-option label="地理" value="GEOGRAPHY" />
            <el-option label="政治" value="POLITICS" />
            <el-option label="美术" value="ART" />
            <el-option label="音乐" value="MUSIC" />
            <el-option label="体育" value="SPORTS" />
            <el-option label="其他" value="OTHER" />
          </el-select>
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
const formRef = ref()

const form = ref({
  type: 'CHINESE'
})

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

const rules = {
  type: [{ required: true, message: '请选择科目类型', trigger: 'change' }]
}

const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await courseApi.getAll()
    courses.value = res.data || []
  } catch (error) {
    console.error('获取科目列表失败:', error)
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  form.value = {
    type: 'CHINESE'
  }
  dialogVisible.value = true
}

const saveCourse = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    // 检查是否已存在该科目类型
    const exists = courses.value.some((c: any) => c.type === form.value.type)
    if (exists) {
      ElMessage.warning('该科目类型已存在')
      return
    }

    // 使用科目类型作为名称
    const data = {
      type: form.value.type,
      name: courseTypeLabels[form.value.type] || form.value.type,
      unitPrice: 0,
      trialPrice: 0
    }

    await courseApi.create(data)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    fetchCourses()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '添加失败')
  }
}

const deleteCourse = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除此科目吗？', '提示', {
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
