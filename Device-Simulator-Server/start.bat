@echo off
echo =====================================
echo SSLAB智能硬件设备模拟器启动脚本
echo =====================================
echo.

echo 正在检查Node.js环境...
node --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到Node.js，请先安装Node.js
    echo 下载地址: https://nodejs.org/
    pause
    exit /b 1
)

echo Node.js版本:
node --version
echo.

echo 正在检查项目依赖...
if not exist "node_modules" (
    echo [信息] 首次运行，正在安装依赖包...
    call npm install
    if errorlevel 1 (
        echo [错误] 依赖包安装失败
        pause
        exit /b 1
    )
    echo [成功] 依赖包安装完成
    echo.
)

echo 正在启动设备模拟器服务器...
echo.
echo ========================================
echo 服务器信息:
echo - Web界面: http://localhost:8080
echo - API接口: http://localhost:8080/api  
echo - 设备管理: http://localhost:8080/devices
echo - 分组管理: http://localhost:8080/groups
echo - 实时监控: http://localhost:8080/monitor
echo ========================================
echo.
echo 按 Ctrl+C 停止服务器
echo.

node src/server.js

echo.
echo 服务器已停止
pause
