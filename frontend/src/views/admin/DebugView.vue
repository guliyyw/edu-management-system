<template>
  <div class="debug">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>调试工具</span>
          <el-tag type="danger">危险区域</el-tag>
        </div>
      </template>

      <el-alert
        title="警告：以下操作会删除数据，请谨慎使用！"
        type="warning"
        description="这些功能仅用于开发和调试环境，生产环境请勿使用。"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="danger-card">
            <template #header>
              <span>清空课节数据</span>
            </template>
            <p>删除所有排课记录和签到记录</p>
            <el-button type="danger" @click="clearLessons" :loading="clearing.lessons">
              清空课节
            </el-button>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card class="danger-card">
            <template #header>
              <span>清空班级数据</span>
            </template>
            <p>删除所有班级和学生关联</p>
            <el-button type="danger" @click="clearClasses" :loading="clearing.classes">
              清空班级
            </el-button>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card class="danger-card">
            <template #header>
              <span>清空学生数据</span>
            </template>
            <p>删除所有学生记录</p>
            <el-button type="danger" @click="clearStudents" :loading="clearing.students">
              清空学生
            </el-button>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card class="danger-card">
            <template #header>
              <span>清空老师数据</span>
            </template>
            <p>删除所有老师记录</p>
            <el-button type="danger" @click="clearTeachers" :loading="clearing.teachers">
              清空老师
            </el-button>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="danger-card">
            <template #header>
              <span>一键清空所有</span>
            </template>
            <p>删除所有业务数据（保留账号）</p>
            <el-button type="danger" @click="clearAll" :loading="clearing.all">
              清空所有
            </el-button>
          </el-card>
        </el-col>
      </el-row>

      <el-divider />

      <h3>操作日志</h3>
      <el-timeline v-if="logs.length > 0">
        <el-timeline-item
          v-for="(log, index) in logs"
          :key="index"
          :type="log.type"
          :timestamp="log.time"
        >
          {{ log.message }}
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无操作记录" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { debugApi } from '@/api/debug'

const clearing = ref({
  lessons: false,
  classes: false,
  students: false,
  teachers: false,
  all: false
})

const logs = ref<{ type: 'success' | 'danger'; message: string; time: string }[]>(
[])

const addLog = (type: 'success' | 'danger', message: string) => {
  logs.value.unshift({
    type,
    message,
    time: new Date().toLocaleString()
  })
}

const clearLessons = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有课节数据吗？这将删除所有排课记录和签到记录，且不可恢复！',
      '危险操作确认',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    clearing.value.lessons = true
    await debugApi.clearLessons()
    ElMessage.success('课节数据已清空')
    addLog('success', '清空了所有课节数据')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '清空失败')
      addLog('danger', '清空课节数据失败')
    }
  } finally {
    clearing.value.lessons = false
  }
}

const clearClasses = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有班级数据吗？这将删除所有班级和学生关联，且不可恢复！',
      '危险操作确认',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    clearing.value.classes = true
    await debugApi.clearClasses()
    ElMessage.success('班级数据已清空')
    addLog('success', '清空了所有班级数据')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '清空失败')
      addLog('danger', '清空班级数据失败')
    }
  } finally {
    clearing.value.classes = false
  }
}

const clearStudents = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有学生数据吗？这将删除所有学生记录，且不可恢复！',
      '危险操作确认',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    clearing.value.students = true
    await debugApi.clearStudents()
    ElMessage.success('学生数据已清空')
    addLog('success', '清空了所有学生数据')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '清空失败')
      addLog('danger', '清空学生数据失败')
    }
  } finally {
    clearing.value.students = false
  }
}

const clearTeachers = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有老师数据吗？这将删除所有老师记录，且不可恢复！',
      '危险操作确认',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    clearing.value.teachers = true
    await debugApi.clearTeachers()
    ElMessage.success('老师数据已清空')
    addLog('success', '清空了所有老师数据')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '清空失败')
      addLog('danger', '清空老师数据失败')
    }
  } finally {
    clearing.value.teachers = false
  }
}

const clearAll = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要一键清空所有业务数据吗？这将删除课节、班级、学生、老师、课程等所有数据（保留账号），且不可恢复！',
      '危险操作确认',
      {
        confirmButtonText: '确定清空所有',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    clearing.value.all = true
    await debugApi.clearAll()
    ElMessage.success('所有业务数据已清空')
    addLog('success', '一键清空了所有业务数据')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '清空失败')
      addLog('danger', '清空所有数据失败')
    }
  } finally {
    clearing.value.all = false
  }
}
</script>

<style scoped>
.debug {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.danger-card {
  text-align: center;
}

.danger-card p {
  color: #909399;
  font-size: 14px;
  margin-bottom: 15px;
  min-height: 40px;
}

.danger-card .el-button {
  width: 100%;
}
</style>
