<template>
  <el-container class="main-layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon size="28"><School /></el-icon>
        <span>教务管理系统</span>
      </div>
      
      <el-menu
        :default-active="$route.path"
        router
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        
        <!-- 老师菜单 -->
        <template v-if="authStore.isTeacher">
          <el-divider content-position="left" class="menu-divider">老师端</el-divider>
          <el-menu-item index="/my-schedule">
            <el-icon><Calendar /></el-icon>
            <span>我的课表</span>
          </el-menu-item>
          <el-menu-item index="/my-attendance">
            <el-icon><Check /></el-icon>
            <span>签到管理</span>
          </el-menu-item>
          <el-menu-item index="/travel-config">
            <el-icon><MapLocation /></el-icon>
            <span>路程配置</span>
          </el-menu-item>
          <el-menu-item index="/my-stats">
            <el-icon><TrendCharts /></el-icon>
            <span>个人统计</span>
          </el-menu-item>
        </template>
        
        <!-- 教务菜单 -->
        <template v-if="authStore.isAdmin || authStore.isStaff">
          <el-divider content-position="left" class="menu-divider">教务端</el-divider>
          <el-menu-item index="/students">
            <el-icon><User /></el-icon>
            <span>学生管理</span>
          </el-menu-item>
          <el-menu-item index="/classes">
            <el-icon><School /></el-icon>
            <span>班级管理</span>
          </el-menu-item>
          <el-menu-item index="/schedule">
            <el-icon><Calendar /></el-icon>
            <span>排课中心</span>
          </el-menu-item>
          <el-menu-item index="/attendance-manage">
            <el-icon><Edit /></el-icon>
            <span>签到管理</span>
          </el-menu-item>
          <el-menu-item index="/reports">
            <el-icon><Document /></el-icon>
            <span>报表中心</span>
          </el-menu-item>
        </template>
        
        <!-- 管理员菜单 -->
        <template v-if="authStore.isAdmin">
          <el-divider content-position="left" class="menu-divider">管理员</el-divider>
          <el-menu-item index="/campuses">
            <el-icon><OfficeBuilding /></el-icon>
            <span>校区管理</span>
          </el-menu-item>
          <el-menu-item index="/teachers">
            <el-icon><UserFilled /></el-icon>
            <span>老师管理</span>
          </el-menu-item>
          <el-menu-item index="/courses">
            <el-icon><Reading /></el-icon>
            <span>课程管理</span>
          </el-menu-item>
          <el-menu-item index="/users">
            <el-icon><Lock /></el-icon>
            <span>账号管理</span>
          </el-menu-item>
          <el-menu-item index="/global-reports">
            <el-icon><DataLine /></el-icon>
            <span>全局报表</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <breadcrumb />
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ authStore.userName }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const handleCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      authStore.logout()
      ElMessage.success('已退出登录')
    })
  } else if (command === 'profile') {
    // TODO: 打开个人信息对话框
  }
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
}

.sidebar {
  background-color: #304156;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  overflow-y: auto;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #1f2d3d;
}

.logo .el-icon {
  margin-right: 10px;
}

.sidebar-menu {
  border-right: none;
}

.menu-divider {
  margin: 10px 0;
  color: #909399;
}

.menu-divider :deep(.el-divider__text) {
  background-color: #304156;
  color: #909399;
  font-size: 12px;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: fixed;
  top: 0;
  right: 0;
  left: 220px;
  z-index: 100;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
}

.main-content {
  margin-top: 60px;
  margin-left: 220px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
}
</style>
