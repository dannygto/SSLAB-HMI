# SSLAB-Android-HMI API集成开发指南

## 📋 API集成当前状态

### ✅ 已完成的基础架构 (100%)
- **网络模块**: Retrofit + OkHttpClient 配置完成
- **依赖注入**: Hilt模块配置完成  
- **API接口定义**: 基础API接口定义完成
- **数据模型**: 设备数据模型定义完成
- **WebSocket**: WebSocket服务配置完成

### 🚧 待完成的API集成任务 (优先级排序)

## 第一优先级：API接口对接 (本周重点)

### 1.1 设备管理API集成
**目标**: 与Web平台设备管理功能完全对齐

#### 需要集成的Web平台API:
```bash
# Web平台已实现的完整API (http://localhost:8080)
GET    /api/devices                    # 获取所有设备
POST   /api/devices                    # 添加设备  
GET    /api/devices/{id}               # 获取特定设备
PUT    /api/devices/{id}               # 更新设备
DELETE /api/devices/{id}               # 删除设备
POST   /api/devices/{id}/control       # 控制设备
POST   /api/devices/control/batch      # 批量控制设备
POST   /api/devices/search             # 搜索设备

GET    /api/groups                     # 获取所有分组
POST   /api/groups                     # 创建分组
PUT    /api/groups/{id}                # 更新分组
DELETE /api/groups/{id}                # 删除分组

GET    /api/stats                      # 获取统计信息
GET    /api/system/health              # 系统健康状态
POST   /api/system/create-test-devices # 创建测试设备
```

#### Android需要实现的功能:
1. **设备列表管理**: 与Web平台设备管理页面功能对齐
2. **设备控制**: 支持6种SSLAB设备类型控制
3. **分组管理**: 设备分组分配和批量控制
4. **实时监控**: WebSocket实时数据同步
5. **设备发现**: mDNS设备自动发现

### 1.2 SSLAB设备类型支持
**需要支持的6种设备类型** (与Web平台完全一致):

#### A. 环境监测装置 (ENVIRONMENT_MONITOR)
- **监测数据**: 温度、湿度、CO2浓度、光照度
- **API端点**: `/api/devices/{id}/data` 
- **控制命令**: `getData`, `setThreshold`

#### B. 学生电源终端 (STUDENT_POWER_TERMINAL)  
- **监测数据**: 电压、电流、功率、安全状态
- **API端点**: `/api/devices/{id}/control`
- **控制命令**: `turnOn`, `turnOff`, `setVoltage`, `setCurrent`

#### C. 环境控制器 (ENVIRONMENT_CONTROLLER)
- **控制功能**: 供水系统、排风系统
- **API端点**: `/api/devices/{id}/control` 
- **控制命令**: `waterOn`, `waterOff`, `ventilationOn`, `ventilationOff`

#### D. 窗帘控制器 (CURTAIN_CONTROLLER)
- **控制功能**: 窗帘开关、位置控制
- **API端点**: `/api/devices/{id}/control`
- **控制命令**: `open`, `close`, `setPosition`

#### E. 灯光控制器 (LIGHTING_CONTROLLER)
- **控制功能**: 灯光开关、亮度调节、色温控制
- **API端点**: `/api/devices/{id}/control`
- **控制命令**: `turnOn`, `turnOff`, `setBrightness`, `setColorTemp`

#### F. 升降控制器 (LIFT_CONTROLLER)
- **控制功能**: 升降台高度控制、位置反馈
- **API端点**: `/api/devices/{id}/control`
- **控制命令**: `moveUp`, `moveDown`, `setHeight`

## 第二优先级：WebSocket实时通信 (下周重点)

### 2.1 实时数据订阅
**Web平台WebSocket功能** (已实现):
- **连接端点**: `ws://localhost:8080`
- **消息类型**: `device-data`, `device-status`, `device-added`, `device-removed`
- **数据格式**: JSON格式实时设备状态

#### Android需要实现:
1. **WebSocket连接管理**: 自动重连、心跳检测
2. **实时数据处理**: 设备状态实时更新
3. **UI实时刷新**: 界面数据实时同步
4. **事件通知**: 设备状态变化通知

### 2.2 设备发现协议
**Web平台发现服务** (已实现):
- **mDNS服务**: 自动设备发现
- **UDP广播**: 端口12345设备搜索
- **心跳检测**: 设备在线状态监控

#### Android需要实现:
1. **网络扫描**: WiFi网络设备扫描
2. **mDNS集成**: 自动发现ESP8266设备  
3. **设备认证**: 设备身份验证
4. **状态同步**: 设备在线状态监控

## 第三优先级：功能完善与优化 (后续2周)

### 3.1 用户体验优化
- **离线缓存**: 设备数据本地缓存
- **错误处理**: 网络异常处理
- **加载状态**: 操作反馈提示
- **数据同步**: 本地与服务端数据同步

### 3.2 性能优化
- **内存管理**: 大量设备数据优化
- **网络优化**: 请求合并和缓存
- **UI优化**: 列表滚动性能优化
- **电池优化**: 后台任务优化

## 📋 开发计划时间表

### 本周 (第1周): API基础对接
- [x] Web平台API文档分析和整理  
- [ ] Android API接口代码重构和完善
- [ ] 设备管理基础功能实现
- [ ] API调用测试和调试

### 下周 (第2周): 实时通信集成
- [ ] WebSocket连接管理实现
- [ ] 实时数据订阅和处理
- [ ] 设备发现协议集成
- [ ] 设备状态同步实现

### 第3周: 设备控制功能
- [ ] 6种SSLAB设备控制功能实现
- [ ] 设备分组管理功能
- [ ] 批量设备操作功能
- [ ] 设备监控界面完善

### 第4周: 集成测试和优化
- [ ] 与Web平台功能对齐测试
- [ ] 用户体验优化和错误处理
- [ ] 性能测试和优化
- [ ] 文档完善和交付准备

## 🔧 技术实施细节

### API BASE URL配置
```kotlin
// 当前配置 (需要调整为实际服务器地址)
const val BASE_URL = "http://localhost:8080/"

// 实际部署时可能的地址
const val PRODUCTION_URL = "http://192.168.1.100:8080/"
```

### 关键依赖库版本
```kotlin
// 网络库
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'

// WebSocket
implementation 'com.squareup.okhttp3:okhttp:4.11.0'

// 依赖注入
implementation 'com.google.dagger:hilt-android:2.47'
```

## 📊 成功指标

### API集成完成指标:
- [ ] **设备管理**: Android可完整管理所有设备 (CRUD操作)
- [ ] **设备控制**: Android可控制6种SSLAB设备类型
- [ ] **实时监控**: Android实时显示设备状态变化
- [ ] **分组管理**: Android支持设备分组和批量操作
- [ ] **设备发现**: Android自动发现网络中的ESP8266设备

### 功能对齐验证:
- [ ] **Web平台对比**: Android功能与Web平台100%对齐
- [ ] **API兼容性**: 所有Web平台API在Android正常工作
- [ ] **数据一致性**: Android和Web显示数据完全一致
- [ ] **操作同步**: Android操作在Web平台实时反映

---

## 📝 开发规范

### 代码结构规范
```
data/
├── api/           # API接口定义
├── model/         # 数据模型
├── repository/    # 数据仓库
└── network/       # 网络配置

ui/
├── screens/       # 界面屏幕
├── components/    # 公共组件
└── navigation/    # 导航管理
```

### API调用规范
```kotlin
// 统一错误处理
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val message: String) : Result<T>()
    data class Loading<T>(val isLoading: Boolean) : Result<T>()
}

// 统一API响应格式
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: String?
)
```

---

**下一步行动**: 
1. 立即开始API接口代码完善和测试
2. 验证与Web平台的API兼容性
3. 实现设备管理基础功能
4. 准备WebSocket实时通信集成

**预期成果**: 
在2周内完成Android客户端与Web设备管理平台的完整API对接，实现功能100%对齐。
