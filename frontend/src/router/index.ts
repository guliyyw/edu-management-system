import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { public: true }
    },
    {
      path: '/',
      name: 'Layout',
      component: () => import('@/layouts/MainLayout.vue'),
      redirect: '/dashboard',
      children: [
        // 仪表盘
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: '仪表盘', icon: 'Odometer' }
        },
        // 老师端路由
        {
          path: 'my-schedule',
          name: 'MySchedule',
          component: () => import('@/views/teacher/MyScheduleView.vue'),
          meta: { title: '我的课表', icon: 'Calendar', roles: ['TEACHER'] }
        },
        {
          path: 'my-attendance',
          name: 'MyAttendance',
          component: () => import('@/views/teacher/MyAttendanceView.vue'),
          meta: { title: '签到管理', icon: 'Check', roles: ['TEACHER'] }
        },
        {
          path: 'travel-config',
          name: 'TravelConfig',
          component: () => import('@/views/teacher/TravelConfigView.vue'),
          meta: { title: '路程配置', icon: 'MapLocation', roles: ['TEACHER'] }
        },
        {
          path: 'my-stats',
          name: 'MyStats',
          component: () => import('@/views/teacher/MyStatsView.vue'),
          meta: { title: '个人统计', icon: 'TrendCharts', roles: ['TEACHER'] }
        },
        // 教务端路由
        {
          path: 'students',
          name: 'Students',
          component: () => import('@/views/staff/StudentsView.vue'),
          meta: { title: '学生管理', icon: 'User', roles: ['ADMIN', 'STAFF'] }
        },
        {
          path: 'classes',
          name: 'Classes',
          component: () => import('@/views/staff/ClassesView.vue'),
          meta: { title: '班级管理', icon: 'School', roles: ['ADMIN', 'STAFF'] }
        },
        {
          path: 'schedule',
          name: 'Schedule',
          component: () => import('@/views/staff/ScheduleView.vue'),
          meta: { title: '排课中心', icon: 'Calendar', roles: ['ADMIN', 'STAFF'] }
        },
        {
          path: 'attendance-manage',
          name: 'AttendanceManage',
          component: () => import('@/views/staff/AttendanceManageView.vue'),
          meta: { title: '签到管理', icon: 'Edit', roles: ['ADMIN', 'STAFF'] }
        },
        {
          path: 'reports',
          name: 'Reports',
          component: () => import('@/views/staff/ReportsView.vue'),
          meta: { title: '报表中心', icon: 'Document', roles: ['ADMIN', 'STAFF'] }
        },
        // 管理员路由
        {
          path: 'campuses',
          name: 'Campuses',
          component: () => import('@/views/admin/CampusesView.vue'),
          meta: { title: '校区管理', icon: 'OfficeBuilding', roles: ['ADMIN'] }
        },
        {
          path: 'teachers',
          name: 'Teachers',
          component: () => import('@/views/admin/TeachersView.vue'),
          meta: { title: '老师管理', icon: 'UserFilled', roles: ['ADMIN'] }
        },
        {
          path: 'courses',
          name: 'Courses',
          component: () => import('@/views/admin/CoursesView.vue'),
          meta: { title: '课程管理', icon: 'Reading', roles: ['ADMIN'] }
        },
        {
          path: 'users',
          name: 'Users',
          component: () => import('@/views/admin/UsersView.vue'),
          meta: { title: '账号管理', icon: 'Lock', roles: ['ADMIN'] }
        },
        {
          path: 'global-reports',
          name: 'GlobalReports',
          component: () => import('@/views/admin/GlobalReportsView.vue'),
          meta: { title: '全局报表', icon: 'DataLine', roles: ['ADMIN'] }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/login'
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.public) {
    next()
    return
  }
  
  if (!authStore.token) {
    next('/login')
    return
  }
  
  // 检查角色权限
  const requiredRoles = to.meta.roles as string[] | undefined
  if (requiredRoles && !requiredRoles.includes(authStore.userRole || '')) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router
