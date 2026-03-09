<template>
  <div class="my-attendance">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>签到管理</span>
          <div class="header-actions">
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择日期"
              size="small"
              value-format="YYYY-MM-DD"
              @change="fetchLessons"
            />
            <el-button type="primary" size="small" @click="fetchLessons" style="margin-left: 10px;">查询</el-button>
          </div>
        </div>
      </template>

      <div v-if="selectedDate" class="info-bar">
        <el-tag>{{ selectedDate }}</el-tag>
        <el-tag type="success">共 {{ lessons.length }} 节课</el-tag>
      </div>

      <el-collapse v-model="activeNames" v-if="lessons.length > 0">
        <el-collapse-item
          v-for="lesson in lessons"
          :key="lesson.id"
          :name="lesson.id"
        >
          <template #title>
            <div class="lesson-title">
              <span class="lesson-time">{{ lesson.startTime }} - {{ lesson.endTime }}</span>
              <span class="lesson-course">{{ lesson.classInfo?.course?.name || '未知课程' }}</span>
              <span class="lesson-classroom">{{ lesson.classroom }}</span>
              <el-tag size="small" :type="getStatusType(lesson.status)">{{ getStatusLabel(lesson.status) }}</el-tag>
            </div>
          </template>

          <div class="lesson-actions-bar" v-if="lesson.attendances?.length > 0">
            <el-button type="primary" size="small" @click="openBatchSignDialog(lesson)">批量签到</el-button>
          </div>

          <el-table :data="lesson.attendances" border v-if="lesson.attendances?.length > 0" height="350" style="width: 100%">
            <el-table-column prop="student.name" label="学生姓名" width="120" />
            <el-table-column prop="student.parentPhone" label="家长电话" width="150" />
            <el-table-column label="当前状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getAttendanceType(row.status)">{{ getAttendanceLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="修改状态" width="150">
              <template #default="{ row }">
                <el-select
                  v-model="row.newStatus"
                  size="small"
                  placeholder="修改状态"
                >
                  <el-option label="到课" value="PRESENT" />
                  <el-option label="请假" value="LEAVE" />
                  <el-option label="缺课" value="ABSENT" />
                  <el-option label="试课" value="TRIAL" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="修改理由">
              <template #default="{ row }">
                <el-input
                  v-model="row.modifyReason"
                  size="small"
                  placeholder="请输入修改理由（选填）"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="saveModify(row, lesson.id)">保存</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-else description="暂无学生签到记录" />
        </el-collapse-item>
      </el-collapse>

      <el-empty v-else-if="selectedDate" description="该日期暂无课程" />
      <el-empty v-else description="请选择日期查询课程" />
    </el-card>

    <!-- 批量签到对话框 -->
    <el-dialog v-model="batchSignDialogVisible" title="批量签到" width="700px">
      <div v-if="currentLesson" class="batch-sign-header">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="课程">{{ currentLesson.classInfo?.course?.name }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ currentLesson.startTime }} - {{ currentLesson.endTime }}</el-descriptions-item>
          <el-descriptions-item label="教室">{{ currentLesson.classroom }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="batch-sign-actions">
        <el-radio-group v-model="batchSignStatus" size="small">
          <el-radio-button value="PRESENT">到课</el-radio-button>
          <el-radio-button value="LEAVE">请假</el-radio-button>
          <el-radio-button value="ABSENT">缺课</el-radio-button>
          <el-radio-button value="TRIAL">试课</el-radio-button>
        </el-radio-group>
        <el-button type="primary" size="small" @click="applyBatchSignStatus">应用状态</el-button>
      </div>

      <el-table :data="batchSignList" border height="400" v-if="batchSignList.length > 0">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="student.name" label="学生姓名" width="120" />
        <el-table-column prop="student.parentPhone" label="家长电话" width="150" />
        <el-table-column label="当前状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAttendanceType(row.status)">{{ getAttendanceLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签到状态" width="150">
          <template #default="{ row }">
            <el-select v-model="row.newStatus" size="small" placeholder="选择状态">
              <el-option label="到课" value="PRESENT" />
              <el-option label="请假" value="LEAVE" />
              <el-option label="缺课" value="ABSENT" />
              <el-option label="试课" value="TRIAL" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="备注">
          <template #default="{ row }">
            <el-input v-model="row.remark" size="small" placeholder="备注（选填）" />
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="batchSignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveBatchSign">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { lessonApi } from '@/api/lesson'
import dayjs from 'dayjs'

const route = useRoute()
const authStore = useAuthStore()

const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const lessons = ref([])
const activeNames = ref([])

// 批量签到相关
const batchSignDialogVisible = ref(false)
const currentLesson = ref<any>(null)
const batchSignList = ref<any[]>([])
const batchSignStatus = ref('PRESENT')

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    'SCHEDULED': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'SCHEDULED': '已排课',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return labels[status] || status
}

const getAttendanceType = (status: string) => {
  const types: Record<string, string> = {
    'PENDING': 'info',
    'PRESENT': 'success',
    'ABSENT': 'danger',
    'LEAVE': 'warning',
    'TRIAL': 'primary'
  }
  return types[status] || 'info'
}

const getAttendanceLabel = (status: string) => {
  const labels: Record<string, string> = {
    'PENDING': '待签到',
    'PRESENT': '到课',
    'ABSENT': '缺课',
    'LEAVE': '请假',
    'TRIAL': '试课'
  }
  return labels[status] || status
}

const fetchLessons = async () => {
  if (!authStore.user?.teacherId) return

  if (!selectedDate.value) {
    ElMessage.warning('请选择日期')
    return
  }

  try {
    const res = await lessonApi.getByTeacherAndDate(authStore.user.teacherId, selectedDate.value)
    lessons.value = (res.data || []).map((lesson: any) => ({
      ...lesson,
      attendances: (lesson.attendances || []).map((a: any) => ({
        ...a,
        newStatus: a.status,
        modifyReason: ''
      }))
    }))

    // 如果有指定lessonId，展开对应面板
    const lessonId = route.query.lessonId
    if (lessonId) {
      activeNames.value = [Number(lessonId)]
    } else {
      activeNames.value = lessons.value.map((l: any) => l.id)
    }
  } catch (error) {
    console.error('获取课节失败:', error)
    ElMessage.error('获取课节失败')
  }
}

const saveModify = async (row: any, lessonId: number) => {
  if (row.newStatus === row.status) {
    ElMessage.warning('状态未改变')
    return
  }

  try {
    await lessonApi.modifyAttendance(lessonId, row.student.id, row.newStatus, row.remark, row.modifyReason)
    ElMessage.success('修改成功')
    row.status = row.newStatus
    row.modifyReason = ''
  } catch (error) {
    ElMessage.error('修改失败')
  }
}

// 打开批量签到对话框
const openBatchSignDialog = (lesson: any) => {
  currentLesson.value = lesson
  batchSignList.value = (lesson.attendances || []).map((a: any) => ({
    ...a,
    newStatus: a.status === 'PENDING' ? 'PRESENT' : a.status,
    remark: ''
  }))
  batchSignStatus.value = 'PRESENT'
  batchSignDialogVisible.value = true
}

// 应用批量状态
const applyBatchSignStatus = () => {
  batchSignList.value.forEach((item: any) => {
    item.newStatus = batchSignStatus.value
  })
  ElMessage.success('已应用到所有学生')
}

// 保存批量签到
const saveBatchSign = async () => {
  if (!currentLesson.value) return

  const changedList = batchSignList.value.filter((item: any) => item.newStatus !== item.status)

  if (changedList.length === 0) {
    ElMessage.warning('没有状态变更需要保存')
    return
  }

  try {
    let successCount = 0
    let failCount = 0

    for (const row of changedList) {
      try {
        await lessonApi.modifyAttendance(
          currentLesson.value.id,
          row.student.id,
          row.newStatus,
          row.remark,
          ''
        )
        successCount++
      } catch (error) {
        failCount++
      }
    }

    if (successCount > 0) {
      ElMessage.success(`批量签到完成，成功 ${successCount} 人${failCount > 0 ? `，失败 ${failCount} 人` : ''}`)
    }
    if (failCount > 0 && successCount === 0) {
      ElMessage.error('批量签到失败')
    }

    batchSignDialogVisible.value = false
    fetchLessons()
  } catch (error) {
    ElMessage.error('批量签到失败')
  }
}

onMounted(() => {
  fetchLessons()
})
</script>

<style scoped>
.my-attendance {
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

.info-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.lesson-title {
  display: flex;
  align-items: center;
  gap: 15px;
}

.lesson-time {
  font-weight: bold;
  color: #409EFF;
}

.lesson-course {
  flex: 1;
}

.lesson-classroom {
  color: #67C23A;
  font-size: 12px;
}

.lesson-actions-bar {
  margin-bottom: 10px;
  display: flex;
  justify-content: flex-end;
}

.batch-sign-header {
  margin-bottom: 20px;
}

.batch-sign-actions {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
