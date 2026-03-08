#!/bin/bash

echo "==================================="
echo "教务管理系统启动脚本"
echo "==================================="

# 启动后端
echo "正在启动后端服务..."
cd backend
if [ -f "mvnw" ]; then
    ./mvnw spring-boot:run &
else
    mvn spring-boot:run &
fi
BACKEND_PID=$!
cd ..

# 等待后端启动
echo "等待后端服务启动..."
sleep 10

# 启动前端
echo "正在启动前端服务..."
cd frontend
npm install
npm run dev &
FRONTEND_PID=$!
cd ..

echo ""
echo "==================================="
echo "服务启动完成！"
echo "后端地址: http://localhost:8080"
echo "前端地址: http://localhost:3000"
echo "==================================="
echo ""
echo "测试账号:"
echo "  管理员: admin / admin123"
echo "  教务: staff1 / staff123"
echo "  老师: teacher1 / teacher123"
echo ""
echo "按 Ctrl+C 停止服务"
echo "==================================="

# 等待用户中断
trap "kill $BACKEND_PID $FRONTEND_PID; exit" INT
wait
