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
              value-format="YYYY-MM-DD"
              @change="fetchLessons"
            />
            <el-select v-model="selectedCampus" placeholder="选择校区" style="width: 150px;" @change="fetchLessons">
              <el-option
                v-for="campus in campuses"
                :key="campus.id"
                :label="campus.name"
                :value="campus.id"
              />
            </el-select>
            <el-button type="primary" @click="fetchLessons" style="margin-left: 10px;">查询</el-button>
          </div>
        </div>
      </template>

      <div v-if="selectedCampus && selectedDate" class="info-bar">
        <el-tag>{{ selectedDate }}</el-tag>
        <el-tag type="info">{{ getCampusName(selectedCampus) }}</el-tag>
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
              <span class="lesson-teacher">{{ lesson.classInfo?.teacher?.name || '未知老师' }}</span>
              <span class="lesson-classroom">{{ lesson.classroom }}</span>
              <el-tag size="small" :type="getStatusType(lesson.status)">{{ lesson.status }}</el-tag>
            </div>
          </template>

          <el-table :data="lesson.attendances" border v-if="lesson.attendances?.length > 0">
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

          <el-empty v-else description="暂无学生签到记录" />
        </el-collapse-item>
      </el-collapse>

      <el-empty v-else-if="selectedCampus && selectedDate" description="该日期暂无课程" />
      <el-empty v-else description="请选择日期和校区查询课程" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { lessonApi } from '@/api/lesson'
import { campusApi } from '@/api/campus'
import dayjs from 'dayjs'

const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const selectedCampus = ref<number | null>(null)
const campuses = ref([])
const lessons = ref([])
const activeNames = ref([])

const getCampusName = (id: number) => {
  const campus = campuses.value.find((c: any) => c.id === id)
  return campus?.name || '未知校区'
}

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

const fetchCampuses = async () => {
  try {
    const res = await campusApi.getActive()
    campuses.value = res.data || []
    // 如果有校区，默认选择第一个
    if (campuses.value.length > 0 && !selectedCampus.value) {
      selectedCampus.value = campuses.value[0].id
    }
  } catch (error) {
    console.error('获取校区失败:', error)
  }
}

const fetchLessons = async () => {
  if (!selectedCampus.value || !selectedDate.value) {
    ElMessage.warning('请选择日期和校区')
    return
  }

  try {
    const res = await lessonApi.getByCampusAndDate(selectedCampus.value, selectedDate.value)
    lessons.value = (res.data || []).map((lesson: any) => ({
      ...lesson,
      attendances: (lesson.attendances || []).map((a: any) => ({
        ...a,
        newStatus: a.status,
        modifyReason: ''
      }))
    }))
    // 默认展开所有课程
    activeNames.value = lessons.value.map((l: any) => l.id)
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
  fetchCampuses().then(() => {
    // 校区加载完成后自动查询
    if (selectedCampus.value && selectedDate.value) {
      fetchLessons()
    }
  })
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

.lesson-teacher {
  color: #909399;
}

.lesson-classroom {
  color: #67C23A;
  font-size: 12px;
}
</style>
