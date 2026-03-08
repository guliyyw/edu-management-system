<template>
  <div class="my-attendance">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>签到管理</span>
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="选择日期"
            size="small"
            @change="fetchLessons"
          />
        </div>
      </template>
      
      <el-collapse v-model="activeNames">
        <el-collapse-item
          v-for="lesson in lessons"
          :key="lesson.id"
          :name="lesson.id"
        >
          <template #title>
            <div class="lesson-title">
              <span class="lesson-time">{{ lesson.startTime }} - {{ lesson.endTime }}</span>
              <span class="lesson-course">{{ lesson.classInfo.course.name }}</span>
              <el-tag size="small" :type="getStatusType(lesson.status)">{{ lesson.status }}</el-tag>
            </div>
          </template>
          
          <el-table :data="lesson.attendances" border>
            <el-table-column prop="student.name" label="学生姓名" width="120" />
            <el-table-column prop="student.parentPhone" label="家长电话" width="150" />
            <el-table-column label="状态" width="150">
              <template #default="{ row }">
                <el-select
                  v-model="row.status"
                  size="small"
                  @change="handleStatusChange(row, lesson.id)"
                >
                  <el-option label="待签到" value="PENDING" />
                  <el-option label="到课" value="PRESENT" />
                  <el-option label="请假" value="LEAVE" />
                  <el-option label="缺课" value="ABSENT" />
                  <el-option label="试课" value="TRIAL" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="备注">
              <template #default="{ row }">
                <el-input
                  v-model="row.remark"
                  size="small"
                  placeholder="请输入备注"
                  @blur="saveRemark(row, lesson.id)"
                />
              </template>
            </el-table-column>
            <el-table-column label="试课" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.isTrial" type="warning" size="small">是</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
        </el-collapse-item>
      </el-collapse>
      
      <el-empty v-if="lessons.length === 0" description="暂无课程" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { lessonApi } from '@/api/lesson'
import dayjs from 'dayjs'

const route = useRoute()
const authStore = useAuthStore()

const selectedDate = ref(new Date())
const lessons = ref([])
const activeNames = ref([])

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    'SCHEDULED': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return types[status] || 'info'
}

const fetchLessons = async () => {
  if (!authStore.user?.teacherId) return
  
  const date = dayjs(selectedDate.value).format('YYYY-MM-DD')
  
  try {
    const res = await lessonApi.getByTeacherAndDate(authStore.user.teacherId, date)
    lessons.value = res.data || []
    
    // 如果有指定lessonId，展开对应面板
    const lessonId = route.query.lessonId
    if (lessonId) {
      activeNames.value = [Number(lessonId)]
    }
  } catch (error) {
    console.error('获取课节失败:', error)
  }
}

const handleStatusChange = async (row: any, lessonId: number) => {
  try {
    await lessonApi.markAttendance(lessonId, row.student.id, row.status, row.remark)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

const saveRemark = async (row: any, lessonId: number) => {
  try {
    await lessonApi.markAttendance(lessonId, row.student.id, row.status, row.remark)
  } catch (error) {
    console.error('保存备注失败:', error)
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
</style>
