# SSLAB智能硬件设备模拟器

## 项目简介

SSLAB智能硬件设备模拟器是一个功能完整的设备模拟服务器，专为SSLAB智能硬件实验室教学环境设计。它提供了一个完整的Web管理界面，可以模拟各种智能硬件设备，支持设备分组管理，并提供REST API和WebSocket实时通信接口。

## 主要功能

### 🚀 核心特性
- **多设备类型支持**: 智能开关、智能插座、LED控制器、环境传感器、运动传感器、电力仪表
- **设备分组管理**: 支持教室分组，如HX001（海心楼101）、SY001（实验楼201）
- **实时数据模拟**: 自动生成传感器数据和设备状态变化
- **Web管理界面**: 直观的设备管理和监控界面
- **REST API接口**: 完整的设备控制和管理API
- **WebSocket实时通信**: 实时设备状态推送
- **设备发现服务**: mDNS和UDP广播发现协议

### 🌐 Web界面功能
- **主控台**: 系统概览、快速操作、分组状态监控
- **设备管理**: 设备添加、删除、配置、控制
- **分组管理**: 教室分组配置和管理
- **实时监控**: 设备状态实时监控和数据可视化

## 快速开始

### 环境要求
- Node.js 16.0+ 
- npm 或 yarn
- 支持的操作系统: Windows, macOS, Linux

### 安装和启动

#### 方法1: 使用启动脚本（推荐-Windows）
```bash
# 双击运行启动脚本
start.bat
```

#### 方法2: 手动启动
```bash
# 1. 安装依赖
npm install

# 2. 启动服务器
npm start

# 开发模式（文件变更自动重启）
npm run dev
```

### 访问界面
启动成功后，打开浏览器访问：
- **主控台**: http://localhost:8080
- **设备管理**: http://localhost:8080/devices  
- **分组管理**: http://localhost:8080/groups
- **实时监控**: http://localhost:8080/monitor

## API接口文档

### 基础接口

#### 获取所有设备
```http
GET /api/devices
```

**查询参数**:
- `groupId`: 分组过滤
- `type`: 设备类型过滤  
- `status`: 状态过滤

**响应**:
```json
{
  "success": true,
  "data": [
    {
      "id": "device-uuid",
      "name": "HX001-智能开关-01",
      "type": "SMART_SWITCH", 
      "groupId": "HX001",
      "ipAddress": "192.168.1.101",
      "macAddress": "AA:BB:CC:DD:EE:01",
      "port": 80,
      "status": "ONLINE",
      "properties": {
        "isOn": false,
        "batteryLevel": 85,
        "signalStrength": 92
      },
      "metadata": {
        "manufacturer": "SSLAB",
        "model": "SSLAB-SW-001",
        "firmware": "1.0.0"
      }
    }
  ],
  "total": 1
}
```

#### 添加设备
```http
POST /api/devices
```

**请求体**:
```json
{
  "name": "新设备名称",
  "type": "SMART_SWITCH",
  "groupId": "HX001",
  "ipAddress": "192.168.1.100",
  "port": 80
}
```

#### 控制设备
```http
POST /api/devices/{deviceId}/control
```

**请求体**:
```json
{
  "command": "turnOn",
  "params": {
    "brightness": 80
  }
}
```

**支持的控制命令**:
- `turnOn`: 开启设备
- `turnOff`: 关闭设备  
- `toggle`: 切换状态
- `setBrightness`: 设置亮度（LED控制器）
- `setColor`: 设置颜色（LED控制器）
- `setSensitivity`: 设置灵敏度（运动传感器）

#### 分组设备控制
```http
POST /api/groups/{groupId}/control
```

#### 批量设备控制
```http
POST /api/devices/control/batch
```

**请求体**:
```json
{
  "deviceIds": ["device-1", "device-2"],
  "command": "turnOn",
  "params": {}
}
```

### 分组管理接口

#### 获取分组列表
```http
GET /api/groups
```

#### 添加分组
```http
POST /api/groups
```

**请求体**:
```json
{
  "groupId": "HX002",
  "name": "海心楼102实验室",
  "building": "海心楼",
  "floor": 1,
  "room": "102",
  "description": "物联网应用实验室"
}
```

### 统计信息接口

#### 获取设备统计
```http
GET /api/stats/devices
```

#### 获取系统健康状态
```http
GET /api/system/health
```

## WebSocket接口

### 连接
```javascript
const socket = io('http://localhost:8080');
```

### 事件监听

#### 接收设备列表
```javascript
socket.on('devices:list', (devices) => {
  console.log('设备列表:', devices);
});
```

#### 设备状态变化
```javascript
socket.on('device:deviceAdded', (device) => {
  console.log('设备已添加:', device);
});

socket.on('device:deviceRemoved', (device) => {
  console.log('设备已删除:', device);
});

socket.on('device:deviceControlled', (data) => {
  console.log('设备已控制:', data);
});
```

#### 统计信息更新
```javascript
socket.on('stats:update', (stats) => {
  console.log('统计信息:', stats);
});
```

### 发送命令

#### 获取设备列表
```javascript
socket.emit('devices:get');
```

#### 控制设备
```javascript
socket.emit('device:control', {
  id: 'device-id',
  command: 'turnOn',
  params: {}
});
```

#### 添加设备
```javascript
socket.emit('device:add', {
  name: '新设备',
  type: 'SMART_SWITCH',
  groupId: 'HX001'
});
```

## 设备类型说明

### 智能开关 (SMART_SWITCH)
- **属性**: isOn, switchCount, batteryLevel, signalStrength
- **控制**: turnOn, turnOff, toggle

### 智能插座 (SMART_OUTLET)  
- **属性**: isOn, voltage, current, power, energy
- **控制**: turnOn, turnOff, toggle

### LED控制器 (LED_CONTROLLER)
- **属性**: isOn, brightness, color, mode
- **控制**: turnOn, turnOff, setBrightness, setColor

### 环境传感器 (ENVIRONMENT_SENSOR)
- **属性**: temperature, humidity, airQuality, pressure
- **控制**: 仅读取，无控制命令

### 运动传感器 (MOTION_SENSOR)
- **属性**: motionDetected, lastMotionTime, sensitivity
- **控制**: setSensitivity

### 电力仪表 (POWER_METER)
- **属性**: voltage, current, power, energy, powerFactor
- **控制**: 仅读取，无控制命令

## 设备发现协议

### UDP广播发现
- **端口**: 12345
- **协议**: JSON over UDP
- **广播间隔**: 30秒

#### 发现请求格式
```json
{
  "type": "discover",
  "groupId": "HX001",
  "deviceType": "SMART_SWITCH"
}
```

#### 发现响应格式
```json
{
  "type": "discovery_response",
  "timestamp": "2024-01-01T00:00:00.000Z",
  "devices": [...],
  "total": 5
}
```

## 分组配置说明

### 默认分组
系统预配置了以下教室分组：

- **HX001**: 海心楼101智能硬件实验室
- **SY001**: 实验楼201电子技术实验室

### 分组ID格式
- 格式: `{楼栋代码}{楼层}{房间号}`
- 示例: HX001（海心楼1层01室）、SY201（实验楼2层01室）

## 开发和扩展

### 项目结构
```
Device-Simulator-Server/
├── src/
│   ├── server.js              # 服务器主入口
│   ├── device/
│   │   ├── DeviceManager.js   # 设备管理器
│   │   └── VirtualDevice.js   # 虚拟设备类
│   ├── api/
│   │   └── ApiRouter.js       # REST API路由
│   ├── websocket/
│   │   └── WebSocketHandler.js # WebSocket处理器
│   └── discovery/
│       └── DiscoveryService.js # 设备发现服务
├── public/
│   ├── index.html             # 主控台页面
│   ├── devices.html           # 设备管理页面
│   ├── groups.html            # 分组管理页面
│   └── monitor.html           # 实时监控页面
├── package.json               # 项目配置
├── start.bat                  # Windows启动脚本
└── README.md                  # 说明文档
```

### 添加新设备类型

1. 在 `VirtualDevice.js` 中的 `DeviceType` 枚举添加新类型
2. 在 `initializeProperties()` 方法中添加类型特定的属性
3. 在 `control()` 方法中添加控制逻辑
4. 更新Web界面的设备类型选项

### 自定义数据模拟
在 `VirtualDevice.js` 的 `updateSensorData()` 方法中自定义数据生成逻辑。

## 配置说明

### 服务器配置
- **端口**: 8080 (可通过环境变量 PORT 修改)
- **UDP发现端口**: 12345
- **WebSocket**: 自动启用

### 环境变量
```bash
PORT=8080                    # 服务器端口
DISCOVERY_PORT=12345         # 设备发现端口
LOG_LEVEL=info              # 日志级别
```

## 常见问题

### Q: 为什么设备无法发现？
A: 检查UDP端口12345是否被占用，确保防火墙允许UDP通信。

### Q: Web界面无法访问？
A: 检查端口8080是否被占用，确认服务器正常启动。

### Q: 设备控制失败？
A: 确认设备状态为ONLINE，检查控制命令格式是否正确。

### Q: 如何持久化设备配置？
A: 当前版本设备配置保存在内存中，重启后会重新初始化。可扩展添加数据库支持。

## 许可证

MIT License

## 技术支持

如有问题或建议，请联系SSLAB技术支持团队。

---

**SSLAB智能硬件实验室**  
*让智能硬件教学更简单*
