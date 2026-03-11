# 教育管理系统 - 安卓手机端

基于 uni-app 开发的跨平台移动应用，支持安卓手机端访问教育管理系统。

## 功能特性

- 🔧 **服务器地址配置** - 首次启动时可自定义输入后端服务器地址
- 🔐 **用户登录** - 支持账号密码登录，JWT Token 认证
- 📊 **首页仪表盘** - 展示今日课程、出勤率、最新公告
- 📅 **课程表** - 查看每日课程安排，支持日期切换
- 📋 **考勤记录** - 查看个人考勤统计和记录
- 📢 **公告通知** - 查看系统公告，支持重要公告标记

## 技术栈

- **框架**: uni-app (Vue 3 + TypeScript)
- **状态管理**: Pinia
- **UI 组件**: uni-ui
- **构建工具**: Vite

## 项目结构

```
mobile-app/
├── src/
│   ├── api/              # API 接口封装
│   │   ├── auth.ts       # 认证相关接口
│   │   ├── announcement.ts # 公告接口
│   │   ├── lesson.ts     # 课程接口
│   │   └── attendance.ts # 考勤接口
│   ├── pages/            # 页面文件
│   │   ├── config/       # 服务器配置页
│   │   ├── login/        # 登录页
│   │   ├── home/         # 首页
│   │   ├── schedule/     # 课程表
│   │   ├── attendance/   # 考勤页
│   │   └── announcements/ # 公告页
│   ├── stores/           # Pinia 状态管理
│   │   ├── config.ts     # 配置状态（服务器地址）
│   │   └── auth.ts       # 认证状态
│   ├── utils/            # 工具函数
│   │   └── request.ts    # 请求封装
│   ├── static/           # 静态资源
│   ├── App.vue           # 应用入口
│   ├── main.ts           # 主入口文件
│   ├── manifest.json     # 应用配置
│   ├── pages.json        # 页面路由配置
│   └── uni-app.d.ts      # 类型声明
├── package.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

## 快速开始

### 1. 安装依赖

```bash
cd mobile-app
npm install
```

### 2. 开发调试（H5）

```bash
npm run dev:h5
```

### 3. 打包安卓应用

```bash
# 使用 HBuilderX 打开项目，选择 发行 -> 原生 App-云打包
# 或者使用 CLI 打包（需要配置安卓 SDK）
npm run build:app-android
```

## 后端地址配置说明

应用首次启动时会进入配置页面，用户需要输入后端服务器地址：

1. **本地测试**: 输入电脑的局域网 IP 地址，如 `192.168.1.100:8080`
2. **服务器部署**: 输入服务器的公网 IP 或域名，如 `api.example.com`
3. **自动补全**: 系统会自动添加 `http://` 前缀（如果没有协议头）

配置信息会保存在本地存储中，下次启动自动加载。

## API 接口规范

应用与后端通过 RESTful API 通信，接口规范与现有 Web 前端保持一致：

- 基础路径: `{serverUrl}/api`
- 认证方式: Bearer Token
- 响应格式: `{ code: number, message: string, data: any }`

## 与现有后端集成

本移动应用复用现有的 Spring Boot 后端服务：

- 认证接口: `/api/auth/login`
- 公告接口: `/api/announcements`
- 课程接口: `/api/lessons/*`
- 考勤接口: `/api/attendance/*`

确保后端服务已启动并配置了正确的 CORS 设置。

## 注意事项

1. **网络权限**: 安卓应用需要网络权限才能访问后端服务
2. **HTTPS**: 生产环境建议使用 HTTPS 协议
3. **CORS**: 后端需要配置跨域访问支持
4. **Token 过期**: 应用会自动处理 Token 过期，跳转到登录页

## 开发计划

- [ ] 添加消息推送功能
- [ ] 支持离线缓存
- [ ] 添加扫码签到功能
- [ ] 支持多语言
