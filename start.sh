#!/bin/bash

# 教务管理系统一键启动脚本
# 支持：本地启动(Windows 7/10/11, Linux, macOS) 和 Docker部署

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 打印带颜色的信息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 显示帮助信息
show_help() {
    echo "==================================="
    echo "教务管理系统启动脚本"
    echo "==================================="
    echo ""
    echo "用法: ./start.sh [选项]"
    echo ""
    echo "选项:"
    echo "  local       本地启动模式（默认）"
    echo "  docker      Docker部署模式"
    echo "  stop        停止所有服务"
    echo "  restart     重启服务"
    echo "  status      查看服务状态"
    echo "  logs        查看日志"
    echo "  help        显示帮助信息"
    echo ""
    echo "示例:"
    echo "  ./start.sh              # 本地启动"
    echo "  ./start.sh docker       # Docker部署"
    echo "  ./start.sh stop         # 停止服务"
    echo "==================================="
}

# 检测操作系统
detect_os() {
    local os="unknown"
    local version=""
    
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        os="linux"
        if [ -f /etc/os-release ]; then
            version=$(grep PRETTY_NAME /etc/os-release | cut -d'"' -f2)
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        os="macos"
        version=$(sw_vers -productVersion)
    elif [[ "$OSTYPE" == "cygwin" ]] || [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
        os="windows"
        # 尝试检测Windows版本
        if command -v wmic &> /dev/null; then
            version=$(wmic os get caption /value 2>/dev/null | grep -oP 'Caption=\K.*' || echo "Unknown Windows")
        elif [ -n "$OS" ]; then
            version="Windows $OS"
        fi
    elif [[ -n "$WINDIR" ]]; then
        os="windows"
        # 通过PowerShell检测Windows版本
        if command -v powershell &> /dev/null; then
            version=$(powershell -Command "[System.Environment]::OSVersion.VersionString" 2>/dev/null || echo "Windows")
        else
            version="Windows"
        fi
    fi
    
    echo "$os|$version"
}

# 检测Windows版本
detect_windows_version() {
    local version_info="$1"
    
    if echo "$version_info" | grep -qi "windows.*7"; then
        echo "win7"
    elif echo "$version_info" | grep -qi "windows.*10"; then
        echo "win10"
    elif echo "$version_info" | grep -qi "windows.*11"; then
        echo "win11"
    elif echo "$version_info" | grep -qi "6.1"; then
        echo "win7"
    elif echo "$version_info" | grep -qi "10.0"; then
        # Windows 10和11都是10.0，需要进一步判断
        local build=$(powershell -Command "[System.Environment]::OSVersion.Version.Build" 2>/dev/null)
        if [ -n "$build" ] && [ "$build" -ge 22000 ]; then
            echo "win11"
        else
            echo "win10"
        fi
    else
        echo "windows"
    fi
}

# 检查依赖
check_dependencies() {
    local os="$1"
    local missing_deps=()
    
    print_info "检查依赖环境..."
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        missing_deps+=("Java 17+")
    else
        local java_version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
        print_success "Java版本: $java_version"
    fi
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        if [ ! -f "$SCRIPT_DIR/backend/mvnw" ]; then
            missing_deps+=("Maven")
        fi
    else
        local mvn_version=$(mvn -version 2>&1 | head -n1)
        print_success "Maven已安装"
    fi
    
    # 检查Node.js
    if ! command -v node &> /dev/null; then
        missing_deps+=("Node.js 18+")
    else
        local node_version=$(node --version)
        print_success "Node.js版本: $node_version"
    fi
    
    # 检查npm
    if ! command -v npm &> /dev/null; then
        missing_deps+=("npm")
    else
        local npm_version=$(npm --version)
        print_success "npm版本: $npm_version"
    fi
    
    if [ ${#missing_deps[@]} -ne 0 ]; then
        print_error "缺少以下依赖:"
        for dep in "${missing_deps[@]}"; do
            echo "  - $dep"
        done
        echo ""
        print_info "请安装以上依赖后重试"
        exit 1
    fi
    
    print_success "所有依赖检查通过"
}

# 检查Docker环境
check_docker() {
    print_info "检查Docker环境..."
    
    if ! command -v docker &> /dev/null; then
        print_error "未安装Docker"
        echo "请访问 https://docs.docker.com/get-docker/ 安装Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        # 检查docker compose（新版）
        if ! docker compose version &> /dev/null; then
            print_error "未安装Docker Compose"
            echo "请访问 https://docs.docker.com/compose/install/ 安装Docker Compose"
            exit 1
        fi
    fi
    
    # 检查Docker是否运行
    if ! docker info &> /dev/null; then
        print_error "Docker服务未运行"
        echo "请启动Docker服务后重试"
        exit 1
    fi
    
    print_success "Docker环境检查通过"
}

# 本地启动 - 后端
start_backend() {
    print_info "正在启动后端服务..."
    
    cd "$SCRIPT_DIR/backend"
    
    # 检查mvnw是否存在且有执行权限
    if [ -f "mvnw" ]; then
        if [ ! -x "mvnw" ]; then
            chmod +x mvnw
        fi
        ./mvnw spring-boot:run > "$SCRIPT_DIR/backend.log" 2>&1 &
    else
        mvn spring-boot:run > "$SCRIPT_DIR/backend.log" 2>&1 &
    fi
    
    BACKEND_PID=$!
    echo $BACKEND_PID > "$SCRIPT_DIR/.backend.pid"
    
    cd "$SCRIPT_DIR"
    print_success "后端服务已启动 (PID: $BACKEND_PID)"
}

# 本地启动 - 前端
start_frontend() {
    print_info "正在启动前端服务..."
    
    cd "$SCRIPT_DIR/frontend"
    
    # 检查node_modules是否存在
    if [ ! -d "node_modules" ]; then
        print_info "首次运行，正在安装前端依赖..."
        npm install
    fi
    
    npm run dev > "$SCRIPT_DIR/frontend.log" 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > "$SCRIPT_DIR/.frontend.pid"
    
    cd "$SCRIPT_DIR"
    print_success "前端服务已启动 (PID: $FRONTEND_PID)"
}

# 本地启动模式
start_local() {
    echo "==================================="
    echo "教务管理系统 - 本地启动模式"
    echo "==================================="
    
    # 检测操作系统
    local os_info=$(detect_os)
    local os=$(echo "$os_info" | cut -d'|' -f1)
    local version=$(echo "$os_info" | cut -d'|' -f2)
    
    echo ""
    print_info "检测到操作系统: $version"
    
    # Windows版本特殊处理
    if [ "$os" == "windows" ]; then
        local win_ver=$(detect_windows_version "$version")
        case $win_ver in
            win7)
                print_info "Windows 7 系统，使用兼容模式启动"
                ;;
            win10)
                print_info "Windows 10 系统"
                ;;
            win11)
                print_info "Windows 11 系统"
                ;;
        esac
    fi
    
    echo ""
    
    # 检查依赖
    check_dependencies "$os"
    
    echo ""
    
    # 启动后端
    start_backend
    
    # 等待后端启动
    print_info "等待后端服务启动..."
    local max_attempts=30
    local attempt=0
    while [ $attempt -lt $max_attempts ]; do
        if curl -s http://localhost:8080/actuator/health &> /dev/null || \
           curl -s http://localhost:8080 &> /dev/null; then
            print_success "后端服务已就绪"
            break
        fi
        sleep 2
        attempt=$((attempt + 1))
        echo -n "."
    done
    
    if [ $attempt -eq $max_attempts ]; then
        print_warning "后端服务启动可能未完成，继续启动前端..."
    fi
    
    echo ""
    
    # 启动前端
    start_frontend
    
    # 等待前端启动
    sleep 5
    
    echo ""
    echo "==================================="
    print_success "服务启动完成！"
    echo "==================================="
    echo ""
    echo "访问地址:"
    echo "  前端: http://localhost:3000"
    echo "  后端: http://localhost:8080"
    echo ""
    echo "测试账号:"
    echo "  管理员: admin / admin123"
    echo "  教务:   staff1 / staff123"
    echo "  老师:   teacher1 / teacher123"
    echo ""
    echo "日志文件:"
    echo "  后端: $SCRIPT_DIR/backend.log"
    echo "  前端: $SCRIPT_DIR/frontend.log"
    echo ""
    echo "管理命令:"
    echo "  ./start.sh stop     # 停止服务"
    echo "  ./start.sh status   # 查看状态"
    echo "  ./start.sh logs     # 查看日志"
    echo "==================================="
    echo ""
    print_info "按 Ctrl+C 停止所有服务"
    echo ""
    
    # 保持脚本运行，直到用户中断
    while true; do
        sleep 1
    done
}

# Docker部署模式
start_docker() {
    echo "==================================="
    echo "教务管理系统 - Docker部署模式"
    echo "==================================="
    
    # 检查Docker环境
    check_docker
    
    echo ""
    print_info "正在构建并启动Docker容器..."
    
    # 检查docker-compose文件是否存在
    if [ ! -f "$SCRIPT_DIR/docker-compose.yml" ]; then
        print_error "未找到docker-compose.yml文件"
        exit 1
    fi
    
    # 停止旧容器
    print_info "停止旧容器..."
    docker-compose down 2>/dev/null || true
    
    # 构建并启动
    print_info "构建镜像并启动服务..."
    docker-compose up --build -d
    
    if [ $? -ne 0 ]; then
        print_error "Docker部署失败"
        exit 1
    fi
    
    echo ""
    print_info "等待服务启动..."
    sleep 10
    
    echo ""
    echo "==================================="
    print_success "Docker部署完成！"
    echo "==================================="
    echo ""
    echo "访问地址:"
    echo "  前端: http://localhost:3000"
    echo "  后端: http://localhost:8080"
    echo ""
    echo "Docker命令:"
    echo "  docker-compose ps       # 查看容器状态"
    echo "  docker-compose logs -f  # 查看日志"
    echo "  docker-compose down     # 停止服务"
    echo "  ./start.sh stop         # 停止服务"
    echo "==================================="
}

# 停止服务
stop_services() {
    print_info "正在停止服务..."
    
    # 停止本地服务
    if [ -f "$SCRIPT_DIR/.backend.pid" ]; then
        local pid=$(cat "$SCRIPT_DIR/.backend.pid")
        if kill -0 $pid 2>/dev/null; then
            kill $pid
            print_success "后端服务已停止"
        fi
        rm -f "$SCRIPT_DIR/.backend.pid"
    fi
    
    if [ -f "$SCRIPT_DIR/.frontend.pid" ]; then
        local pid=$(cat "$SCRIPT_DIR/.frontend.pid")
        if kill -0 $pid 2>/dev/null; then
            kill $pid
            print_success "前端服务已停止"
        fi
        rm -f "$SCRIPT_DIR/.frontend.pid"
    fi
    
    # 停止Docker容器
    if [ -f "$SCRIPT_DIR/docker-compose.yml" ]; then
        if docker-compose ps 2>/dev/null | grep -q "Up"; then
            docker-compose down
            print_success "Docker容器已停止"
        fi
    fi
    
    # 清理Java进程
    if command -v jps &> /dev/null; then
        local java_pids=$(jps | grep -E "(spring|boot)" | awk '{print $1}')
        if [ -n "$java_pids" ]; then
            echo "$java_pids" | xargs kill -9 2>/dev/null || true
        fi
    fi
    
    print_success "所有服务已停止"
}

# 查看服务状态
show_status() {
    echo "==================================="
    echo "服务状态"
    echo "==================================="
    echo ""
    
    # 检查本地服务
    local backend_running=false
    local frontend_running=false
    
    if [ -f "$SCRIPT_DIR/.backend.pid" ]; then
        local pid=$(cat "$SCRIPT_DIR/.backend.pid")
        if kill -0 $pid 2>/dev/null; then
            backend_running=true
        fi
    fi
    
    if [ -f "$SCRIPT_DIR/.frontend.pid" ]; then
        local pid=$(cat "$SCRIPT_DIR/.frontend.pid")
        if kill -0 $pid 2>/dev/null; then
            frontend_running=true
        fi
    fi
    
    echo "本地服务:"
    if $backend_running; then
        print_success "  后端: 运行中"
    else
        echo "  后端: 未运行"
    fi
    
    if $frontend_running; then
        print_success "  前端: 运行中"
    else
        echo "  前端: 未运行"
    fi
    
    echo ""
    echo "Docker容器:"
    if [ -f "$SCRIPT_DIR/docker-compose.yml" ]; then
        docker-compose ps 2>/dev/null || echo "  未运行"
    else
        echo "  未配置"
    fi
    
    echo ""
    echo "端口状态:"
    if command -v netstat &> /dev/null || command -v ss &> /dev/null; then
        local ports=("8080" "3000" "5432")
        for port in "${ports[@]}"; do
            if command -v netstat &> /dev/null; then
                if netstat -tuln 2>/dev/null | grep -q ":$port "; then
                    print_success "  端口 $port: 已占用"
                else
                    echo "  端口 $port: 空闲"
                fi
            elif command -v ss &> /dev/null; then
                if ss -tuln 2>/dev/null | grep -q ":$port "; then
                    print_success "  端口 $port: 已占用"
                else
                    echo "  端口 $port: 空闲"
                fi
            fi
        done
    fi
}

# 查看日志
show_logs() {
    echo "==================================="
    echo "查看日志"
    echo "==================================="
    echo ""
    
    echo "1) 后端日志"
    echo "2) 前端日志"
    echo "3) Docker日志"
    echo "4) 所有日志"
    echo ""
    read -p "请选择 [1-4]: " choice
    
    case $choice in
        1)
            if [ -f "$SCRIPT_DIR/backend.log" ]; then
                tail -f "$SCRIPT_DIR/backend.log"
            else
                print_error "后端日志文件不存在"
            fi
            ;;
        2)
            if [ -f "$SCRIPT_DIR/frontend.log" ]; then
                tail -f "$SCRIPT_DIR/frontend.log"
            else
                print_error "前端日志文件不存在"
            fi
            ;;
        3)
            if [ -f "$SCRIPT_DIR/docker-compose.yml" ]; then
                docker-compose logs -f
            else
                print_error "Docker未配置"
            fi
            ;;
        4)
            if [ -f "$SCRIPT_DIR/docker-compose.yml" ]; then
                docker-compose logs -f &
            fi
            if [ -f "$SCRIPT_DIR/backend.log" ]; then
                tail -f "$SCRIPT_DIR/backend.log" &
            fi
            if [ -f "$SCRIPT_DIR/frontend.log" ]; then
                tail -f "$SCRIPT_DIR/frontend.log" &
            fi
            wait
            ;;
        *)
            print_error "无效选择"
            ;;
    esac
}

# 主函数
main() {
    local command="${1:-local}"
    
    case "$command" in
        local|start)
            start_local
            ;;
        docker)
            start_docker
            ;;
        stop)
            stop_services
            ;;
        restart)
            stop_services
            sleep 2
            start_local
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "未知命令: $command"
            show_help
            exit 1
            ;;
    esac
}

# 捕获中断信号
trap 'echo ""; print_info "接收到中断信号，正在清理..."; stop_services; exit 0' INT TERM

# 执行主函数
main "$@"
