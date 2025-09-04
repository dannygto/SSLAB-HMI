const dgram = require('dgram');

/**
 * 设备发现服务
 * 模拟mDNS和UDP广播发现
 */
class DiscoveryService {
    constructor(deviceManager) {
        this.deviceManager = deviceManager;
        this.udpServer = null;
        this.broadcastTimer = null;
        this.port = 12345;
        this.isRunning = false;
    }
    
    /**
     * 启动发现服务
     */
    start() {
        if (this.isRunning) {
            console.log('🔍 设备发现服务已经在运行');
            return;
        }
        
        try {
            this.setupUDPServer();
            this.startBroadcast();
            this.isRunning = true;
            console.log(`🔍 设备发现服务已启动 (UDP:${this.port})`);
        } catch (error) {
            console.error('启动设备发现服务失败:', error);
        }
    }
    
    /**
     * 停止发现服务
     */
    stop() {
        if (!this.isRunning) {
            return;
        }
        
        try {
            if (this.broadcastTimer) {
                clearInterval(this.broadcastTimer);
                this.broadcastTimer = null;
            }
            
            if (this.udpServer) {
                this.udpServer.close();
                this.udpServer = null;
            }
            
            this.isRunning = false;
            console.log('🔍 设备发现服务已停止');
        } catch (error) {
            console.error('停止设备发现服务失败:', error);
        }
    }
    
    /**
     * 设置UDP服务器
     */
    setupUDPServer() {
        this.udpServer = dgram.createSocket('udp4');
        
        this.udpServer.on('listening', () => {
            const address = this.udpServer.address();
            console.log(`📡 UDP服务器监听: ${address.address}:${address.port}`);
            
            // 启用广播
            this.udpServer.setBroadcast(true);
        });
        
        this.udpServer.on('message', (message, remote) => {
            try {
                const request = JSON.parse(message.toString());
                this.handleDiscoveryRequest(request, remote);
            } catch (error) {
                console.error('处理发现请求失败:', error);
            }
        });
        
        this.udpServer.on('error', (error) => {
            console.error('UDP服务器错误:', error);
        });
        
        this.udpServer.bind(this.port);
    }
    
    /**
     * 处理设备发现请求
     */
    handleDiscoveryRequest(request, remote) {
        console.log(`🔍 收到发现请求来自 ${remote.address}:${remote.port}`, request);
        
        const { type, groupId, deviceType } = request;
        
        if (type === 'discover') {
            let devices = this.deviceManager.getAllDevices();
            
            // 按分组过滤
            if (groupId) {
                devices = devices.filter(device => device.groupId === groupId);
            }
            
            // 按设备类型过滤
            if (deviceType) {
                devices = devices.filter(device => device.type === deviceType);
            }
            
            // 构建响应
            const response = {
                type: 'discovery_response',
                timestamp: new Date().toISOString(),
                devices: devices.map(device => this.createDiscoveryInfo(device)),
                total: devices.length
            };
            
            // 发送响应
            const responseBuffer = Buffer.from(JSON.stringify(response));
            this.udpServer.send(responseBuffer, remote.port, remote.address, (error) => {
                if (error) {
                    console.error('发送发现响应失败:', error);
                } else {
                    console.log(`📤 已发送发现响应到 ${remote.address}:${remote.port} (${devices.length}个设备)`);
                }
            });
        }
    }
    
    /**
     * 创建设备发现信息
     */
    createDiscoveryInfo(device) {
        return {
            id: device.id,
            name: device.name,
            type: device.type,
            groupId: device.groupId,
            ipAddress: device.ipAddress,
            macAddress: device.macAddress,
            port: device.port,
            status: device.status,
            services: this.getDeviceServices(device),
            txtRecords: this.generateTxtRecords(device),
            lastSeen: device.metadata.lastSeen
        };
    }
    
    /**
     * 获取设备服务
     */
    getDeviceServices(device) {
        const baseServices = [
            {
                name: 'http',
                port: device.port,
                protocol: 'tcp',
                description: 'HTTP API服务'
            },
            {
                name: 'websocket',
                port: device.port + 1,
                protocol: 'tcp', 
                description: 'WebSocket实时通信'
            }
        ];
        
        // 根据设备类型添加特定服务
        switch (device.type) {
            case 'ENVIRONMENT_MONITOR':
                baseServices.push({
                    name: 'environment-data',
                    port: device.port + 2,
                    protocol: 'udp',
                    description: '环境监测数据推送'
                });
                break;
                
            case 'STUDENT_POWER_TERMINAL':
                baseServices.push({
                    name: 'power-control',
                    port: device.port + 3,
                    protocol: 'tcp',
                    description: '电源控制服务'
                });
                break;
                
            case 'ENVIRONMENT_CONTROLLER':
                baseServices.push({
                    name: 'env-control',
                    port: device.port + 4,
                    protocol: 'tcp',
                    description: '环境控制服务'
                });
                break;
                
            case 'CURTAIN_CONTROLLER':
                baseServices.push({
                    name: 'curtain-control',
                    port: device.port + 5,
                    protocol: 'tcp',
                    description: '窗帘控制服务'
                });
                break;
                
            case 'LIGHTING_CONTROLLER':
                baseServices.push({
                    name: 'lighting-control',
                    port: device.port + 6,
                    protocol: 'tcp',
                    description: '灯光控制服务'
                });
                break;
                
            case 'LIFT_CONTROLLER':
                baseServices.push({
                    name: 'lift-control',
                    port: device.port + 7,
                    protocol: 'tcp',
                    description: '升降控制服务'
                });
                break;
        }
        
        return baseServices;
    }
    
    /**
     * 生成TXT记录
     */
    generateTxtRecords(device) {
        const metadata = device.metadata || {};
        return {
            version: '1.0',
            manufacturer: metadata.manufacturer || 'SSLAB',
            model: metadata.model || device.model || 'Unknown',
            firmware: metadata.firmware || '1.0.0',
            groupId: device.groupId || '',
            deviceType: device.type || 'UNKNOWN',
            capabilities: this.getDeviceCapabilities(device).join(','),
            api: `http://${device.ipAddress || '192.168.1.100'}:${device.port || 80}/api`,
            websocket: `ws://${device.ipAddress || '192.168.1.100'}:${(device.port || 80) + 1}`
        };
    }
    
    /**
     * 获取设备能力
     */
    getDeviceCapabilities(device) {
        const capabilities = ['status', 'control', 'config'];
        
        switch (device.type) {
            case 'ENVIRONMENT_MONITOR':
                capabilities.push('environment_sensor', 'temperature', 'humidity', 'air_quality', 'power_monitor');
                break;
                
            case 'STUDENT_POWER_TERMINAL':
                capabilities.push('power_control', 'voltage_adjust', 'current_limit', 'output_switch');
                break;
                
            case 'ENVIRONMENT_CONTROLLER':
                capabilities.push('water_supply', 'ventilation', 'environment_control');
                break;
                
            case 'CURTAIN_CONTROLLER':
                capabilities.push('curtain_control', 'area_control');
                break;
                
            case 'LIGHTING_CONTROLLER':
                capabilities.push('lighting_control', 'blackboard_light', 'classroom_light');
                break;
                
            case 'LIFT_CONTROLLER':
                capabilities.push('lift_control', 'position_control', 'computer_switch');
                break;
        }
        
        return capabilities;
    }
    
    /**
     * 启动定期广播
     */
    startBroadcast() {
        // 每30秒广播一次设备信息
        this.broadcastTimer = setInterval(() => {
            this.broadcastDevices();
        }, 30000);
        
        // 立即广播一次
        this.broadcastDevices();
    }
    
    /**
     * 广播设备信息
     */
    broadcastDevices() {
        try {
            const devices = this.deviceManager.getAllDevices();
            const onlineDevices = devices.filter(device => device.status === 'ONLINE');
            
            if (onlineDevices.length === 0) {
                return;
            }
            
            const broadcast = {
                type: 'device_broadcast',
                timestamp: new Date().toISOString(),
                devices: onlineDevices.map(device => this.createDiscoveryInfo(device)),
                total: onlineDevices.length
            };
            
            const broadcastBuffer = Buffer.from(JSON.stringify(broadcast));
            
            // 广播到255.255.255.255
            this.udpServer.send(broadcastBuffer, this.port, '255.255.255.255', (error) => {
                if (error) {
                    console.error('广播设备信息失败:', error);
                } else {
                    console.log(`📡 已广播 ${onlineDevices.length} 个在线设备信息`);
                }
            });
            
        } catch (error) {
            console.error('广播设备失败:', error);
        }
    }
    
    /**
     * 发送设备状态变更通知
     */
    notifyDeviceChange(device, changeType) {
        if (!this.isRunning || !this.udpServer) {
            return;
        }
        
        try {
            const notification = {
                type: 'device_change',
                changeType: changeType, // 'added', 'removed', 'updated', 'status_changed'
                timestamp: new Date().toISOString(),
                device: this.createDiscoveryInfo(device)
            };
            
            const notificationBuffer = Buffer.from(JSON.stringify(notification));
            
            this.udpServer.send(notificationBuffer, this.port, '255.255.255.255', (error) => {
                if (error) {
                    console.error('发送设备变更通知失败:', error);
                } else {
                    console.log(`📤 已发送设备变更通知: ${device.name} (${changeType})`);
                }
            });
            
        } catch (error) {
            console.error('发送设备变更通知失败:', error);
        }
    }
    
    /**
     * 获取发现服务状态
     */
    getStatus() {
        return {
            isRunning: this.isRunning,
            port: this.port,
            deviceCount: this.deviceManager.getDeviceCount(),
            uptime: this.isRunning ? Date.now() - this.startTime : 0
        };
    }
}

module.exports = DiscoveryService;
