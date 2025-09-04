@echo off
chcp 65001 >nul
title SSLAB设备模拟器服务器

echo.
echo 🚀 SSLAB设备模拟器服务器启动脚本
echo ================================
echo.

echo 🔄 正在清理占用的端口...
taskkill /F /IM node.exe >nul 2>&1

echo 📂 切换到服务器目录...
cd /d "D:\CODE\SSLAB-HMI\Device-Simulator-Server"

echo.
echo 🚀 启动SSLAB设备模拟器服务器...
echo 服务器将在 http://localhost:8080 上运行
echo 按 Ctrl+C 可以停止服务器
echo.

node src/server.js

pause
