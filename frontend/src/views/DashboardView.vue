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
            <div class="stat-value">{{ stats.classCount }}</div>
            <div class="stat-label">班级数量</div>
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
              <el-button v-if="isAdmin || isStaff" text @click="$router.push('/announcements')">
                管理公告
              </el-button>
            </div>
          </template>
          <el-timeline v-loading="noticeLoading">
            <el-timeline-item
              v-for="notice in notices"
              :key="notice.id"
              :type="notice.type"
              :timestamp="formatDate(notice.createdAt)"
            >
              <el-tooltip :content="notice.content" placement="top" :show-after="500">
                <span class="notice-title">{{ notice.title }}</span>
              </el-tooltip>
              <el-tag v-if="notice.isPinned" size="small" type="danger" class="ml-5">置顶</el-tag>
            </el-timeline-item>
            <el-empty v-if="notices.length === 0" description="暂无公告" :image-size="60" />
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { campusApi } from '@/api/campus'
import { teacherApi } from '@/api/teacher'
import { studentApi } from '@/api/student'
import { classApi } from '@/api/class'
import { lessonApi } from '@/api/lesson'
import { announcementApi, type Announcement } from '@/api/announcement'
import dayjs from 'dayjs'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const stats = ref({
  campusCount: 0,
  teacherCount: 0,
  studentCount: 0,
  classCount: 0
})
const todayLessons = ref([])
const notices = ref<Announcement[]>([])
const noticeLoading = ref(false)

const fetchStats = async () => {
  try {
    const role = authStore.user?.role
    
    if (role === 'ADMIN' || role === 'STAFF') {
      // 管理员和教务可以看到所有统计数据
      const [campuses, teachers, students, classes] = await Promise.all([
        campusApi.getAll(),
        teacherApi.getAll(),
        studentApi.getAll(),
        classApi.getAll()
      ])

      stats.value = {
        campusCount: campuses.data?.length || 0,
        teacherCount: teachers.data?.length || 0,
        studentCount: students.data?.length || 0,
        classCount: classes.data?.length || 0
      }
    } else if (role === 'TEACHER' && authStore.user?.teacherId) {
      // 老师只能看到与自己相关的统计数据
      const [campuses, teacherClasses] = await Promise.all([
        campusApi.getAll(),
        classApi.getByTeacher(authStore.user.teacherId)
      ])
      
      // 获取老师的学生数量
      const classList = teacherClasses.data || []
      const studentIds = new Set()
      classList.forEach((cls: any) => {
        cls.students?.forEach((s: any) => {
          if (s.student?.id) studentIds.add(s.student.id)
        })
      })

      stats.value = {
        campusCount: campuses.data?.length || 0,
        teacherCount: 1, // 老师自己
        studentCount: studentIds.size,
        classCount: classList.length
      }
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const fetchTodayLessons = async () => {
  loading.value = true
  try {
    const today = dayjs().format('YYYY-MM-DD')
    let res
    
    if (authStore.user?.teacherId) {
      // 老师角色 - 查看自己的课表
      res = await lessonApi.getByTeacherAndDate(authStore.user.teacherId, today)
    } else if (authStore.user?.role === 'ADMIN' || authStore.user?.role === 'STAFF') {
      // 管理员或教务角色 - 查看所有今日课表
      res = await lessonApi.getAll()
      // 过滤出今日的课程
      if (res.data) {
        res.data = res.data.filter((lesson: any) => lesson.date === today)
      }
    } else {
      todayLessons.value = []
      return
    }
    
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

const fetchNotices = async () => {
  noticeLoading.value = true
  try {
    const res = await announcementApi.getTop5()
    notices.value = res.data || []
  } catch (error) {
    console.error('获取公告失败:', error)
  } finally {
    noticeLoading.value = false
  }
}

const formatDate = (dateStr: string) => {
  return dayjs(dateStr).format('YYYY-MM-DD')
}

const isAdmin = computed(() => authStore.user?.role === 'ADMIN')
const isStaff = computed(() => authStore.user?.role === 'STAFF')

onMounted(() => {
  fetchStats()
  fetchTodayLessons()
  fetchNotices()
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

.notice-title {
  cursor: pointer;
  color: #303133;
}

.notice-title:hover {
  color: #409eff;
}

.ml-5 {
  margin-left: 5px;
}
</style>
