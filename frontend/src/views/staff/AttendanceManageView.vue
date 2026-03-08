<template>
  <div class="attendance-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>签到管理</span>
          <div class="header-actions">
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择日期"
              style="margin-right: 10px;"
              @change="fetchLessons"
            />
            <el-select v-model="selectedCampus" placeholder="选择校区" style="width: 150px;">
              <el-option
                v-for="campus in campuses"
                :key="campus.id"
                :label="campus.name"
                :value="campus.id"
              />
            </el-select>
          </div>
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
              <span class="lesson-teacher">{{ lesson.classInfo.teacher.name }}</span>
              <el-tag size="small" :type="getStatusType(lesson.status)">{{ lesson.status }}</el-tag>
            </div>
          </template>
          
          <el-table :data="lesson.attendances" border>
            <el-table-column prop="student.name" label="学生姓名" width="120" />
            <el-table-column prop="student.parentPhone" label="家长电话" width="150" />
            <el-table-column label="当前状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getAttendanceType(row.status)">{{ row.status }}</el-tag>
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
                  placeholder="请输入修改理由"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="saveModify(row, lesson.id)">保存</el-button>
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
import { ElMessage } from 'element-plus'
import { lessonApi } from '@/api/lesson'
import { campusApi } from '@/api/campus'
import dayjs from 'dayjs'

const selectedDate = ref(new Date())
const selectedCampus = ref<number | null>(null)
const campuses = ref([])
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

const fetchCampuses = async () => {
  try {
    const res = await campusApi.getActive()
    campuses.value = res.data || []
  } catch (error) {
    console.error('获取校区失败:', error)
  }
}

const fetchLessons = async () => {
  if (!selectedCampus.value) return
  
  const date = dayjs(selectedDate.value).format('YYYY-MM-DD')
  
  try {
    const res = await lessonApi.getByCampusAndDate(selectedCampus.value, date)
    lessons.value = (res.data || []).map((lesson: any) => ({
      ...lesson,
      attendances: (lesson.attendances || []).map((a: any) => ({
        ...a,
        newStatus: a.status,
        modifyReason: ''
      }))
    }))
  } catch (error) {
    console.error('获取课节失败:', error)
  }
}

const saveModify = async (row: any, lessonId: number) => {
  if (row.newStatus === row.status) {
    ElMessage.warning('状态未改变')
    return
  }
  
  if (!row.modifyReason) {
    ElMessage.warning('请输入修改理由')
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

onMounted(() => {
  fetchCampuses()
})
</script>

<style scoped>
.attendance-manage {
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

.lesson-teacher {
  color: #909399;
}
</style>
