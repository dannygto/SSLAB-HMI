const express = require('express');
const { DeviceType } = require('../device/VirtualDevice');

/**
 * API路由器
 * 提供设备管理的REST API接口
 */
class ApiRouter {
    constructor(deviceManager, server = null) {
        this.deviceManager = deviceManager;
        this.server = server; // 服务器实例，用于获取连接数等信息
        this.router = express.Router();
        this.setupRoutes();
    }
    
    /**
     * 设置路由
     */
    setupRoutes() {
        // 设备相关路由
        this.router.get('/devices', this.getAllDevices.bind(this));
        this.router.get('/devices/stats', this.getDeviceStats.bind(this));
        this.router.post('/devices', this.addDevice.bind(this));
        this.router.get('/devices/:id', this.getDevice.bind(this));
        this.router.put('/devices/:id', this.updateDevice.bind(this));
        this.router.delete('/devices/:id', this.removeDevice.bind(this));
        this.router.post('/devices/:id/control', this.controlDevice.bind(this));
        this.router.put('/devices/:id/group', this.assignDeviceToGroup.bind(this));
        
        // 分组设备路由
        this.router.get('/groups/:groupId/devices', this.getDevicesByGroup.bind(this));
        this.router.post('/groups/:groupId/control', this.controlDevicesByGroup.bind(this));
        
        // 批量操作路由
        this.router.post('/devices/control/batch', this.controlDevicesBatch.bind(this));
        this.router.post('/devices/search', this.searchDevices.bind(this));
        
        // 统计信息路由
        this.router.get('/stats', this.getStats.bind(this));
        this.router.get('/stats/devices', this.getDeviceStats.bind(this));
        this.router.get('/stats/groups', this.getGroupStats.bind(this));
        
        // 分组管理路由
        this.router.get('/groups', this.getGroups.bind(this));
        this.router.post('/groups', this.addGroup.bind(this));
        this.router.put('/groups/:groupId', this.updateGroup.bind(this));
        this.router.delete('/groups/:groupId', this.removeGroup.bind(this));
        
        // 系统管理路由
        this.router.post('/system/reset', this.resetSystem.bind(this));
        this.router.post('/system/clear', this.clearDevices.bind(this));
        this.router.get('/system/health', this.getSystemHealth.bind(this));
        this.router.post('/system/create-test-devices', this.createTestDevices.bind(this));
        
        // API文档和设备类型信息路由
        this.router.get('/info/device-types', this.getDeviceTypes.bind(this));
        this.router.get('/info/api-docs', this.getApiDocs.bind(this));
        this.router.get('/info/device-capabilities/:type', this.getDeviceCapabilities.bind(this));
    }

    /**
     * 获取所有设备
     */
    async getAllDevices(req, res) {
        try {
            const devices = this.deviceManager.getAllDevices();
            res.json({
                success: true,
                data: devices
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 添加设备
     */
    async addDevice(req, res) {
        try {
            const { name, type, groupId } = req.body;
            
            if (!name || !type) {
                return res.status(400).json({
                    success: false,
                    error: '设备名称和类型为必填项'
                });
            }
            
            if (!Object.values(DeviceType).includes(type)) {
                return res.status(400).json({
                    success: false,
                    error: '无效的设备类型'
                });
            }

            // 传递config对象给DeviceManager
            const device = this.deviceManager.addDevice({
                name: name,
                type: type,
                groupId: groupId
            });
            
            res.json({
                success: true,
                data: device
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 获取指定设备
     */
    async getDevice(req, res) {
        try {
            const { id } = req.params;
            const device = this.deviceManager.getDevice(id);
            
            if (!device) {
                return res.status(404).json({
                    success: false,
                    error: '设备不存在'
                });
            }
            
            res.json({
                success: true,
                data: device
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 更新设备信息
     */
    async updateDevice(req, res) {
        try {
            const { id } = req.params;
            const updates = req.body;
            
            const device = this.deviceManager.updateDevice(id, updates);
            res.json({
                success: true,
                data: device
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 删除设备
     */
    async removeDevice(req, res) {
        try {
            const { id } = req.params;
            const device = this.deviceManager.removeDevice(id);
            
            res.json({
                success: true,
                data: device
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 控制设备
     */
    async controlDevice(req, res) {
        try {
            const { id } = req.params;
            const { command, params } = req.body;
            
            if (!command) {
                return res.status(400).json({
                    success: false,
                    error: '命令参数为必填项'
                });
            }
            
            const result = this.deviceManager.controlDevice(id, command, params || {});
            res.json({
                success: true,
                data: result
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 分组控制设备
     */
    async controlDevicesByGroup(req, res) {
        try {
            const { groupId } = req.params;
            const { command, params } = req.body;
            
            if (!command) {
                return res.status(400).json({
                    success: false,
                    error: '命令参数为必填项'
                });
            }
            
            const result = this.deviceManager.controlDevicesByGroup(groupId, command, params || {});
            res.json({
                success: true,
                data: result
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 批量控制设备
     */
    async controlDevicesBatch(req, res) {
        try {
            const { deviceIds, command, params } = req.body;
            
            if (!deviceIds || !Array.isArray(deviceIds) || !command) {
                return res.status(400).json({
                    success: false,
                    error: '设备ID列表和命令为必填项'
                });
            }
            
            const result = this.deviceManager.controlDevices(deviceIds, command, params || {});
            res.json({
                success: true,
                data: result
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 获取分组设备
     */
    async getDevicesByGroup(req, res) {
        try {
            const { groupId } = req.params;
            const devices = this.deviceManager.getDevicesByGroup(groupId);
            
            res.json({
                success: true,
                data: devices
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 搜索设备
     */
    async searchDevices(req, res) {
        try {
            const { query, type, groupId, enabled } = req.body;
            const devices = this.deviceManager.searchDevices({ query, type, groupId, enabled });
            
            res.json({
                success: true,
                data: devices
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 获取统计信息
     */
    async getStats(req, res) {
        try {
            const stats = this.deviceManager.getStats();
            res.json({
                success: true,
                data: stats
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 获取设备统计
     */
    async getDeviceStats(req, res) {
        try {
            const stats = this.deviceManager.getDeviceStats();
            res.json({
                success: true,
                data: stats
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 获取分组统计
     */
    async getGroupStats(req, res) {
        try {
            const stats = this.deviceManager.getGroupStats();
            res.json({
                success: true,
                data: stats
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 获取所有分组
     */
    async getGroups(req, res) {
        try {
            const groups = this.deviceManager.getGroups();
            res.json({
                success: true,
                data: groups
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 添加分组
     */
    async addGroup(req, res) {
        try {
            const { groupId, name, description } = req.body;
            
            if (!groupId || !name) {
                return res.status(400).json({
                    success: false,
                    error: '分组ID和名称为必填项'
                });
            }
            
            const group = this.deviceManager.addGroup(groupId, name, description);
            res.json({
                success: true,
                data: group
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 更新分组
     */
    async updateGroup(req, res) {
        try {
            const { groupId } = req.params;
            const updates = req.body;
            
            const group = this.deviceManager.updateGroup(groupId, updates);
            res.json({
                success: true,
                data: group
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 删除分组
     */
    async removeGroup(req, res) {
        try {
            const { groupId } = req.params;
            const group = this.deviceManager.removeGroup(groupId);
            
            res.json({
                success: true,
                data: group
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 重置系统
     */
    async resetSystem(req, res) {
        try {
            this.deviceManager.reset();
            res.json({
                success: true,
                message: '系统已重置'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 清空设备
     */
    async clearDevices(req, res) {
        try {
            this.deviceManager.clearAllDevices();
            res.json({
                success: true,
                message: '所有设备已清空'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 获取系统健康状态
     */
    async getSystemHealth(req, res) {
        try {
            const os = require('os');
            const path = require('path');
            
            // 获取内存信息 (字节转换为MB)
            const memoryUsage = process.memoryUsage();
            const totalMemory = os.totalmem();
            const freeMemory = os.freemem();
            const usedMemory = totalMemory - freeMemory;
            
            // 获取CPU信息
            const cpus = os.cpus();
            const loadAverage = os.loadavg();
            
            // 获取系统平台信息
            const platform = os.platform();
            const architecture = os.arch();
            const hostname = os.hostname();
            
            // 格式化运行时间
            const uptimeSeconds = process.uptime();
            const systemUptimeSeconds = os.uptime();
            
            const health = {
                status: 'healthy',
                timestamp: new Date().toISOString(),
                server: {
                    uptime: uptimeSeconds,
                    uptimeFormatted: this.formatUptime(uptimeSeconds),
                    pid: process.pid,
                    nodeVersion: process.version,
                    platform: process.platform,
                    arch: process.arch
                },
                system: {
                    platform: platform,
                    architecture: architecture,
                    hostname: hostname,
                    uptime: systemUptimeSeconds,
                    uptimeFormatted: this.formatUptime(systemUptimeSeconds),
                    cpuCount: cpus.length,
                    cpuModel: cpus[0]?.model || 'Unknown',
                    loadAverage: loadAverage.map(load => Math.round(load * 100) / 100)
                },
                memory: {
                    // 进程内存使用情况 (MB)
                    process: {
                        rss: Math.round(memoryUsage.rss / 1024 / 1024 * 100) / 100,
                        heapTotal: Math.round(memoryUsage.heapTotal / 1024 / 1024 * 100) / 100,
                        heapUsed: Math.round(memoryUsage.heapUsed / 1024 / 1024 * 100) / 100,
                        external: Math.round(memoryUsage.external / 1024 / 1024 * 100) / 100
                    },
                    // 系统内存使用情况 (MB)
                    system: {
                        total: Math.round(totalMemory / 1024 / 1024),
                        free: Math.round(freeMemory / 1024 / 1024),
                        used: Math.round(usedMemory / 1024 / 1024),
                        usagePercent: Math.round((usedMemory / totalMemory) * 100 * 100) / 100
                    }
                },
                application: {
                    deviceCount: this.deviceManager.getDeviceCount(),
                    groupCount: this.deviceManager.getGroupCount(),
                    connectedClients: this.getConnectedClientsCount(),
                    apiCallsToday: this.getApiCallsCount()
                }
            };
            
            res.json({
                success: true,
                data: health
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 批量创建测试设备
     */
    async createTestDevices(req, res) {
        try {
            const { deviceCount = 100, groupCount = 4 } = req.body;
            
            // 先创建分组
            const groups = [];
            const groupNames = ['A区实验室', 'B区实验室', 'C区实验室', 'D区实验室'];
            
            for (let i = 0; i < Math.min(groupCount, groupNames.length); i++) {
                const groupName = groupNames[i];
                const groupId = `test-group-${i + 1}`;
                
                const group = {
                    id: groupId,
                    name: groupName,
                    description: `${groupName}设备分组`,
                    deviceCount: 0
                };
                
                this.deviceManager.addGroup(group);
                groups.push(group);
            }
            
            // 设备类型列表
            const deviceTypes = Object.values(DeviceType);
            const createdDevices = [];
            const devicesPerGroup = Math.ceil(deviceCount / groups.length);
            
            for (let i = 0; i < deviceCount; i++) {
                const groupIndex = Math.floor(i / devicesPerGroup);
                const currentGroup = groups[Math.min(groupIndex, groups.length - 1)];
                
                // 随机选择设备类型
                const deviceType = deviceTypes[Math.floor(Math.random() * deviceTypes.length)];
                
                // 生成设备名称
                const deviceTypeNames = {
                    [DeviceType.ENVIRONMENT_MONITOR]: '环境监测',
                    [DeviceType.STUDENT_POWER_TERMINAL]: '学生电源',
                    [DeviceType.ENVIRONMENT_CONTROLLER]: '环境控制',
                    [DeviceType.CURTAIN_CONTROLLER]: '窗帘控制',
                    [DeviceType.LIGHTING_CONTROLLER]: '灯光控制',
                    [DeviceType.LIFT_CONTROLLER]: '升降控制'
                };
                
                const deviceName = `${deviceTypeNames[deviceType]}_${String(i + 1).padStart(3, '0')}`;
                
                // 创建设备
                const device = this.deviceManager.addDevice({
                    name: deviceName,
                    type: deviceType,
                    groupId: currentGroup.id,
                    config: {
                        autoStart: Math.random() > 0.3, // 70%概率自动启动
                        testDevice: true
                    }
                });
                
                if (device) {
                    createdDevices.push(device);
                    
                    // 如果自动启动，则启动设备
                    if (device.config?.autoStart) {
                        this.deviceManager.startDevice(device.id);
                    }
                }
            }
            
            res.json({
                success: true,
                message: `成功创建 ${createdDevices.length} 个测试设备，分布在 ${groups.length} 个分组中`,
                data: {
                    devices: createdDevices.length,
                    groups: groups.length,
                    groupDetails: groups.map(g => ({
                        id: g.id,
                        name: g.name,
                        deviceCount: this.deviceManager.getDevicesByGroup(g.id).length
                    }))
                }
            });
            
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 格式化运行时间
     */
    formatUptime(seconds) {
        const days = Math.floor(seconds / 86400);
        const hours = Math.floor((seconds % 86400) / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = Math.floor(seconds % 60);
        
        if (days > 0) {
            return `${days}天 ${hours}小时 ${minutes}分钟`;
        } else if (hours > 0) {
            return `${hours}小时 ${minutes}分钟`;
        } else if (minutes > 0) {
            return `${minutes}分钟 ${secs}秒`;
        } else {
            return `${secs}秒`;
        }
    }

    /**
     * 获取连接的客户端数量
     */
    getConnectedClientsCount() {
        return this.server ? this.server.getConnectedClientsCount() : 0;
    }

    /**
     * 获取API调用次数
     */
    getApiCallsCount() {
        return this.server ? this.server.getApiCallsCount() : 0;
    }

    /**
     * 获取设备类型信息
     */
    async getDeviceTypes(req, res) {
        try {
            const deviceTypes = Object.values(DeviceType).map(type => ({
                type: type,
                name: this.getTypeName(type),
                description: this.getTypeDescription(type),
                model: this.getDeviceModel(type)
            }));
            
            res.json({
                success: true,
                data: deviceTypes
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 获取设备能力信息
     */
    async getDeviceCapabilities(req, res) {
        try {
            const { type } = req.params;
            
            if (!Object.values(DeviceType).includes(type)) {
                return res.status(400).json({
                    success: false,
                    error: '无效的设备类型'
                });
            }
            
            const capabilities = this.getCapabilitiesByType(type);
            const commands = this.getCommandsByType(type);
            
            res.json({
                success: true,
                data: {
                    type: type,
                    name: this.getTypeName(type),
                    model: this.getDeviceModel(type),
                    capabilities: capabilities,
                    commands: commands,
                    dataFormat: this.getDataFormatByType(type)
                }
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 获取API文档
     */
    async getApiDocs(req, res) {
        try {
            const apiDocs = {
                title: 'ESP8266设备模拟器 API文档',
                version: '1.0.0',
                description: '提供ESP8266设备管理和控制的REST API接口',
                baseUrl: req.protocol + '://' + req.get('host') + '/api',
                endpoints: {
                    devices: {
                        'GET /devices': {
                            description: '获取所有设备列表',
                            response: { success: 'boolean', data: 'array' }
                        },
                        'POST /devices': {
                            description: '添加新设备',
                            body: { name: 'string', type: 'string', groupId: 'string(optional)' },
                            response: { success: 'boolean', data: 'object' }
                        },
                        'GET /devices/:id': {
                            description: '获取指定设备信息',
                            response: { success: 'boolean', data: 'object' }
                        },
                        'PUT /devices/:id': {
                            description: '更新设备信息',
                            body: { name: 'string(optional)', groupId: 'string(optional)' },
                            response: { success: 'boolean', data: 'object' }
                        },
                        'DELETE /devices/:id': {
                            description: '删除设备',
                            response: { success: 'boolean', data: 'object' }
                        },
                        'POST /devices/:id/control': {
                            description: '控制设备',
                            body: { command: 'string', params: 'object(optional)' },
                            response: { success: 'boolean', data: 'object' }
                        }
                    },
                    groups: {
                        'GET /groups': {
                            description: '获取所有分组',
                            response: { success: 'boolean', data: 'array' }
                        },
                        'POST /groups': {
                            description: '创建分组',
                            body: { groupId: 'string', name: 'string', description: 'string(optional)' },
                            response: { success: 'boolean', data: 'object' }
                        },
                        'PUT /groups/:groupId': {
                            description: '更新分组信息',
                            body: { name: 'string(optional)', description: 'string(optional)' },
                            response: { success: 'boolean', data: 'object' }
                        },
                        'DELETE /groups/:groupId': {
                            description: '删除分组',
                            response: { success: 'boolean', data: 'object' }
                        },
                        'POST /groups/:groupId/control': {
                            description: '控制分组内所有设备',
                            body: { command: 'string', params: 'object(optional)' },
                            response: { success: 'boolean', data: 'object' }
                        }
                    },
                    info: {
                        'GET /info/device-types': {
                            description: '获取支持的设备类型',
                            response: { success: 'boolean', data: 'array' }
                        },
                        'GET /info/device-capabilities/:type': {
                            description: '获取指定类型设备的能力和命令',
                            response: { success: 'boolean', data: 'object' }
                        },
                        'GET /info/api-docs': {
                            description: '获取API文档',
                            response: 'object'
                        }
                    },
                    stats: {
                        'GET /stats': {
                            description: '获取系统统计信息',
                            response: { success: 'boolean', data: 'object' }
                        }
                    }
                },
                examples: {
                    addDevice: {
                        url: 'POST /api/devices',
                        body: {
                            name: '客厅智能开关',
                            type: 'SMART_SWITCH',
                            groupId: 'living-room'
                        }
                    },
                    controlDevice: {
                        url: 'POST /api/devices/device-001/control',
                        body: {
                            command: 'turnOn'
                        }
                    },
                    setLedColor: {
                        url: 'POST /api/devices/led-001/control',
                        body: {
                            command: 'setColor',
                            params: { red: 255, green: 0, blue: 0 }
                        }
                    }
                }
            };
            
            res.json(apiDocs);
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 获取类型名称
     */
    getTypeName(type) {
        const names = {
            [DeviceType.SMART_SWITCH]: '智能开关',
            [DeviceType.LED_CONTROLLER]: 'LED控制器',
            [DeviceType.SENSOR_NODE]: '传感器节点',
            [DeviceType.RELAY_MODULE]: '继电器模块'
        };
        return names[type] || type;
    }
    
    /**
     * 获取类型描述
     */
    getTypeDescription(type) {
        const descriptions = {
            [DeviceType.SMART_SWITCH]: '可远程控制的智能开关设备，支持电量监测和定时控制',
            [DeviceType.LED_CONTROLLER]: '支持调光调色的LED控制器，可设置多种灯效模式',
            [DeviceType.SENSOR_NODE]: '多功能环境传感器节点，监测温湿度、光照和运动',
            [DeviceType.RELAY_MODULE]: '4路继电器控制模块，支持独立控制和互锁功能'
        };
        return descriptions[type] || '';
    }

    /**
     * 获取设备型号
     */
    getDeviceModel(type) {
        const models = {
            [DeviceType.SMART_SWITCH]: 'ESP8266-SW-001',
            [DeviceType.LED_CONTROLLER]: 'ESP8266-LED-002',
            [DeviceType.SENSOR_NODE]: 'ESP8266-SEN-003',
            [DeviceType.RELAY_MODULE]: 'ESP8266-REL-004'
        };
        return models[type] || 'ESP8266-UNKNOWN';
    }
    
    /**
     * 根据类型获取能力列表
     */
    getCapabilitiesByType(type) {
        const capabilities = {
            [DeviceType.SMART_SWITCH]: [
                '开关控制', '功率监测', '电流监测', '电压监测', '定时控制', '累计用电量'
            ],
            [DeviceType.LED_CONTROLLER]: [
                '开关控制', '亮度调节', '颜色设置', '灯效模式', '呼吸灯', '彩虹模式'
            ],
            [DeviceType.SENSOR_NODE]: [
                '温度监测', '湿度监测', '光照监测', '运动检测', '传感器校准', '告警设置'
            ],
            [DeviceType.RELAY_MODULE]: [
                '继电器控制', '4路独立控制', '互锁功能', '状态监测', '批量控制'
            ]
        };
        return capabilities[type] || [];
    }
    
    /**
     * 根据类型获取命令列表
     */
    getCommandsByType(type) {
        const commands = {
            [DeviceType.SMART_SWITCH]: [
                { command: 'turnOn', description: '开启开关', params: [] },
                { command: 'turnOff', description: '关闭开关', params: [] },
                { command: 'toggle', description: '切换状态', params: [] },
                { command: 'schedule', description: '设置定时', params: ['onTime', 'offTime'] }
            ],
            [DeviceType.LED_CONTROLLER]: [
                { command: 'turnOn', description: '开启LED', params: [] },
                { command: 'turnOff', description: '关闭LED', params: [] },
                { command: 'setBrightness', description: '设置亮度', params: ['brightness'] },
                { command: 'setColor', description: '设置颜色', params: ['red', 'green', 'blue'] },
                { command: 'setMode', description: '设置模式', params: ['mode'] }
            ],
            [DeviceType.SENSOR_NODE]: [
                { command: 'calibrate', description: '传感器校准', params: ['sensor', 'offset'] },
                { command: 'setAlert', description: '设置告警', params: ['sensor', 'threshold'] }
            ],
            [DeviceType.RELAY_MODULE]: [
                { command: 'relayOn', description: '开启继电器', params: ['relayId'] },
                { command: 'relayOff', description: '关闭继电器', params: ['relayId'] },
                { command: 'relayToggle', description: '切换继电器', params: ['relayId'] },
                { command: 'setInterlock', description: '设置互锁', params: ['group'] }
            ]
        };
        return commands[type] || [];
    }
    
    /**
     * 根据类型获取数据格式
     */
    getDataFormatByType(type) {
        const formats = {
            [DeviceType.SMART_SWITCH]: {
                switch: { state: 'ON|OFF', voltage: 'number', current: 'number', power: 'number', energy: 'number' },
                schedule: { enabled: 'boolean', onTime: 'string', offTime: 'string' }
            },
            [DeviceType.LED_CONTROLLER]: {
                led: { 
                    state: 'ON|OFF', 
                    brightness: 'number(0-100)', 
                    color: { red: 'number(0-255)', green: 'number(0-255)', blue: 'number(0-255)' },
                    mode: 'static|breathing|rainbow|strobe'
                }
            },
            [DeviceType.SENSOR_NODE]: {
                sensors: {
                    temperature: { value: 'number', unit: 'string', calibration: 'number' },
                    humidity: { value: 'number', unit: 'string', calibration: 'number' },
                    light: { value: 'number', unit: 'string' },
                    motion: { detected: 'boolean', lastDetection: 'string', sensitivity: 'number' }
                }
            },
            [DeviceType.RELAY_MODULE]: {
                relays: [
                    { id: 'number', state: 'ON|OFF', name: 'string', type: 'NO|NC', voltage: 'number', maxCurrent: 'number' }
                ]
            }
        };
        return formats[type] || {};
    }
    
    /**
     * 将设备分配到分组
     */
    async assignDeviceToGroup(req, res) {
        try {
            const { id } = req.params;
            const { groupId } = req.body;
            
            console.log(`分配设备 ${id} 到分组 ${groupId}`);
            
            // 使用getDeviceInstance获取设备实例进行修改
            const deviceInstance = this.deviceManager.getDeviceInstance(id);
            if (!deviceInstance) {
                return res.status(404).json({
                    success: false,
                    error: '设备不存在',
                    code: 'DEVICE_NOT_FOUND'
                });
            }
            
            // 如果groupId为null或空，表示移出分组
            if (!groupId) {
                deviceInstance.groupId = null;
                console.log(`设备 ${id} 已移出分组`);
            } else {
                // 检查分组是否存在
                const group = this.deviceManager.getGroup(groupId);
                if (!group) {
                    return res.status(404).json({
                        success: false,
                        error: '分组不存在',
                        code: 'GROUP_NOT_FOUND'
                    });
                }
                
                deviceInstance.groupId = groupId;
                console.log(`设备 ${id} 已分配到分组 ${groupId}`);
            }
            
            res.json({
                success: true,
                message: groupId ? `设备已分配到分组 ${groupId}` : '设备已移出分组',
                data: {
                    deviceId: id,
                    groupId: groupId
                }
            });
            
        } catch (error) {
            console.error('分配设备到分组失败:', error);
            res.status(500).json({
                success: false,
                error: '分配设备到分组失败',
                details: error.message
            });
        }
    }

    /**
     * 获取路由器
     */
    getRouter() {
        return this.router;
    }
}

module.exports = ApiRouter;
