<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon blue">
            <el-icon><School /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.campusCount }}</div>
            <div class="stat-label">校区数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon green">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.teacherCount }}</div>
            <div class="stat-label">老师数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon orange">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.studentCount }}</div>
            <div class="stat-label">学生数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon purple">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.courseCount }}</div>
            <div class="stat-label">课程数量</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>今日课表</span>
              <el-button text @click="$router.push('/my-schedule')">查看更多</el-button>
            </div>
          </template>
          <el-table :data="todayLessons" v-loading="loading">
            <el-table-column prop="startTime" label="时间" width="100" />
            <el-table-column prop="classInfo.course.name" label="课程" />
            <el-table-column prop="classInfo.campus.name" label="校区" />
            <el-table-column prop="classroom" label="教室" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="goToAttendance(row)">
                  签到
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统公告</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(notice, index) in notices"
              :key="index"
              :type="notice.type"
              :timestamp="notice.time"
            >
              {{ notice.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { campusApi } from '@/api/campus'
import { teacherApi } from '@/api/teacher'
import { studentApi } from '@/api/student'
import { courseApi } from '@/api/course'
import { lessonApi } from '@/api/lesson'
import dayjs from 'dayjs'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const stats = ref({
  campusCount: 0,
  teacherCount: 0,
  studentCount: 0,
  courseCount: 0
})
const todayLessons = ref([])
const notices = ref([
  { type: 'primary', time: '2024-03-08', content: '系统升级完成，新增路程时间配置功能' },
  { type: 'success', time: '2024-03-07', content: '新学期排课工作已经开始，请各位老师及时确认课表' },
  { type: 'warning', time: '2024-03-05', content: '清明节放假安排已发布，请查看通知' }
])

const fetchStats = async () => {
  try {
    const [campuses, teachers, students, courses] = await Promise.all([
      campusApi.getAll(),
      teacherApi.getAll(),
      studentApi.getAll(),
      courseApi.getAll()
    ])
    
    stats.value = {
      campusCount: campuses.data?.length || 0,
      teacherCount: teachers.data?.length || 0,
      studentCount: students.data?.length || 0,
      courseCount: courses.data?.length || 0
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const fetchTodayLessons = async () => {
  if (!authStore.user?.teacherId) return
  
  loading.value = true
  try {
    const today = dayjs().format('YYYY-MM-DD')
    const res = await lessonApi.getByTeacherAndDate(authStore.user.teacherId, today)
    todayLessons.value = res.data || []
  } catch (error) {
    console.error('获取今日课表失败:', error)
  } finally {
    loading.value = false
  }
}

const goToAttendance = (lesson: any) => {
  router.push(`/my-attendance?lessonId=${lesson.id}`)
}

onMounted(() => {
  fetchStats()
  fetchTodayLessons()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  margin-right: 15px;
}

.stat-icon.blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.green {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-icon.orange {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.purple {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.mt-20 {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
