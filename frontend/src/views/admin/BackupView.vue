<template>
  <div class="backup">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据备份管理</span>
          <div>
            <el-button type="primary" @click="showConfigDialog" style="margin-right: 10px;">
              备份设置
            </el-button>
            <el-button type="warning" @click="showImportDialog" style="margin-right: 10px;">
              导入备份
            </el-button>
            <el-button type="success" @click="createBackup" :loading="creating">
              立即备份
            </el-button>
          </div>
        </div>
      </template>

      <!-- 数据库信息 -->
      <el-descriptions :column="1" border size="small" style="margin-bottom: 20px;">
        <el-descriptions-item label="数据库类型">{{ databaseInfo?.type || 'H2' }}</el-descriptions-item>
        <el-descriptions-item label="数据库路径">
          <el-tooltip :content="databaseInfo?.url || ''" placement="top">
            <span>{{ databaseInfo?.path || '加载中...' }}</span>
          </el-tooltip>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 备份配置信息 -->
      <el-descriptions :column="3" border size="small" style="margin-bottom: 20px;">
        <el-descriptions-item label="备份上限">{{ config?.maxBackups || 30 }} 个</el-descriptions-item>
        <el-descriptions-item label="备份间隔">{{ getIntervalLabel(config?.backupInterval) }}</el-descriptions-item>
        <el-descriptions-item label="备份时间">{{ config?.backupTime || '02:00' }}</el-descriptions-item>
        <el-descriptions-item label="自动备份">
          <el-tag :type="config?.enabled ? 'success' : 'danger'">
            {{ config?.enabled ? '已启用' : '已禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备份路径">{{ config?.backupPath || './backups' }}</el-descriptions-item>
        <el-descriptions-item label="当前备份数">{{ backups.length }} 个</el-descriptions-item>
      </el-descriptions>

      <!-- 备份列表 -->
      <el-table :data="backups" border v-loading="loading" height="calc(100vh - 320px)" style="width: 100%">
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="backupType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.backupType === 'MANUAL' ? 'primary' : 'info'">
              {{ row.backupType === 'MANUAL' ? '手动' : '自动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileSizeFormatted" label="大小" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="downloadBackup(row)">下载</el-button>
            <el-button type="warning" size="small" @click="restoreBackup(row)">恢复</el-button>
            <el-button type="danger" size="small" @click="deleteBackup(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 导入备份对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入备份" width="500px">
      <el-form label-width="120px">
        <el-form-item label="备份文件">
          <el-upload
            ref="uploadRef"
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            accept=".zip,.sql"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 .zip 和 .sql 格式的备份文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item v-if="selectedFile">
          <div class="selected-file">
            <el-icon><Document /></el-icon>
            <span>{{ selectedFile.name }}</span>
            <span class="file-size">({{ formatFileSize(selectedFile.size) }})</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmImport" :loading="importing" :disabled="!selectedFile">
          导入
        </el-button>
      </template>
    </el-dialog>

    <!-- 备份配置对话框 -->
    <el-dialog v-model="configDialogVisible" title="备份设置" width="500px">
      <el-form :model="configForm" ref="configFormRef" label-width="120px">
        <el-form-item label="自动备份">
          <el-switch v-model="configForm.enabled" />
        </el-form-item>
        <el-form-item label="备份上限" prop="maxBackups">
          <el-input-number v-model="configForm.maxBackups" :min="1" :max="100" style="width: 100%;" />
          <div class="form-tip">超过上限时自动删除最旧的备份</div>
        </el-form-item>
        <el-form-item label="备份间隔" prop="backupInterval">
          <el-select v-model="configForm.backupInterval" style="width: 100%;">
            <el-option label="每小时" value="HOURLY" />
            <el-option label="每天" value="DAILY" />
            <el-option label="每周" value="WEEKLY" />
            <el-option label="每月" value="MONTHLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="备份时间" prop="backupTime">
          <el-time-picker v-model="configForm.backupTime" format="HH:mm" value-format="HH:mm" style="width: 100%;" />
          <div class="form-tip">每天执行备份的具体时间</div>
        </el-form-item>
        <el-form-item label="备份路径" prop="backupPath">
          <el-input v-model="configForm.backupPath" placeholder="./backups">
            <template #append>
              <el-dropdown @command="handlePathCommand">
                <el-button>
                  常用路径<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="./backups">默认路径 (./backups)</el-dropdown-item>
                    <el-dropdown-item command="../backups">上级目录 (../backups)</el-dropdown-item>
                    <el-dropdown-item divided command="custom">自定义绝对路径...</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-input>
          <div class="form-tip">
            支持相对路径（如 ./backups）或绝对路径（如 D:/backups 或 /home/user/backups）
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, ArrowDown } from '@element-plus/icons-vue'
import { backupApi, type BackupRecord, type BackupConfig, type DatabaseInfo } from '@/api/backup'
import dayjs from 'dayjs'

const loading = ref(false)
const creating = ref(false)
const importing = ref(false)
const backups = ref<BackupRecord[]>([])
const config = ref<BackupConfig | null>(null)
const databaseInfo = ref<DatabaseInfo | null>(null)
const configDialogVisible = ref(false)
const importDialogVisible = ref(false)
const configFormRef = ref()
const uploadRef = ref()
const selectedFile = ref<File | null>(null)

const configForm = ref({
  maxBackups: 30,
  backupInterval: 'DAILY',
  backupTime: '02:00',
  enabled: true,
  backupPath: './backups'
})

const getIntervalLabel = (interval?: string) => {
  const labels: Record<string, string> = {
    'HOURLY': '每小时',
    'DAILY': '每天',
    'WEEKLY': '每周',
    'MONTHLY': '每月'
  }
  return labels[interval || 'DAILY'] || '每天'
}

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    'SUCCESS': 'success',
    'FAILED': 'danger',
    'PENDING': 'warning'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'SUCCESS': '成功',
    'FAILED': '失败',
    'PENDING': '进行中'
  }
  return labels[status] || status
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const fetchBackups = async () => {
  loading.value = true
  try {
    const res = await backupApi.getAll()
    backups.value = res.data || []
  } catch (error) {
    console.error('获取备份列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchConfig = async () => {
  try {
    const res = await backupApi.getConfig()
    config.value = res.data
    // 更新表单数据
    if (res.data) {
      configForm.value = {
        maxBackups: res.data.maxBackups || 30,
        backupInterval: res.data.backupInterval || 'DAILY',
        backupTime: res.data.backupTime || '02:00',
        enabled: res.data.enabled ?? true,
        backupPath: res.data.backupPath || './backups'
      }
    }
  } catch (error) {
    console.error('获取备份配置失败:', error)
  }
}

const fetchDatabaseInfo = async () => {
  try {
    const res = await backupApi.getDatabaseInfo()
    databaseInfo.value = res.data
  } catch (error) {
    console.error('获取数据库信息失败:', error)
  }
}

const createBackup = async () => {
  try {
    await ElMessageBox.confirm('确定要立即创建备份吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    creating.value = true
    await backupApi.create('手动备份 - ' + dayjs().format('YYYY-MM-DD HH:mm:ss'))
    ElMessage.success('备份创建成功')
    fetchBackups()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '备份创建失败')
    }
  } finally {
    creating.value = false
  }
}

const deleteBackup = async (row: BackupRecord) => {
  try {
    await ElMessageBox.confirm('确定要删除此备份吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await backupApi.delete(row.id)
    ElMessage.success('备份已删除')
    fetchBackups()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const restoreBackup = async (row: BackupRecord) => {
  if (row.status !== 'SUCCESS') {
    ElMessage.warning('只能恢复成功的备份')
    return
  }

  try {
    await ElMessageBox.confirm(
      '恢复备份将覆盖当前数据库中的所有数据，确定要继续吗？\n\n恢复后请重新启动系统！',
      '警告',
      {
        confirmButtonText: '确定恢复',
        cancelButtonText: '取消',
        type: 'danger'
      }
    )

    await backupApi.restore(row.id)
    ElMessage.success('数据库恢复成功，请重新启动系统')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '恢复失败')
    }
  }
}

const downloadBackup = async (row: BackupRecord) => {
  try {
    const res = await backupApi.download(row.id)
    const blob = new Blob([res.data], { type: 'application/zip' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '下载失败')
  }
}

const showConfigDialog = () => {
  configDialogVisible.value = true
}

const showImportDialog = () => {
  importDialogVisible.value = true
  selectedFile.value = null
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

const handleFileChange = (file: any) => {
  selectedFile.value = file.raw
}

const formatFileSize = (size: number) => {
  if (size < 1024) {
    return size + ' B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + ' KB'
  } else if (size < 1024 * 1024 * 1024) {
    return (size / (1024 * 1024)).toFixed(2) + ' MB'
  } else {
    return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
  }
}

const confirmImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择要导入的备份文件')
    return
  }

  try {
    await ElMessageBox.confirm(
      '导入备份将覆盖当前数据库中的所有数据，确定要继续吗？\n\n导入后请重新启动系统！',
      '警告',
      {
        confirmButtonText: '确定导入',
        cancelButtonText: '取消',
        type: 'danger'
      }
    )

    importing.value = true
    await backupApi.import(selectedFile.value)
    ElMessage.success('备份导入成功，请重新启动系统')
    importDialogVisible.value = false
    selectedFile.value = null
    if (uploadRef.value) {
      uploadRef.value.clearFiles()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '导入失败')
    }
  } finally {
    importing.value = false
  }
}

const saveConfig = async () => {
  try {
    await backupApi.updateConfig(configForm.value)
    ElMessage.success('配置保存成功')
    configDialogVisible.value = false
    fetchConfig()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  }
}

const handlePathCommand = (command: string) => {
  if (command === 'custom') {
    ElMessageBox.prompt('请输入自定义备份路径', '自定义路径', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: configForm.value.backupPath || '',
      inputPlaceholder: '例如: D:/backups 或 /home/user/backups'
    }).then(({ value }) => {
      if (value) {
        configForm.value.backupPath = value.trim()
      }
    }).catch(() => {
      // 用户取消
    })
  } else {
    configForm.value.backupPath = command
  }
}

onMounted(() => {
  fetchBackups()
  fetchConfig()
  fetchDatabaseInfo()
})
</script>

<style scoped>
.backup {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.selected-file {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.selected-file .file-size {
  color: #909399;
  font-size: 12px;
}
</style>
