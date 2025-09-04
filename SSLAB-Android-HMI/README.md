# SSLAB-Android-HMI

SSLAB-AI实验室环境控制系统 - Android主控应用

## 项目概述

这是一个基于Android平台的实验室环境控制和教学管理系统，主要功能包括：

- **教学电源管理**: 教师电源设置、学生分组控制、一键参数同步
- **实验环境管理**: 温湿度监测、空气质量检测、设备控制
- **互动教学管理**: 在线答题、实时统计、学习效果分析
- **设备发现与管理**: 自动发现、扫码添加、状态监控、固件升级

## 技术架构

### 主要技术栈
- **Android**: Kotlin + Jetpack Compose
- **架构**: MVVM + Hilt依赖注入
- **网络**: Retrofit + WebSocket
- **数据库**: Room
- **UI**: Material Design 3 深蓝主题

### 项目结构
```
app/
├── src/main/java/com/sslab/hmi/
│   ├── MainActivity.kt                 # 主活动
│   ├── SSLabHMIApplication.kt         # 应用程序类
│   ├── ui/
│   │   ├── navigation/                # 导航管理
│   │   ├── screens/                   # 界面屏幕
│   │   │   ├── home/                  # 首页
│   │   │   ├── power/                 # 教学电源管理
│   │   │   ├── environment/           # 环境管理
│   │   │   ├── interactive/           # 互动教学
│   │   │   ├── device/               # 设备管理
│   │   │   └── settings/             # 系统设置
│   │   ├── components/               # 公共组件
│   │   └── theme/                    # 主题样式
│   ├── data/                         # 数据层
│   ├── domain/                       # 业务逻辑层
│   └── di/                          # 依赖注入配置
└── src/main/res/                     # 资源文件
```

## 核心功能

### 1. 教学电源管理 (TeachingPowerScreen)
- **三栏整合布局**: 低压电源设置 | 高压电源设置 | 学生电源控制
- **实时监控**: 电压、电流数据实时显示
- **一键同步**: 教师参数推送到所有学生设备
- **分组控制**: A/B/C/D组独立开关管理
- **安全保护**: 过载保护、电流限制

### 2. 设备发现与管理
- **自动发现**: mDNS协议自动发现ESP8266设备
- **扫码添加**: 二维码扫描添加新设备
- **状态监控**: 实时设备在线状态
- **OTA升级**: 固件远程升级管理

### 3. 深蓝主题设计
- 基于Material Design 3
- 深蓝色主色调，符合实验室风格
- 自适应明暗主题
- 清晰的信息层次和视觉反馈

## 开发状态

### ✅ 已完成
- [x] 项目架构搭建
- [x] 深蓝主题设计
- [x] 导航系统实现
- [x] 教学电源管理界面(核心功能)
- [x] 首页仪表盘
- [x] 基础界面框架

### 🚧 开发中
- [ ] 网络通信模块
- [ ] 设备发现服务
- [ ] 数据存储层
- [ ] 环境监测功能
- [ ] 交互教学模块

### 📋 待开发
- [ ] ESP8266设备固件
- [ ] AI功能集成
- [ ] 系统集成测试
- [ ] 用户培训文档

## 构建说明

### 环境要求
- Android Studio Hedgehog 2023.1.1+
- Kotlin 1.9.20+
- Android SDK 34
- 最低支持 Android 8.0 (API 26)

### 依赖库
```kotlin
// 核心依赖
implementation("androidx.compose.ui:ui:1.5.5")
implementation("com.google.dagger:hilt-android:2.48")
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("androidx.room:room-runtime:2.6.1")
implementation("com.journeyapps:zxing-android-embedded:4.3.0")
```

### 构建步骤
1. 克隆项目到本地
2. 用Android Studio打开项目
3. 等待Gradle同步完成
4. 连接Android设备或启动模拟器
5. 点击Run按钮运行应用

## 通信协议

### 设备发现协议
```json
{
  "device_type": "teaching_power|environment_monitor|lift_controller|device_controller",
  "device_id": "TP_001|EM_001|LC_001|DC_001",
  "ip_address": "192.168.1.100",
  "capabilities": ["low_voltage", "high_voltage", "student_groups"],
  "status": "online|offline|error",
  "group": "A|B|C|D"
}
```

### 参数同步协议
```json
{
  "action": "sync_power_settings",
  "teacher_settings": {
    "low_voltage": {
      "output_voltage": 15.0,
      "max_current": 2.5,
      "enable": true
    }
  },
  "target_groups": ["A", "B", "C", "D"]
}
```

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 联系方式

项目维护者: SSLAB开发团队
邮箱: [project-email]
项目地址: [repository-url]

---

*该项目是SSLAB-AI实验室环境控制系统的一部分，配合ESP8266硬件设备使用。*
