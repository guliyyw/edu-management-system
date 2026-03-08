# 教务管理系统

## 项目概述

一个完整的K12/培训机构教务管理系统，支持多校区排课、老师跨校区路程管理、精准课时计费。

## 技术栈

- **后端**: Java Spring Boot 3.2 + JPA + PostgreSQL/H2
- **前端**: Vue 3 + TypeScript + Element Plus + Pinia
- **安全**: JWT Token + Spring Security

## 项目结构

```
edu-management-system/
├── backend/          # Spring Boot 后端项目
│   ├── src/main/java/com/edu/management/
│   │   ├── config/       # 配置类
│   │   ├── controller/   # API控制器
│   │   ├── dto/          # 数据传输对象
│   │   ├── entity/       # 实体类
│   │   ├── enums/        # 枚举类
│   │   ├── repository/   # 数据访问层
│   │   ├── security/     # 安全配置
│   │   └── service/      # 业务逻辑层
│   └── src/main/resources/
│       └── application.yml
└── frontend/         # Vue 3 前端项目
    ├── src/
    │   ├── api/        # API接口
    │   ├── components/ # 组件
    │   ├── layouts/    # 布局
    │   ├── router/     # 路由
    │   ├── stores/     # 状态管理
    │   └── views/      # 页面视图
    └── package.json
```

## 核心功能

### 1. 智能排课系统
- 学生维度排课，自动展示已排课程时间
- 老师跨校区冲突检测（动态计算路程时间）
- 可视化课表：周视图/日视图，拖拽调课
- 连堂处理标记
- 试课标记支持

### 2. 路程时间配置
- 老师自设校区间路程时间
- 支持按日期设置不同路程（工作日/周末）
- 动态计算跨校区排课冲突

### 3. 签到系统
- 老师查看今日课表
- 逐个标记学生状态（到课/请假/缺课/试课）
- 教务可修改历史记录并通知老师
- 请假/缺课必须填写备注

### 4. 报表与导出
- 学生课时消耗报表
- 老师授课统计
- 财务流水报表
- Excel导出功能

## 用户角色

| 角色 | 权限范围 | 核心操作 |
|------|----------|----------|
| 管理员 | 全系统 | 创建校区/老师/学生/教务账号、查看所有报表、配置系统参数 |
| 教务 | 指定校区 | 排课、调课、修改老师历史签到记录、查看本校区报表 |
| 老师 | 仅自己 | 查看个人课表、标记学生上课状态、设置跨校区路程时间、查看个人课时统计 |

## 快速开始

### 后端启动

```bash
cd backend
./mvnw spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 http://localhost:3000 启动

### 测试账号

- **管理员**: admin / admin123
- **教务**: staff1 / staff123
- **老师**: teacher1 / teacher123

## API 文档

### 认证相关
- `POST /api/auth/login` - 登录
- `POST /api/auth/logout` - 退出

### 校区管理
- `GET /api/campuses` - 获取所有校区
- `POST /api/campuses` - 创建校区
- `PUT /api/campuses/{id}` - 更新校区
- `DELETE /api/campuses/{id}` - 删除校区

### 老师管理
- `GET /api/teachers` - 获取所有老师
- `POST /api/teachers` - 创建老师
- `PUT /api/teachers/{id}` - 更新老师
- `DELETE /api/teachers/{id}` - 删除老师

### 学生管理
- `GET /api/students` - 获取所有学生
- `POST /api/students` - 创建学生
- `PUT /api/students/{id}` - 更新学生
- `DELETE /api/students/{id}` - 删除学生

### 课程管理
- `GET /api/courses` - 获取所有课程
- `POST /api/courses` - 创建课程
- `PUT /api/courses/{id}` - 更新课程
- `DELETE /api/courses/{id}` - 删除课程

### 班级管理
- `GET /api/classes` - 获取所有班级
- `POST /api/classes` - 创建班级
- `PUT /api/classes/{id}` - 更新班级
- `DELETE /api/classes/{id}` - 删除班级
- `POST /api/classes/{id}/students/{studentId}` - 添加学生到班级
- `DELETE /api/classes/{id}/students/{studentId}` - 从班级移除学生

### 课节管理
- `GET /api/lessons/class/{classId}` - 获取班级课节
- `POST /api/lessons` - 创建课节
- `PUT /api/lessons/{id}` - 更新课节
- `DELETE /api/lessons/{id}` - 删除课节
- `POST /api/lessons/generate` - 批量生成课节
- `POST /api/lessons/{id}/attendance/{studentId}` - 签到
- `POST /api/lessons/{id}/attendance/{studentId}/modify` - 修改签到记录

### 路程时间配置
- `GET /api/travel-times/teacher/{teacherId}` - 获取老师路程配置
- `POST /api/travel-times` - 创建路程配置
- `PUT /api/travel-times/{id}` - 更新路程配置
- `DELETE /api/travel-times/{id}` - 删除路程配置

## 数据库模型

### 核心实体
- **User** - 用户账号
- **Campus** - 校区
- **Teacher** - 老师
- **Student** - 学生
- **Course** - 课程
- **ClassEntity** - 班级
- **ClassStudent** - 班级学生关联（含试课标记）
- **Lesson** - 课节
- **LessonAttendance** - 课节签到记录
- **TravelTime** - 路程时间配置

## 开发计划

### 第一阶段（已完成）
- [x] 基础数据管理（校区/老师/学生/课程）
- [x] 路程时间配置系统
- [x] 排课系统（含动态跨校区冲突检测）
- [x] 老师签到与课时扣减
- [x] 基础报表

### 第二阶段（待开发）
- [ ] 教务修改记录与通知
- [ ] 高级报表与Excel导出
- [ ] 试课转正流程
- [ ] 操作日志审计

## 许可证

MIT License
