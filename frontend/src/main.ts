import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import './styles/table-common.css'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 创建并挂载 Pinia
const pinia = createPinia()
app.use(pinia)

// 初始化认证状态（在路由挂载前）
const authStore = useAuthStore()
authStore.initAuth()

app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
