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
        
        // 初始化互动教学系统管理器
        this.interactiveManager = {
            sessions: new Map(),
            questions: new Map(),
            students: new Map(), // seatId -> studentInfo
            currentSession: null,
            currentQuestion: null,
            lastCreatedQuestion: null, // 最后创建的问题ID
            answers: new Map(), // questionId -> [answers]
            statistics: new Map() // questionId -> statistics
        };
        
        this.setupRoutes();
        this.initializeInteractiveSystem();
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
        
        // 互动教学系统路由
        this.router.get('/interactive/sessions', this.getInteractiveSessions.bind(this));
        this.router.post('/interactive/sessions', this.createInteractiveSession.bind(this));
        this.router.get('/interactive/sessions/current', this.getCurrentSession.bind(this));
        this.router.get('/interactive/sessions/:sessionId', this.getInteractiveSession.bind(this));
        this.router.post('/interactive/sessions/:sessionId/start', this.startInteractiveSession.bind(this));
        this.router.post('/interactive/sessions/:sessionId/stop', this.stopInteractiveSession.bind(this));
        this.router.delete('/interactive/sessions/:sessionId', this.deleteInteractiveSession.bind(this));
        this.router.delete('/interactive/sessions/current', this.endCurrentSession.bind(this));
        this.router.post('/interactive/sessions/clear', this.clearSessionData.bind(this));
        
        this.router.get('/interactive/questions', this.getQuestions.bind(this));
        this.router.post('/interactive/questions', this.createQuestion.bind(this));
        this.router.get('/interactive/questions/current', this.getCurrentQuestion.bind(this));
        this.router.get('/interactive/questions/:questionId', this.getQuestion.bind(this));
        this.router.put('/interactive/questions/:questionId', this.updateQuestion.bind(this));
        this.router.delete('/interactive/questions/:questionId', this.deleteQuestion.bind(this));
        this.router.post('/interactive/questions/publish', this.publishLatestQuestion.bind(this));
        this.router.post('/interactive/questions/:questionId/publish', this.publishQuestion.bind(this));
        this.router.post('/interactive/questions/stop', this.stopCurrentQuestion.bind(this));
        
        this.router.get('/interactive/students', this.getStudents.bind(this));
        this.router.post('/interactive/students', this.assignStudent.bind(this));
        this.router.delete('/interactive/students/:seatId', this.removeStudent.bind(this));
        this.router.post('/interactive/students/:seatId/answer', this.submitAnswer.bind(this));
        
        this.router.get('/interactive/statistics', this.getInteractiveStatistics.bind(this));
        this.router.get('/interactive/statistics/:questionId', this.getQuestionStatistics.bind(this));
        this.router.post('/interactive/statistics/clear', this.clearInteractiveStatistics.bind(this));
        
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
                
                this.deviceManager.addGroup(groupId, groupName, `${groupName}设备分组`);
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
                    [DeviceType.LIFT_CONTROLLER]: '升降控制',
                    [DeviceType.INTERACTIVE_STUDENT_TERMINAL]: '互动终端',
                    [DeviceType.INTERACTIVE_DISPLAY]: '互动显示',
                    [DeviceType.INTERACTIVE_CONTROLLER]: '互动控制'
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
                    },
                    system: {
                        'POST /system/reset': {
                            description: '重置系统（清除所有设备和分组）',
                            response: { success: 'boolean', message: 'string' }
                        },
                        'POST /system/clear': {
                            description: '清空所有设备',
                            response: { success: 'boolean', message: 'string' }
                        },
                        'GET /system/health': {
                            description: '获取系统健康状态和运行信息',
                            response: { success: 'boolean', data: 'object' }
                        },
                        'POST /system/create-test-devices': {
                            description: '批量创建测试设备和分组',
                            body: { deviceCount: 'number(default:100)', groupCount: 'number(default:4)' },
                            response: { success: 'boolean', message: 'string', data: 'object' }
                        }
                    },
                    interactive: {
                        'GET /interactive/sessions': {
                            description: '获取所有互动教学会话',
                            response: { success: 'boolean', data: 'array' }
                        },
                        'POST /interactive/sessions': {
                            description: '创建新的互动教学会话',
                            body: { name: 'string', description: 'string(optional)', timeLimit: 'number(optional)' },
                            response: { success: 'boolean', data: 'object' }
                        },
                        'GET /interactive/sessions/:sessionId': {
                            description: '获取特定会话信息',
                            response: { success: 'boolean', data: 'object' }
                        },
                        'POST /interactive/sessions/:sessionId/start': {
                            description: '开始互动教学会话',
                            response: { success: 'boolean', data: 'object' }
                        },
                        'POST /interactive/sessions/:sessionId/stop': {
                            description: '停止互动教学会话',
                            response: { success: 'boolean', data: 'object' }
                        },
                        'DELETE /interactive/sessions/:sessionId': {
                            description: '删除互动教学会话',
                            response: { success: 'boolean', message: 'string' }
                        },
                        'GET /interactive/sessions/current': {
                            description: '获取当前活动会话',
                            response: { success: 'boolean', session: 'object|null', message: 'string(optional)' }
                        },
                        'DELETE /interactive/sessions/current': {
                            description: '结束当前活动会话',
                            response: { success: 'boolean', message: 'string' }
                        },
                        'POST /interactive/sessions/clear': {
                            description: '清空所有会话数据',
                            response: { success: 'boolean', message: 'string' }
                        },
                        'GET /interactive/questions': {
                            description: '获取所有题目',
                            response: { success: 'boolean', data: 'array' }
                        },
                        'POST /interactive/questions': {
                            description: '创建新题目',
                            body: { content: 'string', options: 'array', correctAnswer: 'string', timeLimit: 'number(optional)', difficulty: 'string(optional)' },
                            response: { success: 'boolean', data: 'object' }
                        },
                        'GET /interactive/questions/:questionId': {
                            description: '获取特定题目信息',
                            response: { success: 'boolean', data: 'object' }
                        },
                        'PUT /interactive/questions/:questionId': {
                            description: '更新题目',
                            body: { content: 'string(optional)', options: 'array(optional)', correctAnswer: 'string(optional)', timeLimit: 'number(optional)', difficulty: 'string(optional)' },
                            response: { success: 'boolean', data: 'object' }
                        },
                        'DELETE /interactive/questions/:questionId': {
                            description: '删除题目',
                            response: { success: 'boolean', message: 'string' }
                        },
                        'POST /interactive/questions/:questionId/publish': {
                            description: '发布题目给学生答题',
                            response: { success: 'boolean', data: 'object' }
                        },
                        'GET /interactive/questions/current': {
                            description: '获取当前发布的题目',
                            response: { success: 'boolean', question: 'object|null', message: 'string(optional)' }
                        },
                        'POST /interactive/questions/stop': {
                            description: '停止当前题目答题',
                            response: { success: 'boolean', message: 'string', questionId: 'string(optional)' }
                        },
                        'GET /interactive/students': {
                            description: '获取所有学生座位状态',
                            response: { success: 'boolean', data: 'array' }
                        },
                        'POST /interactive/students': {
                            description: '分配学生到座位',
                            body: { seatId: 'string', studentName: 'string' },
                            response: { success: 'boolean', data: 'object' }
                        },
                        'DELETE /interactive/students/:seatId': {
                            description: '移除座位上的学生',
                            response: { success: 'boolean', data: 'object' }
                        },
                        'POST /interactive/students/:seatId/answer': {
                            description: '提交学生答案',
                            body: { answer: 'string' },
                            response: { success: 'boolean', data: 'object' }
                        },
                        'GET /interactive/statistics': {
                            description: '获取互动教学系统统计信息',
                            response: { success: 'boolean', data: 'object' }
                        },
                        'GET /interactive/statistics/:questionId': {
                            description: '获取特定题目的统计信息',
                            response: { success: 'boolean', data: 'object' }
                        },
                        'POST /interactive/statistics/clear': {
                            description: '清空所有统计信息',
                            response: { success: 'boolean', message: 'string' }
                        }
                    }
                },
                examples: {
                    addDevice: {
                        url: 'POST /api/devices',
                        body: {
                            name: '环境监测装置-001',
                            type: 'ENVIRONMENT_MONITOR',
                            groupId: 'test-group-1'
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
                    },
                    createTestDevices: {
                        url: 'POST /api/system/create-test-devices',
                        body: {
                            deviceCount: 100,
                            groupCount: 4
                        }
                    },
                    getSystemHealth: {
                        url: 'GET /api/system/health'
                    },
                    addGroup: {
                        url: 'POST /api/groups',
                        body: {
                            groupId: 'lab-a',
                            name: 'A区实验室',
                            description: 'A区实验室设备分组'
                        }
                    },
                    createInteractiveSession: {
                        url: 'POST /api/interactive/sessions',
                        body: {
                            name: '数学基础测试',
                            description: '小学数学基础题目测试',
                            timeLimit: 300
                        }
                    },
                    createQuestion: {
                        url: 'POST /api/interactive/questions',
                        body: {
                            content: '3 + 5 = ?',
                            options: ['6', '7', '8', '9'],
                            correctAnswer: '8',
                            timeLimit: 30,
                            difficulty: 'EASY'
                        }
                    },
                    publishQuestion: {
                        url: 'POST /api/interactive/questions/q1/publish'
                    },
                    assignStudent: {
                        url: 'POST /api/interactive/students',
                        body: {
                            seatId: 'A1',
                            studentName: '张三'
                        }
                    },
                    submitAnswer: {
                        url: 'POST /api/interactive/students/A1/answer',
                        body: {
                            answer: '8'
                        }
                    },
                    getStatistics: {
                        url: 'GET /api/interactive/statistics'
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
            [DeviceType.ENVIRONMENT_MONITOR]: '环境检测装置',
            [DeviceType.STUDENT_POWER_TERMINAL]: '学生电源终端',
            [DeviceType.ENVIRONMENT_CONTROLLER]: '环境控制装置',
            [DeviceType.CURTAIN_CONTROLLER]: '窗帘控制装置',
            [DeviceType.LIGHTING_CONTROLLER]: '灯光控制装置',
            [DeviceType.LIFT_CONTROLLER]: '升降控制装置',
            [DeviceType.INTERACTIVE_STUDENT_TERMINAL]: '互动教学学生终端',
            [DeviceType.INTERACTIVE_DISPLAY]: '互动教学显示设备',
            [DeviceType.INTERACTIVE_CONTROLLER]: '互动教学控制器'
        };
        return names[type] || type;
    }
    
    /**
     * 获取类型描述
     */
    getTypeDescription(type) {
        const descriptions = {
            [DeviceType.ENVIRONMENT_MONITOR]: '环境数据监测与显示装置',
            [DeviceType.STUDENT_POWER_TERMINAL]: '学生实验低压电源控制终端',
            [DeviceType.ENVIRONMENT_CONTROLLER]: '供水排风环境控制装置',
            [DeviceType.CURTAIN_CONTROLLER]: '窗帘自动控制装置',
            [DeviceType.LIGHTING_CONTROLLER]: '教室灯光调节控制装置',
            [DeviceType.LIFT_CONTROLLER]: '实验台升降控制装置',
            [DeviceType.INTERACTIVE_STUDENT_TERMINAL]: '学生互动答题终端设备',
            [DeviceType.INTERACTIVE_DISPLAY]: '互动教学大屏显示设备',
            [DeviceType.INTERACTIVE_CONTROLLER]: '互动教学系统控制器'
        };
        return descriptions[type] || '';
    }

    /**
     * 获取设备型号
     */
    getDeviceModel(type) {
        const models = {
            [DeviceType.ENVIRONMENT_MONITOR]: 'ESP8266-ENV-001',
            [DeviceType.STUDENT_POWER_TERMINAL]: 'ESP8266-PWR-002',
            [DeviceType.ENVIRONMENT_CONTROLLER]: 'ESP8266-CTRL-003',
            [DeviceType.CURTAIN_CONTROLLER]: 'ESP8266-CURTAIN-004',
            [DeviceType.LIGHTING_CONTROLLER]: 'ESP8266-LIGHT-005',
            [DeviceType.LIFT_CONTROLLER]: 'ESP8266-LIFT-006'
        };
        return models[type] || 'ESP8266-UNKNOWN';
    }
    
    /**
     * 根据类型获取能力列表
     */
    getCapabilitiesByType(type) {
        const capabilities = {
            [DeviceType.ENVIRONMENT_MONITOR]: [
                '温度监测', '湿度监测', '光照监测', '空气质量监测', '传感器校准', '数据记录'
            ],
            [DeviceType.STUDENT_POWER_TERMINAL]: [
                '电源控制', '电压监测', '电流监测', '功率监测', '安全保护', '用电统计'
            ],
            [DeviceType.ENVIRONMENT_CONTROLLER]: [
                '供水控制', '排风控制', '温度调节', '湿度调节', '自动模式', '手动模式'
            ],
            [DeviceType.CURTAIN_CONTROLLER]: [
                '窗帘开关', '位置控制', '自动光感', '定时控制', '手动调节', '限位保护'
            ],
            [DeviceType.LIGHTING_CONTROLLER]: [
                '灯光开关', '亮度调节', '色温调节', '场景模式', '定时控制', '能耗监测'
            ],
            [DeviceType.LIFT_CONTROLLER]: [
                '升降控制', '位置控制', '速度调节', '限位保护', '手动模式', '紧急停止'
            ]
        };
        return capabilities[type] || [];
    }
    
    /**
     * 根据类型获取命令列表
     */
    getCommandsByType(type) {
        const commands = {
            [DeviceType.ENVIRONMENT_MONITOR]: [
                { command: 'startMonitoring', description: '开始监测', params: [] },
                { command: 'stopMonitoring', description: '停止监测', params: [] },
                { command: 'calibrate', description: '传感器校准', params: ['sensor', 'offset'] },
                { command: 'setAlert', description: '设置告警阈值', params: ['sensor', 'threshold'] }
            ],
            [DeviceType.STUDENT_POWER_TERMINAL]: [
                { command: 'powerOn', description: '开启电源', params: [] },
                { command: 'powerOff', description: '关闭电源', params: [] },
                { command: 'setVoltage', description: '设置电压', params: ['voltage'] },
                { command: 'setCurrent', description: '设置电流限制', params: ['current'] }
            ],
            [DeviceType.ENVIRONMENT_CONTROLLER]: [
                { command: 'startWaterSupply', description: '开始供水', params: [] },
                { command: 'stopWaterSupply', description: '停止供水', params: [] },
                { command: 'startVentilation', description: '开启排风', params: [] },
                { command: 'stopVentilation', description: '停止排风', params: [] }
            ],
            [DeviceType.CURTAIN_CONTROLLER]: [
                { command: 'openCurtain', description: '开启窗帘', params: [] },
                { command: 'closeCurtain', description: '关闭窗帘', params: [] },
                { command: 'setPosition', description: '设置位置', params: ['position'] }
            ],
            [DeviceType.LIGHTING_CONTROLLER]: [
                { command: 'turnOn', description: '开启灯光', params: [] },
                { command: 'turnOff', description: '关闭灯光', params: [] },
                { command: 'setBrightness', description: '设置亮度', params: ['brightness'] },
                { command: 'setColorTemp', description: '设置色温', params: ['temperature'] }
            ],
            [DeviceType.LIFT_CONTROLLER]: [
                { command: 'liftUp', description: '升起', params: [] },
                { command: 'liftDown', description: '降下', params: [] },
                { command: 'setHeight', description: '设置高度', params: ['height'] },
                { command: 'emergencyStop', description: '紧急停止', params: [] }
            ]
        };
        return commands[type] || [];
    }
    
    /**
     * 根据类型获取数据格式
     */
    getDataFormatByType(type) {
        const formats = {
            [DeviceType.ENVIRONMENT_MONITOR]: {
                temperature: { value: 'number', unit: '°C', range: '-40~80' },
                humidity: { value: 'number', unit: '%', range: '0~100' },
                light: { value: 'number', unit: 'lux', range: '0~65535' },
                airQuality: { value: 'number', unit: 'ppm', range: '0~500' }
            },
            [DeviceType.STUDENT_POWER_TERMINAL]: {
                power: { state: 'ON|OFF', voltage: 'number', current: 'number', power: 'number' },
                safety: { overCurrent: 'boolean', overVoltage: 'boolean', shortCircuit: 'boolean' }
            },
            [DeviceType.ENVIRONMENT_CONTROLLER]: {
                water: { state: 'ON|OFF', flow: 'number', pressure: 'number' },
                ventilation: { state: 'ON|OFF', speed: 'number', direction: 'IN|OUT' }
            },
            [DeviceType.CURTAIN_CONTROLLER]: {
                curtain: { state: 'OPEN|CLOSE|MOVING', position: 'number', target: 'number' }
            },
            [DeviceType.LIGHTING_CONTROLLER]: {
                light: { state: 'ON|OFF', brightness: 'number', colorTemp: 'number', power: 'number' }
            },
            [DeviceType.LIFT_CONTROLLER]: {
                lift: { state: 'UP|DOWN|STOPPED', height: 'number', target: 'number', speed: 'number' }
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
     * 初始化互动教学系统
     */
    initializeInteractiveSystem() {
        // 创建默认题目库
        const defaultQuestions = [
            {
                id: 'q1',
                content: '3 + 5 = ?',
                options: ['6', '7', '8', '9'],
                correctAnswer: '8',
                timeLimit: 30,
                difficulty: 'MEDIUM',
                createTime: Date.now()
            },
            {
                id: 'q2',
                content: '下列哪个字是形声字？',
                options: ['山', '河', '火', '土'],
                correctAnswer: '河',
                timeLimit: 45,
                difficulty: 'MEDIUM',
                createTime: Date.now()
            },
            {
                id: 'q3',
                content: 'Apple的中文意思是？',
                options: ['香蕉', '苹果', '橙子', '梨'],
                correctAnswer: '苹果',
                timeLimit: 20,
                difficulty: 'EASY',
                createTime: Date.now()
            },
            {
                id: 'q4',
                content: '地球围绕什么转动？',
                options: ['月亮', '太阳', '火星', '金星'],
                correctAnswer: '太阳',
                timeLimit: 25,
                difficulty: 'EASY',
                createTime: Date.now()
            },
            {
                id: 'q5',
                content: '中国的首都是？',
                options: ['上海', '广州', '北京', '深圳'],
                correctAnswer: '北京',
                timeLimit: 15,
                difficulty: 'EASY',
                createTime: Date.now()
            }
        ];

        defaultQuestions.forEach(question => {
            this.interactiveManager.questions.set(question.id, question);
        });

        // 初始化16个座位 (A1-D4)
        const rows = ['A', 'B', 'C', 'D'];
        for (const row of rows) {
            for (let col = 1; col <= 4; col++) {
                const seatId = `${row}${col}`;
                this.interactiveManager.students.set(seatId, {
                    seatId,
                    studentName: null,
                    status: 'EMPTY',
                    lastAnswer: null,
                    responseTime: null,
                    isCorrect: null,
                    assignTime: null,
                    isOnline: false
                });
            }
        }

        // 添加一些演示学生数据
        const demoStudents = [
            { seatId: 'A1', name: '张小明', isOnline: true },
            { seatId: 'A2', name: '李小红', isOnline: true },
            { seatId: 'A3', name: '王小华', isOnline: false },
            { seatId: 'A4', name: '刘小军', isOnline: true },
            { seatId: 'B2', name: '陈小美', isOnline: true },
            { seatId: 'B3', name: '林小强', isOnline: true },
            { seatId: 'C1', name: '周小雨', isOnline: false },
            { seatId: 'C4', name: '黄小光', isOnline: true }
        ];

        demoStudents.forEach(student => {
            this.interactiveManager.students.set(student.seatId, {
                seatId: student.seatId,
                studentName: student.name,
                status: student.isOnline ? 'WAITING' : 'OFFLINE',
                lastAnswer: null,
                responseTime: null,
                isCorrect: null,
                assignTime: Date.now(),
                isOnline: student.isOnline
            });
        });

        // 创建默认的互动教学设备
        this.createInteractiveDevices();

        console.log('📚 互动教学系统已初始化');
    }

    /**
     * 创建互动教学设备
     */
    createInteractiveDevices() {
        try {
            // 创建互动教学分组
            const interactiveGroupId = 'interactive-teaching';
            this.deviceManager.addGroup(interactiveGroupId, '互动教学系统', '互动教学相关设备分组');

            // 创建互动教学控制器
            const controller = this.deviceManager.addDevice({
                name: '互动教学主控制器',
                type: DeviceType.INTERACTIVE_CONTROLLER,
                groupId: interactiveGroupId,
                config: {
                    autoStart: true,
                    isActive: false,
                    currentQuestionId: null,
                    startTime: null,
                    timeLimit: 0,
                    autoNext: false,
                    totalQuestions: 0,
                    currentIndex: 0,
                    questionBank: [],
                    connectedTerminals: 0,
                    maxTerminals: 16,
                    signalQuality: 'excellent'
                }
            });

            // 创建互动教学显示设备
            const display = this.deviceManager.addDevice({
                name: '互动教学大屏显示器',
                type: DeviceType.INTERACTIVE_DISPLAY,
                groupId: interactiveGroupId,
                config: {
                    autoStart: true,
                    currentQuestionId: null,
                    questionContent: '',
                    options: [],
                    timeRemaining: 0,
                    isActive: false,
                    brightness: 90,
                    resolution: '1920x1080',
                    totalStudents: 0,
                    answered: 0,
                    correct: 0,
                    incorrect: 0,
                    timeout: 0
                }
            });

            // 创建16个学生终端设备 (A1-D4)
            const rows = ['A', 'B', 'C', 'D'];
            for (const row of rows) {
                for (let col = 1; col <= 4; col++) {
                    const seatId = `${row}${col}`;
                    const terminal = this.deviceManager.addDevice({
                        name: `学生终端-${seatId}`,
                        type: DeviceType.INTERACTIVE_STUDENT_TERMINAL,
                        groupId: interactiveGroupId,
                        config: {
                            autoStart: true,
                            seatId: seatId,
                            studentName: null,
                            status: 'EMPTY',
                            currentQuestionId: null,
                            selectedAnswer: null,
                            submitTime: null,
                            responseTime: null,
                            isCorrect: null,
                            screenOn: true,
                            buttonLights: { A: false, B: false, C: false, D: false },
                            buzzer: false,
                            networkSignal: 80 + Math.random() * 20
                        }
                    });

                    if (terminal && terminal.config?.autoStart) {
                        this.deviceManager.startDevice(terminal.id);
                    }
                }
            }

            if (controller && controller.config?.autoStart) {
                this.deviceManager.startDevice(controller.id);
            }

            if (display && display.config?.autoStart) {
                this.deviceManager.startDevice(display.id);
            }

            console.log('🎯 互动教学设备已创建：1个控制器，1个显示器，16个学生终端');

        } catch (error) {
            console.error('创建互动教学设备失败:', error);
        }
    }

    // ==================== 互动教学API方法 ====================

    /**
     * 获取所有会话
     */
    async getInteractiveSessions(req, res) {
        try {
            const sessions = Array.from(this.interactiveManager.sessions.values());
            res.json({
                success: true,
                data: sessions
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 创建新会话
     */
    async createInteractiveSession(req, res) {
        try {
            const { name, description, timeLimit } = req.body;
            
            if (!name) {
                return res.status(400).json({
                    success: false,
                    error: '会话名称为必填项'
                });
            }

            const sessionId = 's' + Date.now();
            const session = {
                id: sessionId,
                name,
                description: description || '',
                timeLimit: timeLimit || 300, // 默认5分钟
                isActive: false,
                currentQuestionId: null,
                startTime: null,
                endTime: null,
                createTime: Date.now(),
                questions: [],
                totalAnswers: 0
            };

            this.interactiveManager.sessions.set(sessionId, session);

            res.json({
                success: true,
                data: session,
                message: '会话创建成功'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 获取特定会话
     */
    async getInteractiveSession(req, res) {
        try {
            const sessionId = req.params.sessionId;
            const session = this.interactiveManager.sessions.get(sessionId);
            
            if (!session) {
                return res.status(404).json({
                    success: false,
                    error: '会话不存在'
                });
            }

            res.json({
                success: true,
                data: session
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 开始会话
     */
    async startInteractiveSession(req, res) {
        try {
            const sessionId = req.params.sessionId;
            const session = this.interactiveManager.sessions.get(sessionId);
            
            if (!session) {
                return res.status(404).json({
                    success: false,
                    error: '会话不存在'
                });
            }

            if (session.isActive) {
                return res.status(400).json({
                    success: false,
                    error: '会话已经在进行中'
                });
            }

            session.isActive = true;
            session.startTime = Date.now();
            this.interactiveManager.currentSession = sessionId;

            res.json({
                success: true,
                data: session,
                message: '会话已开始'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 停止会话
     */
    async stopInteractiveSession(req, res) {
        try {
            const sessionId = req.params.sessionId;
            const session = this.interactiveManager.sessions.get(sessionId);
            
            if (!session) {
                return res.status(404).json({
                    success: false,
                    error: '会话不存在'
                });
            }

            session.isActive = false;
            session.endTime = Date.now();
            session.currentQuestionId = null;
            this.interactiveManager.currentSession = null;
            this.interactiveManager.currentQuestion = null;

            res.json({
                success: true,
                data: session,
                message: '会话已停止'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 删除会话
     */
    async deleteInteractiveSession(req, res) {
        try {
            const sessionId = req.params.sessionId;
            const session = this.interactiveManager.sessions.get(sessionId);
            
            if (!session) {
                return res.status(404).json({
                    success: false,
                    error: '会话不存在'
                });
            }

            if (session.isActive) {
                return res.status(400).json({
                    success: false,
                    error: '无法删除正在进行的会话'
                });
            }

            this.interactiveManager.sessions.delete(sessionId);

            res.json({
                success: true,
                message: '会话已删除'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 获取所有题目
     */
    async getQuestions(req, res) {
        try {
            const questions = Array.from(this.interactiveManager.questions.values());
            res.json({
                success: true,
                data: questions
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 创建新题目
     */
    async createQuestion(req, res) {
        try {
            const { content, options, correctAnswer, timeLimit, difficulty } = req.body;
            
            if (!content || !options || !correctAnswer) {
                return res.status(400).json({
                    success: false,
                    error: '题目内容、选项和正确答案为必填项'
                });
            }

            if (!Array.isArray(options) || options.length < 2) {
                return res.status(400).json({
                    success: false,
                    error: '选项必须是包含至少2个选项的数组'
                });
            }

            if (!options.includes(correctAnswer)) {
                return res.status(400).json({
                    success: false,
                    error: '正确答案必须是选项中的一个'
                });
            }

            const questionId = 'q' + Date.now();
            const question = {
                id: questionId,
                content,
                options,
                correctAnswer,
                timeLimit: timeLimit || 30,
                difficulty: difficulty || 'MEDIUM',
                createTime: Date.now()
            };

            this.interactiveManager.questions.set(questionId, question);
            this.interactiveManager.lastCreatedQuestion = questionId; // 保存最后创建的问题ID

            res.json({
                success: true,
                data: question,
                message: '题目创建成功'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 获取特定题目
     */
    async getQuestion(req, res) {
        try {
            const questionId = req.params.questionId;
            const question = this.interactiveManager.questions.get(questionId);
            
            if (!question) {
                return res.status(404).json({
                    success: false,
                    error: '题目不存在'
                });
            }

            res.json({
                success: true,
                data: question
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 更新题目
     */
    async updateQuestion(req, res) {
        try {
            const questionId = req.params.questionId;
            const question = this.interactiveManager.questions.get(questionId);
            
            if (!question) {
                return res.status(404).json({
                    success: false,
                    error: '题目不存在'
                });
            }

            const { content, options, correctAnswer, timeLimit, difficulty } = req.body;

            if (content) question.content = content;
            if (options) {
                if (!Array.isArray(options) || options.length < 2) {
                    return res.status(400).json({
                        success: false,
                        error: '选项必须是包含至少2个选项的数组'
                    });
                }
                question.options = options;
            }
            if (correctAnswer) {
                if (!question.options.includes(correctAnswer)) {
                    return res.status(400).json({
                        success: false,
                        error: '正确答案必须是选项中的一个'
                    });
                }
                question.correctAnswer = correctAnswer;
            }
            if (timeLimit) question.timeLimit = timeLimit;
            if (difficulty) question.difficulty = difficulty;

            question.updateTime = Date.now();

            res.json({
                success: true,
                data: question,
                message: '题目更新成功'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 删除题目
     */
    async deleteQuestion(req, res) {
        try {
            const questionId = req.params.questionId;
            const question = this.interactiveManager.questions.get(questionId);
            
            if (!question) {
                return res.status(404).json({
                    success: false,
                    error: '题目不存在'
                });
            }

            // 检查题目是否正在使用
            if (this.interactiveManager.currentQuestion === questionId) {
                return res.status(400).json({
                    success: false,
                    error: '无法删除正在使用的题目'
                });
            }

            this.interactiveManager.questions.delete(questionId);
            this.interactiveManager.answers.delete(questionId);
            this.interactiveManager.statistics.delete(questionId);

            res.json({
                success: true,
                message: '题目已删除'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 发布题目
     */
    async publishQuestion(req, res) {
        try {
            const questionId = req.params.questionId;
            const question = this.interactiveManager.questions.get(questionId);
            
            if (!question) {
                return res.status(404).json({
                    success: false,
                    error: '题目不存在'
                });
            }

            // 设置当前题目
            this.interactiveManager.currentQuestion = questionId;
            
            // 重置所有学生状态为等待中
            for (const [seatId, student] of this.interactiveManager.students.entries()) {
                if (student.studentName) {
                    student.status = 'WAITING';
                    student.lastAnswer = null;
                    student.responseTime = null;
                    student.isCorrect = null;
                }
            }

            // 初始化答题记录
            this.interactiveManager.answers.set(questionId, []);
            
            // 更新当前会话
            if (this.interactiveManager.currentSession) {
                const session = this.interactiveManager.sessions.get(this.interactiveManager.currentSession);
                if (session) {
                    session.currentQuestionId = questionId;
                }
            }

            res.json({
                success: true,
                data: {
                    questionId,
                    question,
                    startTime: Date.now()
                },
                message: '题目已发布'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 发布最后创建的题目
     */
    async publishLatestQuestion(req, res) {
        try {
            const questionId = this.interactiveManager.lastCreatedQuestion;
            
            if (!questionId) {
                return res.status(400).json({
                    success: false,
                    error: '没有可发布的题目，请先创建题目'
                });
            }

            const question = this.interactiveManager.questions.get(questionId);
            
            if (!question) {
                return res.status(404).json({
                    success: false,
                    error: '题目不存在'
                });
            }

            // 设置当前题目
            this.interactiveManager.currentQuestion = questionId;
            
            // 重置所有学生状态为等待中
            for (const [seatId, student] of this.interactiveManager.students.entries()) {
                if (student.studentName) {
                    student.status = 'WAITING';
                    student.lastAnswer = null;
                    student.responseTime = null;
                    student.isCorrect = null;
                }
            }

            // 初始化答题记录
            this.interactiveManager.answers.set(questionId, []);
            
            // 更新当前会话
            if (this.interactiveManager.currentSession) {
                const session = this.interactiveManager.sessions.get(this.interactiveManager.currentSession);
                if (session) {
                    session.currentQuestionId = questionId;
                }
            }

            res.json({
                success: true,
                data: {
                    questionId,
                    question,
                    startTime: Date.now()
                },
                message: '题目已发布'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 获取学生座位状态
     */
    async getStudents(req, res) {
        try {
            const students = Array.from(this.interactiveManager.students.values());
            res.json({
                success: true,
                data: students
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 分配学生到座位
     */
    async assignStudent(req, res) {
        try {
            const { seatId, studentName } = req.body;
            
            if (!seatId || !studentName) {
                return res.status(400).json({
                    success: false,
                    error: '座位ID和学生姓名为必填项'
                });
            }

            const student = this.interactiveManager.students.get(seatId);
            if (!student) {
                return res.status(404).json({
                    success: false,
                    error: '座位不存在'
                });
            }

            student.studentName = studentName;
            student.status = 'WAITING';
            student.assignTime = Date.now();
            student.lastAnswer = null;
            student.responseTime = null;
            student.isCorrect = null;

            res.json({
                success: true,
                data: student,
                message: '学生分配成功'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 移除学生
     */
    async removeStudent(req, res) {
        try {
            const seatId = req.params.seatId;
            const student = this.interactiveManager.students.get(seatId);
            
            if (!student) {
                return res.status(404).json({
                    success: false,
                    error: '座位不存在'
                });
            }

            student.studentName = null;
            student.status = 'EMPTY';
            student.assignTime = null;
            student.lastAnswer = null;
            student.responseTime = null;
            student.isCorrect = null;

            res.json({
                success: true,
                data: student,
                message: '学生已移除'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 提交答案
     */
    async submitAnswer(req, res) {
        try {
            const seatId = req.params.seatId;
            const { answer } = req.body;
            
            if (!answer) {
                return res.status(400).json({
                    success: false,
                    error: '答案不能为空'
                });
            }

            const student = this.interactiveManager.students.get(seatId);
            if (!student) {
                return res.status(404).json({
                    success: false,
                    error: '座位不存在'
                });
            }

            if (!student.studentName) {
                return res.status(400).json({
                    success: false,
                    error: '该座位没有分配学生'
                });
            }

            const currentQuestionId = this.interactiveManager.currentQuestion;
            if (!currentQuestionId) {
                return res.status(400).json({
                    success: false,
                    error: '当前没有发布题目'
                });
            }

            const question = this.interactiveManager.questions.get(currentQuestionId);
            if (!question) {
                return res.status(404).json({
                    success: false,
                    error: '题目不存在'
                });
            }

            const currentTime = Date.now();
            const responseTime = Math.floor(Math.random() * 10000) + 1000; // 模拟响应时间1-11秒
            const isCorrect = answer === question.correctAnswer;

            // 创建答题记录
            const answerRecord = {
                studentId: student.studentName,
                seatPosition: seatId,
                questionId: currentQuestionId,
                answer,
                timestamp: currentTime,
                isCorrect,
                responseTime
            };

            // 更新答题记录
            const answers = this.interactiveManager.answers.get(currentQuestionId) || [];
            // 移除该学生之前的答题记录
            const filteredAnswers = answers.filter(a => a.seatPosition !== seatId);
            filteredAnswers.push(answerRecord);
            this.interactiveManager.answers.set(currentQuestionId, filteredAnswers);

            // 更新学生状态
            student.status = isCorrect ? 'CORRECT' : 'INCORRECT';
            student.lastAnswer = answer;
            student.responseTime = responseTime;
            student.isCorrect = isCorrect;

            // 更新统计信息
            this.updateQuestionStatistics(currentQuestionId);

            res.json({
                success: true,
                data: {
                    answerRecord,
                    student
                },
                message: '答案提交成功'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 获取互动教学统计信息
     */
    async getInteractiveStatistics(req, res) {
        try {
            const currentQuestionId = this.interactiveManager.currentQuestion;
            let currentQuestionStats = null;

            if (currentQuestionId) {
                currentQuestionStats = this.interactiveManager.statistics.get(currentQuestionId);
            }

            const totalStudents = Array.from(this.interactiveManager.students.values())
                .filter(s => s.studentName).length;

            const totalQuestions = this.interactiveManager.questions.size;
            const totalSessions = this.interactiveManager.sessions.size;
            const activeSessions = Array.from(this.interactiveManager.sessions.values())
                .filter(s => s.isActive).length;

            res.json({
                success: true,
                data: {
                    currentQuestionId,
                    currentQuestionStats,
                    totalStudents,
                    totalQuestions,
                    totalSessions,
                    activeSessions,
                    students: Array.from(this.interactiveManager.students.values())
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
     * 获取特定题目的统计信息
     */
    async getQuestionStatistics(req, res) {
        try {
            const questionId = req.params.questionId;
            const statistics = this.interactiveManager.statistics.get(questionId);
            
            if (!statistics) {
                return res.status(404).json({
                    success: false,
                    error: '该题目暂无统计信息'
                });
            }

            const answers = this.interactiveManager.answers.get(questionId) || [];

            res.json({
                success: true,
                data: {
                    questionId,
                    statistics,
                    answers
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
     * 清空互动教学统计信息
     */
    async clearInteractiveStatistics(req, res) {
        try {
            this.interactiveManager.answers.clear();
            this.interactiveManager.statistics.clear();
            this.interactiveManager.currentQuestion = null;

            // 重置所有学生状态
            for (const [seatId, student] of this.interactiveManager.students.entries()) {
                if (student.studentName) {
                    student.status = 'WAITING';
                    student.lastAnswer = null;
                    student.responseTime = null;
                    student.isCorrect = null;
                }
            }

            res.json({
                success: true,
                message: '统计信息已清空'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 更新题目统计信息
     */
    updateQuestionStatistics(questionId) {
        const answers = this.interactiveManager.answers.get(questionId) || [];
        const totalStudents = Array.from(this.interactiveManager.students.values())
            .filter(s => s.studentName).length;

        const totalAnswered = answers.length;
        const correctAnswers = answers.filter(a => a.isCorrect).length;
        const incorrectAnswers = totalAnswered - correctAnswers;
        const unanswered = totalStudents - totalAnswered;

        const responseTimes = answers.map(a => a.responseTime);
        const averageResponseTime = responseTimes.length > 0 
            ? Math.floor(responseTimes.reduce((sum, time) => sum + time, 0) / responseTimes.length)
            : 0;

        const statistics = {
            totalAnswered,
            correctAnswers,
            incorrectAnswers,
            unanswered,
            averageResponseTime,
            fastestTime: responseTimes.length > 0 ? Math.min(...responseTimes) : 0,
            slowestTime: responseTimes.length > 0 ? Math.max(...responseTimes) : 0,
            updateTime: Date.now()
        };

        this.interactiveManager.statistics.set(questionId, statistics);
        return statistics;
    }

    /**
     * 获取当前会话
     */
    async getCurrentSession(req, res) {
        try {
            const currentSessionId = this.interactiveManager.currentSession;
            if (!currentSessionId) {
                res.json({
                    success: true,
                    session: null,
                    message: '无活动会话'
                });
                return;
            }

            const session = this.interactiveManager.sessions.get(currentSessionId);
            res.json({
                success: true,
                session: session || null
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 结束当前会话
     */
    async endCurrentSession(req, res) {
        try {
            if (!this.interactiveManager.currentSession) {
                res.status(404).json({
                    success: false,
                    error: '没有活动会话'
                });
                return;
            }

            this.interactiveManager.currentSession = null;
            this.interactiveManager.currentQuestion = null;
            
            res.json({
                success: true,
                message: '会话已结束'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 清除会话数据
     */
    async clearSessionData(req, res) {
        try {
            this.interactiveManager.sessions.clear();
            this.interactiveManager.questions.clear();
            this.interactiveManager.answers.clear();
            this.interactiveManager.statistics.clear();
            this.interactiveManager.currentSession = null;
            this.interactiveManager.currentQuestion = null;
            
            // 重置学生状态
            for (const [seatId, student] of this.interactiveManager.students) {
                if (student.studentName) {
                    student.status = 'WAITING';
                    student.lastAnswer = null;
                    student.responseTime = null;
                    student.isCorrect = null;
                }
            }
            
            res.json({
                success: true,
                message: '会话数据已清空'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 获取当前题目
     */
    async getCurrentQuestion(req, res) {
        try {
            const currentQuestionId = this.interactiveManager.currentQuestion;
            if (!currentQuestionId) {
                res.json({
                    success: true,
                    data: null,
                    message: '没有当前题目'
                });
                return;
            }

            const question = this.interactiveManager.questions.get(currentQuestionId);
            res.json({
                success: true,
                data: question || null
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }

    /**
     * 停止当前题目
     */
    async stopCurrentQuestion(req, res) {
        try {
            if (!this.interactiveManager.currentQuestion) {
                res.status(404).json({
                    success: false,
                    error: '没有正在进行的题目'
                });
                return;
            }

            const questionId = this.interactiveManager.currentQuestion;
            this.interactiveManager.currentQuestion = null;
            
            // 更新统计信息
            this.updateQuestionStatistics(questionId);
            
            res.json({
                success: true,
                message: '题目已停止',
                questionId
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: error.message
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
