<template>
  <div class="campuses">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>校区管理</span>
          <el-button type="primary" size="small" @click="showAddDialog">添加校区</el-button>
        </div>
      </template>
      
      <!-- 桌面端表格 -->
      <div class="mobile-card-table">
        <el-table :data="campuses" border v-loading="loading" height="calc(100vh - 220px)" style="width: 100%">
          <el-table-column prop="name" label="校区名称" />
          <el-table-column prop="address" label="地址" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="editCampus(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="deleteCampus(row.id)">删除</el-button>
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
            v-for="campus in campuses" 
            :key="campus.id" 
            class="mobile-card-item"
          >
            <div class="mobile-card-header">
              <span class="mobile-card-title">{{ campus.name }}</span>
              <el-tag :type="campus.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                {{ campus.status === 'ACTIVE' ? '正常' : '停用' }}
              </el-tag>
            </div>
            <div class="mobile-card-body">
              <div class="mobile-card-row">
                <span class="mobile-card-label">地址</span>
                <span class="mobile-card-value">{{ campus.address || '-' }}</span>
              </div>
            </div>
            <div class="mobile-card-actions">
              <el-button type="primary" size="small" @click="editCampus(campus)">编辑</el-button>
              <el-button type="danger" size="small" @click="deleteCampus(campus.id)">删除</el-button>
            </div>
          </div>
          <el-empty v-if="campuses.length === 0" description="暂无数据" />
        </template>
      </div>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑校区' : '添加校区'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="校区名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入校区名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" type="textarea" placeholder="请输入地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCampus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { campusApi } from '@/api/campus'

const loading = ref(false)
const campuses = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = ref({
  id: null as number | null,
  name: '',
  address: ''
})

const rules = {
  name: [{ required: true, message: '请输入校区名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

const fetchCampuses = async () => {
  loading.value = true
  try {
    const res = await campusApi.getAll()
    campuses.value = res.data || []
  } catch (error) {
    console.error('获取校区列表失败:', error)
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  isEdit.value = false
  form.value = {
    id: null,
    name: '',
    address: ''
  }
  dialogVisible.value = true
}

const editCampus = (row: any) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    name: row.name,
    address: row.address
  }
  dialogVisible.value = true
}

const saveCampus = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  try {
    if (isEdit.value && form.value.id) {
      await campusApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await campusApi.create(form.value)
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    fetchCampuses()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  }
}

const deleteCampus = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除此校区吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await campusApi.delete(id)
    ElMessage.success('删除成功')
    fetchCampuses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchCampuses()
})
</script>

<style scoped>
.campuses {
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
