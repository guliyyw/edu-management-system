<template>
  <div class="teachers">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>老师管理</span>
          <el-button type="primary" size="small" @click="showAddDialog">添加老师</el-button>
        </div>
      </template>
      
      <!-- 桌面端表格 -->
      <div class="mobile-card-table">
        <el-table :data="teachers" border v-loading="loading" height="calc(100vh - 220px)" style="width: 100%">
          <el-table-column prop="name" label="老师姓名" width="120" />
          <el-table-column prop="phone" label="电话" width="150" />
          <el-table-column label="所属校区">
            <template #default="{ row }">
              <el-tag
                v-for="campus in row.campuses"
                :key="campus.id"
                size="small"
                style="margin-right: 5px;"
              >
                {{ campus.name }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="editTeacher(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="deleteTeacher(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 移动端卡片列表 -->
      <div class="mobile-card-list">
        <div v-if="loading" class="mobile-loading">
          <el-skeleton :rows="3" animated />
        </div>
        <template v-else>
          <div 
            v-for="teacher in teachers" 
            :key="teacher.id" 
            class="mobile-card-item"
          >
            <div class="mobile-card-header">
              <span class="mobile-card-title">{{ teacher.name }}</span>
              <el-tag :type="teacher.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                {{ teacher.status === 'ACTIVE' ? '在职' : '停用' }}
              </el-tag>
            </div>
            <div class="mobile-card-body">
              <div class="mobile-card-row">
                <span class="mobile-card-label">电话</span>
                <span class="mobile-card-value">{{ teacher.phone || '-' }}</span>
              </div>
              <div class="mobile-card-row">
                <span class="mobile-card-label">所属校区</span>
                <span class="mobile-card-value">
                  <el-tag
                    v-for="campus in teacher.campuses"
                    :key="campus.id"
                    size="small"
                    style="margin-right: 5px; margin-bottom: 5px;"
                  >
                    {{ campus.name }}
                  </el-tag>
                  <span v-if="!teacher.campuses?.length">-</span>
                </span>
              </div>
            </div>
            <div class="mobile-card-actions">
              <el-button type="primary" size="small" @click="editTeacher(teacher)">编辑</el-button>
              <el-button type="danger" size="small" @click="deleteTeacher(teacher.id)">删除</el-button>
            </div>
          </div>
          <el-empty v-if="teachers.length === 0" description="暂无数据" />
        </template>
      </div>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑老师' : '添加老师'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="老师姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入老师姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="所属校区" prop="campusIds">
          <el-select v-model="form.campusIds" multiple placeholder="请选择校区" style="width: 100%;">
            <el-option
              v-for="campus in campuses"
              :key="campus.id"
              :label="campus.name"
              :value="campus.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTeacher">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { teacherApi } from '@/api/teacher'
import { campusApi } from '@/api/campus'

const loading = ref(false)
const teachers = ref([])
const campuses = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = ref({
  id: null as number | null,
  name: '',
  phone: '',
  campusIds: [] as number[]
})

const rules = {
  name: [{ required: true, message: '请输入老师姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  campusIds: [{ required: true, message: '请选择所属校区', trigger: 'change' }]
}

const fetchTeachers = async () => {
  loading.value = true
  try {
    const res = await teacherApi.getAll()
    teachers.value = res.data || []
  } catch (error) {
    console.error('获取老师列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchCampuses = async () => {
  try {
    const res = await campusApi.getActive()
    campuses.value = res.data || []
  } catch (error) {
    console.error('获取校区列表失败:', error)
  }
}

const showAddDialog = () => {
  isEdit.value = false
  form.value = {
    id: null,
    name: '',
    phone: '',
    campusIds: []
  }
  dialogVisible.value = true
}

const editTeacher = (row: any) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    name: row.name,
    phone: row.phone,
    campusIds: row.campuses.map((c: any) => c.id)
  }
  dialogVisible.value = true
}

const saveTeacher = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    const data = {
      name: form.value.name,
      phone: form.value.phone,
      campusIds: form.value.campusIds
    }

    if (isEdit.value && form.value.id) {
      await teacherApi.update(form.value.id, data)
      ElMessage.success('更新成功')
    } else {
      await teacherApi.create(data)
      ElMessage.success('添加成功')
    }

    dialogVisible.value = false
    fetchTeachers()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || (isEdit.value ? '更新失败' : '添加失败'))
  }
}

const deleteTeacher = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除此老师吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await teacherApi.delete(id)
    ElMessage.success('删除成功')
    fetchTeachers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchTeachers()
  fetchCampuses()
})
</script>

<style scoped>
.teachers {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 移动端加载状态 */
.mobile-loading {
  padding: 20px;
}
</style>
