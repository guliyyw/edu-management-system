<template>
  <div class="announcements-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>发布公告
          </el-button>
        </div>
      </template>

      <el-table :data="announcements" border v-loading="loading" height="calc(100vh - 240px)" style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isPinned" label="置顶" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isPinned" type="danger" size="small">置顶</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'info'" size="small">
              {{ row.isActive ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdByName" label="发布人" width="120" />
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑公告' : '发布公告'"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            placeholder="请输入公告内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="主要" value="primary" />
            <el-option label="成功" value="success" />
            <el-option label="警告" value="warning" />
            <el-option label="危险" value="danger" />
            <el-option label="信息" value="info" />
          </el-select>
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.isPinned" active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.isActive" active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker
            v-model="form.publishTime"
            type="datetime"
            placeholder="选择生效时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="form.expireTime"
            type="datetime"
            placeholder="选择过期时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { announcementApi, type Announcement } from '@/api/announcement'
import dayjs from 'dayjs'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const announcements = ref<Announcement[]>([])
const formRef = ref()

const form = ref<Partial<Announcement>>({
  title: '',
  content: '',
  type: 'primary',
  isPinned: false,
  isActive: true,
  publishTime: '',
  expireTime: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const fetchAnnouncements = async () => {
  loading.value = true
  try {
    const res = await announcementApi.getAll()
    announcements.value = res.data || []
  } catch (error) {
    console.error('获取公告列表失败:', error)
    ElMessage.error('获取公告列表失败')
  } finally {
    loading.value = false
  }
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    primary: '主要',
    success: '成功',
    warning: '警告',
    danger: '危险',
    info: '信息'
  }
  return map[type] || type
}

const formatDate = (dateStr: string) => {
  return dateStr ? dayjs(dateStr).format('YYYY-MM-DD HH:mm') : '-'
}

const handleAdd = () => {
  isEdit.value = false
  form.value = {
    title: '',
    content: '',
    type: 'primary',
    isPinned: false,
    isActive: true,
    publishTime: '',
    expireTime: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row: Announcement) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row: Announcement) => {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
      type: 'warning'
    })
    await announcementApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchAnnouncements()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除公告失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value && form.value.id) {
      await announcementApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await announcementApi.create(form.value)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    fetchAnnouncements()
  } catch (error) {
    console.error('保存公告失败:', error)
    ElMessage.error('保存失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchAnnouncements()
})
</script>

<style scoped>
.announcements-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
