<template>
  <div class="travel-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>路程时间配置</span>
          <el-button type="primary" @click="showAddDialog">添加配置</el-button>
        </div>
      </template>
      
      <el-alert
        title="提示"
        description="设置校区之间的路程时间，系统将自动检测跨校区排课冲突。工作日和周末可设置不同的路程时间。"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />
      
      <el-table :data="travelTimes" border v-loading="loading" height="calc(100vh - 280px)" style="width: 100%">
        <el-table-column prop="fromCampus.name" label="起点校区" />
        <el-table-column prop="toCampus.name" label="终点校区" />
        <el-table-column prop="travelMinutes" label="路程时间(分钟)" width="150">
          <template #default="{ row }">
            <span style="color: #409EFF; font-weight: bold;">{{ row.travelMinutes }} 分钟</span>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveDate" label="生效日期" width="120" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editTravelTime(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteTravelTime(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑路程配置' : '添加路程配置'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="起点校区" prop="fromCampusId">
          <el-select v-model="form.fromCampusId" placeholder="选择起点校区" style="width: 100%;">
            <el-option
              v-for="campus in campuses"
              :key="campus.id"
              :label="campus.name"
              :value="campus.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="终点校区" prop="toCampusId">
          <el-select v-model="form.toCampusId" placeholder="选择终点校区" style="width: 100%;">
            <el-option
              v-for="campus in campuses"
              :key="campus.id"
              :label="campus.name"
              :value="campus.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="路程时间" prop="travelMinutes">
          <el-input-number v-model="form.travelMinutes" :min="5" :max="180" style="width: 100%;" />
          <span style="margin-left: 10px; color: #909399;">分钟</span>
        </el-form-item>
        <el-form-item label="生效日期" prop="effectiveDate">
          <el-date-picker
            v-model="form.effectiveDate"
            type="date"
            placeholder="选择生效日期"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTravelTime">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { travelTimeApi } from '@/api/travelTime'
import { campusApi } from '@/api/campus'
import dayjs from 'dayjs'

const authStore = useAuthStore()

const loading = ref(false)
const travelTimes = ref([])
const campuses = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = ref({
  id: null as number | null,
  fromCampusId: null as number | null,
  toCampusId: null as number | null,
  travelMinutes: 30,
  effectiveDate: new Date()
})

const rules = {
  fromCampusId: [{ required: true, message: '请选择起点校区', trigger: 'change' }],
  toCampusId: [{ required: true, message: '请选择终点校区', trigger: 'change' }],
  travelMinutes: [{ required: true, message: '请输入路程时间', trigger: 'blur' }],
  effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }]
}

const fetchTravelTimes = async () => {
  if (!authStore.user?.teacherId) return
  
  loading.value = true
  try {
    const res = await travelTimeApi.getByTeacher(authStore.user.teacherId)
    travelTimes.value = res.data || []
  } catch (error) {
    console.error('获取路程配置失败:', error)
  } finally {
    loading.value = false
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

const showAddDialog = () => {
  isEdit.value = false
  form.value = {
    id: null,
    fromCampusId: null,
    toCampusId: null,
    travelMinutes: 30,
    effectiveDate: new Date()
  }
  dialogVisible.value = true
}

const editTravelTime = (row: any) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    fromCampusId: row.fromCampus.id,
    toCampusId: row.toCampus.id,
    travelMinutes: row.travelMinutes,
    effectiveDate: new Date(row.effectiveDate)
  }
  dialogVisible.value = true
}

const saveTravelTime = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  try {
    const data = {
      teacher: { id: authStore.user?.teacherId },
      fromCampus: { id: form.value.fromCampusId },
      toCampus: { id: form.value.toCampusId },
      travelMinutes: form.value.travelMinutes,
      effectiveDate: dayjs(form.value.effectiveDate).format('YYYY-MM-DD')
    }
    
    if (isEdit.value && form.value.id) {
      await travelTimeApi.update(form.value.id, data)
      ElMessage.success('更新成功')
    } else {
      await travelTimeApi.create(data)
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    fetchTravelTimes()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  }
}

const deleteTravelTime = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除此配置吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await travelTimeApi.delete(id)
    ElMessage.success('删除成功')
    fetchTravelTimes()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchTravelTimes()
  fetchCampuses()
})
</script>

<style scoped>
.travel-config {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
