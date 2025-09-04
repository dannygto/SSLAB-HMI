/**
 * WebSocket处理器
 * 负责实时通信和事件推送
 */
class WebSocketHandler {
    constructor(io, deviceManager) {
        this.io = io;
        this.deviceManager = deviceManager;
        this.connectedClients = new Map();
    }
    
    /**
     * 初始化WebSocket处理
     */
    initialize() {
        this.io.on('connection', (socket) => {
            console.log(`📱 客户端连接: ${socket.id}`);
            
            // 保存客户端信息
            this.connectedClients.set(socket.id, {
                socket,
                connectedAt: new Date(),
                subscriptions: new Set()
            });
            
            // 发送初始数据
            this.sendInitialData(socket);
            
            // 设置事件处理器
            this.setupSocketHandlers(socket);
            
            // 断开连接处理
            socket.on('disconnect', () => {
                console.log(`📱 客户端断开: ${socket.id}`);
                this.connectedClients.delete(socket.id);
            });
        });
        
        // 监听设备管理器事件
        this.deviceManager.addEventListener(this.handleDeviceEvent.bind(this));
        
        // 定期发送统计信息
        this.startStatsTimer();
    }
    
    /**
     * 发送初始数据
     */
    sendInitialData(socket) {
        try {
            // 只发送真实存在的数据
            const devices = this.deviceManager.getAllDevices();
            const groups = this.deviceManager.getGroupConfigs();
            const stats = this.deviceManager.getDeviceStats();
            
            // 只有当存在设备时才发送设备列表
            if (devices.length > 0) {
                socket.emit('devices:list', devices);
            }
            
            // 只有当存在分组时才发送分组列表
            if (groups.length > 0) {
                socket.emit('groups:list', groups);
            }
            
            // 始终发送统计信息（即使为0）
            socket.emit('stats:update', stats);
            
            console.log(`📊 已向客户端 ${socket.id} 发送初始数据 (设备: ${devices.length}, 分组: ${groups.length})`);
        } catch (error) {
            console.error('发送初始数据失败:', error);
            socket.emit('error', { message: '获取初始数据失败' });
        }
    }
    
    /**
     * 设置Socket事件处理器
     */
    setupSocketHandlers(socket) {
        const client = this.connectedClients.get(socket.id);
        
        // 订阅事件
        socket.on('subscribe', (data) => {
            try {
                const { events } = data;
                if (Array.isArray(events)) {
                    events.forEach(event => client.subscriptions.add(event));
                    socket.emit('subscribe:success', { events });
                    console.log(`📡 客户端 ${socket.id} 订阅事件:`, events);
                }
            } catch (error) {
                socket.emit('subscribe:error', { error: error.message });
            }
        });
        
        // 取消订阅
        socket.on('unsubscribe', (data) => {
            try {
                const { events } = data;
                if (Array.isArray(events)) {
                    events.forEach(event => client.subscriptions.delete(event));
                    socket.emit('unsubscribe:success', { events });
                }
            } catch (error) {
                socket.emit('unsubscribe:error', { error: error.message });
            }
        });
        
        // 获取设备列表
        socket.on('devices:get', (data = {}) => {
            try {
                const { groupId, type } = data;
                let devices;
                
                if (groupId) {
                    devices = this.deviceManager.getDevicesByGroup(groupId);
                } else if (type) {
                    devices = this.deviceManager.getDevicesByType(type);
                } else {
                    devices = this.deviceManager.getAllDevices();
                }
                
                socket.emit('devices:list', devices);
            } catch (error) {
                socket.emit('devices:error', { error: error.message });
            }
        });
        
        // 添加设备
        socket.on('device:add', (data) => {
            try {
                const device = this.deviceManager.addDevice(data);
                socket.emit('device:add:success', device);
            } catch (error) {
                socket.emit('device:add:error', { error: error.message });
            }
        });
        
        // 更新设备
        socket.on('device:update', (data) => {
            try {
                const { id, config } = data;
                const device = this.deviceManager.updateDevice(id, config);
                socket.emit('device:update:success', device);
            } catch (error) {
                socket.emit('device:update:error', { error: error.message });
            }
        });
        
        // 删除设备
        socket.on('device:remove', (data) => {
            try {
                const { id } = data;
                const device = this.deviceManager.removeDevice(id);
                socket.emit('device:remove:success', device);
            } catch (error) {
                socket.emit('device:remove:error', { error: error.message });
            }
        });
        
        // 控制设备
        socket.on('device:control', (data) => {
            try {
                const { id, command, params } = data;
                const result = this.deviceManager.controlDevice(id, command, params || {});
                socket.emit('device:control:success', { id, command, result });
            } catch (error) {
                socket.emit('device:control:error', { 
                    id: data.id, 
                    command: data.command, 
                    error: error.message 
                });
            }
        });
        
        // 批量控制设备
        socket.on('devices:control:batch', (data) => {
            try {
                const { deviceIds, command, params } = data;
                const result = this.deviceManager.controlDevices(deviceIds, command, params || {});
                socket.emit('devices:control:batch:success', result);
            } catch (error) {
                socket.emit('devices:control:batch:error', { error: error.message });
            }
        });
        
        // 分组控制
        socket.on('group:control', (data) => {
            try {
                const { groupId, command, params } = data;
                const result = this.deviceManager.controlDevicesByGroup(groupId, command, params || {});
                socket.emit('group:control:success', result);
            } catch (error) {
                socket.emit('group:control:error', { error: error.message });
            }
        });
        
        // 获取统计信息
        socket.on('stats:get', () => {
            try {
                const stats = this.deviceManager.getDeviceStats();
                socket.emit('stats:update', stats);
            } catch (error) {
                socket.emit('stats:error', { error: error.message });
            }
        });
        
        // 搜索设备
        socket.on('devices:search', (data) => {
            try {
                const { query } = data;
                const devices = this.deviceManager.searchDevices(query);
                socket.emit('devices:search:result', devices);
            } catch (error) {
                socket.emit('devices:search:error', { error: error.message });
            }
        });
        
        // 分组管理
        socket.on('groups:get', () => {
            try {
                const groups = this.deviceManager.getGroupConfigs();
                socket.emit('groups:list', groups);
            } catch (error) {
                socket.emit('groups:error', { error: error.message });
            }
        });
        
        socket.on('group:add', (data) => {
            try {
                const group = this.deviceManager.addGroupConfig(data);
                socket.emit('group:add:success', group);
            } catch (error) {
                socket.emit('group:add:error', { error: error.message });
            }
        });
        
        socket.on('group:update', (data) => {
            try {
                const { groupId, config } = data;
                const group = this.deviceManager.updateGroupConfig(groupId, config);
                socket.emit('group:update:success', group);
            } catch (error) {
                socket.emit('group:update:error', { error: error.message });
            }
        });
        
        socket.on('group:remove', (data) => {
            try {
                const { groupId } = data;
                const group = this.deviceManager.removeGroupConfig(groupId);
                socket.emit('group:remove:success', group);
            } catch (error) {
                socket.emit('group:remove:error', { error: error.message });
            }
        });
        
        // 系统管理
        socket.on('system:reset', () => {
            try {
                this.deviceManager.resetDeviceStates();
                socket.emit('system:reset:success');
            } catch (error) {
                socket.emit('system:reset:error', { error: error.message });
            }
        });
        
        socket.on('system:clear', () => {
            try {
                this.deviceManager.clearAllDevices();
                socket.emit('system:clear:success');
            } catch (error) {
                socket.emit('system:clear:error', { error: error.message });
            }
        });
    }
    
    /**
     * 处理设备管理器事件
     */
    handleDeviceEvent(event, data) {
        console.log(`📡 广播事件: ${event}`);
        
        // 向所有客户端广播事件
        this.io.emit(`device:${event}`, data);
        
        // 如果是设备状态变化，更新统计信息
        if (['deviceAdded', 'deviceRemoved', 'deviceControlled'].includes(event)) {
            const stats = this.deviceManager.getDeviceStats();
            this.io.emit('stats:update', stats);
        }
        
        // 如果是分组相关事件
        if (event.startsWith('group')) {
            const groups = this.deviceManager.getGroupConfigs();
            this.io.emit('groups:list', groups);
        }
    }
    
    /**
     * 启动统计信息定时器
     */
    startStatsTimer() {
        // 每10秒发送一次统计信息
        setInterval(() => {
            try {
                const stats = this.deviceManager.getDeviceStats();
                this.io.emit('stats:update', stats);
                
                // 发送连接信息
                const connectionInfo = {
                    connectedClients: this.connectedClients.size,
                    timestamp: new Date().toISOString()
                };
                this.io.emit('connection:info', connectionInfo);
            } catch (error) {
                console.error('发送统计信息失败:', error);
            }
        }, 10000);
    }
    
    /**
     * 向特定客户端发送消息
     */
    sendToClient(clientId, event, data) {
        const client = this.connectedClients.get(clientId);
        if (client && client.socket) {
            client.socket.emit(event, data);
            return true;
        }
        return false;
    }
    
    /**
     * 向订阅了特定事件的客户端发送消息
     */
    sendToSubscribers(event, data) {
        let count = 0;
        for (const [clientId, client] of this.connectedClients) {
            if (client.subscriptions.has(event)) {
                client.socket.emit(event, data);
                count++;
            }
        }
        return count;
    }
    
    /**
     * 获取连接状态
     */
    getConnectionStatus() {
        return {
            totalClients: this.connectedClients.size,
            clients: Array.from(this.connectedClients.entries()).map(([id, client]) => ({
                id,
                connectedAt: client.connectedAt,
                subscriptions: Array.from(client.subscriptions)
            }))
        };
    }
    
    /**
     * 断开所有客户端
     */
    disconnectAllClients() {
        for (const [clientId, client] of this.connectedClients) {
            client.socket.disconnect(true);
        }
        this.connectedClients.clear();
        console.log('📱 所有客户端已断开连接');
    }
}

module.exports = WebSocketHandler;
