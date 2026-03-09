<template>
  <div class="users">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>账号管理</span>
          <el-button type="primary" size="small" @click="showAddDialog">添加账号</el-button>
        </div>
      </template>
      
      <el-table :data="users" border v-loading="loading" height="calc(100vh - 220px)" style="width: 100%">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">{{ getRoleName(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最后登录" width="180">
          <template #default="{ row }">
            {{ row.lastLoginAt ? formatDate(row.lastLoginAt) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editUser(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteUser(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑账号' : '添加账号'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="password" v-if="isEdit">
          <el-input v-model="form.password" type="password" placeholder="不修改请留空" />
          <div class="form-tip">留空表示不修改密码</div>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="选择角色" style="width: 100%;" @change="onRoleChange">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="教务" value="STAFF" />
            <el-option label="老师" value="TEACHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="关联老师" prop="teacherId" v-if="form.role === 'TEACHER'">
          <el-select v-model="form.teacherId" placeholder="选择老师" style="width: 100%;">
            <el-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="teacher.name"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关联校区" prop="campusId" v-if="form.role === 'STAFF'">
          <el-select v-model="form.campusId" placeholder="选择校区" style="width: 100%;">
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
        <el-button type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api/user'
import { teacherApi } from '@/api/teacher'
import { campusApi } from '@/api/campus'
import dayjs from 'dayjs'

const loading = ref(false)
const users = ref([])
const teachers = ref([])
const campuses = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = ref({
  id: null as number | null,
  username: '',
  password: '',
  realName: '',
  role: 'STAFF',
  phone: '',
  teacherId: null as number | null,
  campusId: null as number | null
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: !isEdit.value, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const getRoleName = (role: string) => {
  const names: Record<string, string> = {
    'ADMIN': '管理员',
    'STAFF': '教务',
    'TEACHER': '老师'
  }
  return names[role] || role
}

const getRoleType = (role: string) => {
  const types: Record<string, string> = {
    'ADMIN': 'danger',
    'STAFF': 'warning',
    'TEACHER': 'success'
  }
  return types[role] || 'info'
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await userApi.getAll()
    users.value = res.data || []
  } catch (error: any) {
    console.error('获取用户列表失败:', error)
    ElMessage.error(error.response?.data?.message || '获取用户列表失败')
    users.value = []
  } finally {
    loading.value = false
  }
}

const fetchOptions = async () => {
  try {
    const [teachersRes, campusesRes] = await Promise.all([
      teacherApi.getActive(),
      campusApi.getActive()
    ])
    teachers.value = teachersRes.data || []
    campuses.value = campusesRes.data || []
  } catch (error) {
    console.error('获取选项失败:', error)
  }
}

const onRoleChange = () => {
  form.value.teacherId = null
  form.value.campusId = null
}

const showAddDialog = () => {
  isEdit.value = false
  form.value = {
    id: null,
    username: '',
    password: '',
    realName: '',
    role: 'STAFF',
    phone: '',
    teacherId: null,
    campusId: null
  }
  dialogVisible.value = true
}

const editUser = (row: any) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    username: row.username,
    password: '',
    realName: row.realName,
    role: row.role,
    phone: row.phone,
    teacherId: row.teacherId,
    campusId: row.campusId
  }
  dialogVisible.value = true
}

const saveUser = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    const data = { ...form.value }
    // 编辑模式下如果密码为空，删除该字段（表示不修改密码）
    if (isEdit.value && !data.password) {
      delete (data as any).password
    }

    if (isEdit.value && form.value.id) {
      await userApi.update(form.value.id, data)
    } else {
      await userApi.create(data)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
    dialogVisible.value = false
    fetchUsers()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || (isEdit.value ? '更新失败' : '添加失败'))
  }
}

const deleteUser = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除此账号吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await userApi.delete(id)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

onMounted(() => {
  fetchUsers()
  fetchOptions()
})
</script>

<style scoped>
.users {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
