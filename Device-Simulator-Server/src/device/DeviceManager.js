const { VirtualDevice, DeviceType, DeviceStatus } = require('./VirtualDevice');

/**
 * 设备管理器
 * 负责管理所有虚拟设备
 */
class DeviceManager {
    constructor() {
        this.devices = new Map();
        this.groups = new Map();
        this.currentId = 1;
        this.discoveryService = null;
        this.eventListeners = new Set();
    }

    /**
     * 设置设备发现服务
     */
    setDiscoveryService(discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * 创建新设备
     */
    createDevice(type, name, metadata = {}) {
        const deviceId = 'd' + this.currentId.toString().padStart(3, '0');
        this.currentId++;
        
        const device = new VirtualDevice(deviceId, type, name, metadata);
        this.devices.set(deviceId, device);
        
        const deviceState = device.getState();
        
        // 通知发现服务
        if (this.discoveryService) {
            this.discoveryService.registerDevice(deviceState);
        }
        
        this.emitEvent('deviceCreated', deviceState);
        console.log(`🔌 新设备已创建: ${device.name} (${deviceId}) - ${type}`);
        
        return deviceState;
    }

    /**
     * 移除设备
     */
    removeDevice(deviceId) {
        const device = this.devices.get(deviceId);
        if (!device) {
            throw new Error(`设备不存在: ${deviceId}`);
        }
        
        const deviceState = device.getState();
        device.destroy();
        this.devices.delete(deviceId);
        
        this.emitEvent('deviceRemoved', deviceState);
        console.log(`🗑️ 设备已移除: ${device.name} (${deviceId})`);
        
        return deviceState;
    }
    
    /**
     * 获取设备状态
     */
    getDevice(deviceId) {
        const device = this.devices.get(deviceId);
        if (!device) {
            throw new Error(`设备不存在: ${deviceId}`);
        }
        return device.getState();
    }
    
    /**
     * 获取设备实例（用于内部操作）
     */
    getDeviceInstance(deviceId) {
        const device = this.devices.get(deviceId);
        if (!device) {
            throw new Error(`设备不存在: ${deviceId}`);
        }
        return device;
    }

    /**
     * 获取所有设备
     */
    getAllDevices() {
        return Array.from(this.devices.values()).map(device => device.getState());
    }
    
    /**
     * 添加设备
     */
    addDevice(config) {
        const device = new VirtualDevice(config);
        this.devices.set(device.id, device);
        
        this.emitEvent('deviceAdded', device.getState());
        console.log(`✅ 设备已添加: ${device.name} (${device.id})`);
        
        return device.getState();
    }
    
    /**
     * 移除设备
     */
    removeDevice(deviceId) {
        const device = this.devices.get(deviceId);
        if (!device) {
            throw new Error(`设备不存在: ${deviceId}`);
        }
        
        const deviceState = device.getState();
        device.destroy();
        this.devices.delete(deviceId);
        
        this.emitEvent('deviceRemoved', deviceState);
        console.log(`🗑️ 设备已移除: ${device.name} (${deviceId})`);
        
        return deviceState;
    }
    
    /**
     * 获取设备状态
     */
    getDevice(deviceId) {
        const device = this.devices.get(deviceId);
        if (!device) {
            throw new Error(`设备不存在: ${deviceId}`);
        }
        return device.getState();
    }
    
    /**
     * 获取设备实例（用于内部操作）
     */
    getDeviceInstance(deviceId) {
        const device = this.devices.get(deviceId);
        if (!device) {
            throw new Error(`设备不存在: ${deviceId}`);
        }
        return device;
    }
    
    /**
     * 获取所有设备
     */
    getAllDevices() {
        return Array.from(this.devices.values()).map(device => device.getState());
    }
    
    /**
     * 根据分组获取设备
     */
    getDevicesByGroup(groupId) {
        return Array.from(this.devices.values())
            .filter(device => device.groupId === groupId)
            .map(device => device.getState());
    }
    
    /**
     * 根据类型获取设备
     */
    getDevicesByType(deviceType) {
        return Array.from(this.devices.values())
            .filter(device => device.type === deviceType)
            .map(device => device.getState());
    }
    
    /**
     * 控制设备
     */
    controlDevice(deviceId, command, params) {
        const device = this.devices.get(deviceId);
        if (!device) {
            throw new Error(`设备不存在: ${deviceId}`);
        }
        
        const result = device.control(command, params);
        this.emitEvent('deviceControlled', {
            deviceId,
            command,
            params,
            result
        });
        
        return result;
    }
    
    /**
     * 批量控制设备
     */
    controlDevices(deviceIds, command, params) {
        const results = [];
        const errors = [];
        
        for (const deviceId of deviceIds) {
            try {
                const result = this.controlDevice(deviceId, command, params);
                results.push({ deviceId, success: true, result });
            } catch (error) {
                errors.push({ deviceId, success: false, error: error.message });
            }
        }
        
        return { results, errors };
    }
    
    /**
     * 按分组控制设备
     */
    controlDevicesByGroup(groupId, command, params) {
        const groupDevices = Array.from(this.devices.values())
            .filter(device => device.groupId === groupId);
        
        const deviceIds = groupDevices.map(device => device.id);
        return this.controlDevices(deviceIds, command, params);
    }
    
    /**
     * 更新设备配置
     */
    updateDevice(deviceId, config) {
        const device = this.devices.get(deviceId);
        if (!device) {
            throw new Error(`设备不存在: ${deviceId}`);
        }
        
        const result = device.updateConfig(config);
        this.emitEvent('deviceUpdated', result);
        
        return result;
    }
    
    /**
     * 获取设备统计信息
     */
    getDeviceStats() {
        const allDevices = Array.from(this.devices.values());
        
        const stats = {
            total: allDevices.length,
            online: allDevices.filter(d => d.enabled).length,
            offline: allDevices.filter(d => !d.enabled).length,
            byType: {},
            byGroup: {}
        };
        
        // 按类型统计
        for (const type of Object.values(DeviceType)) {
            const typeDevices = allDevices.filter(d => d.type === type);
            stats.byType[type] = {
                total: typeDevices.length,
                online: typeDevices.filter(d => d.enabled).length
            };
        }
        
        // 按分组统计
        const groups = new Set(allDevices.map(d => d.groupId).filter(g => g));
        for (const groupId of groups) {
            const groupDevices = allDevices.filter(d => d.groupId === groupId);
            stats.byGroup[groupId] = {
                total: groupDevices.length,
                online: groupDevices.filter(d => d.enabled).length,
                name: this.groups.get(groupId)?.name || groupId
            };
        }
        
        return stats;
    }
    
    /**
     * 获取分组配置
     */
    getGroupConfigs() {
        return Array.from(this.groups.values());
    }
    
    /**
     * 获取分组列表（getGroupConfigs的别名）
     */
    getGroups() {
        return this.getGroupConfigs();
    }
    
    /**
     * 获取单个分组配置
     */
    getGroup(groupId) {
        return this.groups.get(groupId);
    }
    
    /**
     * 获取设备数量
     */
    getDeviceCount() {
        return this.devices.size;
    }
    
    /**
     * 获取分组数量
     */
    getGroupCount() {
        return this.groups.size;
    }
    
    /**
     * 添加分组配置
     */
    addGroupConfig(config) {
        this.groups.set(config.groupId, config);
        this.emitEvent('groupAdded', config);
        return config;
    }
    
    /**
     * 添加分组（API使用）
     */
    addGroup(groupId, name, description = '') {
        const config = {
            groupId,
            name,
            description,
            building: '',
            floor: 0,
            room: ''
        };
        return this.addGroupConfig(config);
    }
    
    /**
     * 更新分组配置
     */
    updateGroupConfig(groupId, config) {
        if (!this.groups.has(groupId)) {
            throw new Error(`分组不存在: ${groupId}`);
        }
        
        const updatedConfig = { ...this.groups.get(groupId), ...config };
        this.groups.set(groupId, updatedConfig);
        this.emitEvent('groupUpdated', updatedConfig);
        
        return updatedConfig;
    }
    
    /**
     * 删除分组配置
     */
    removeGroupConfig(groupId) {
        if (!this.groups.has(groupId)) {
            throw new Error(`分组不存在: ${groupId}`);
        }
        
        // 检查是否有设备在使用此分组
        const groupDevices = Array.from(this.devices.values())
            .filter(device => device.groupId === groupId);
        
        if (groupDevices.length > 0) {
            throw new Error(`分组 ${groupId} 仍有 ${groupDevices.length} 个设备在使用，无法删除`);
        }
        
        const config = this.groups.get(groupId);
        this.groups.delete(groupId);
        this.emitEvent('groupRemoved', config);
        
        return config;
    }
    
    /**
     * 获取设备数量
     */
    getDeviceCount() {
        return this.devices.size;
    }
    
    /**
     * 搜索设备
     */
    searchDevices(query) {
        const lowerQuery = query.toLowerCase();
        return Array.from(this.devices.values())
            .filter(device => 
                device.name.toLowerCase().includes(lowerQuery) ||
                device.type.toLowerCase().includes(lowerQuery) ||
                device.groupId.toLowerCase().includes(lowerQuery) ||
                device.ipAddress.includes(query)
            )
            .map(device => device.getState());
    }
    
    /**
     * 添加事件监听器
     */
    addEventListener(listener) {
        this.eventListeners.add(listener);
    }
    
    /**
     * 移除事件监听器
     */
    removeEventListener(listener) {
        this.eventListeners.delete(listener);
    }
    
    /**
     * 发送事件
     */
    emitEvent(event, data) {
        for (const listener of this.eventListeners) {
            try {
                listener(event, data);
            } catch (error) {
                console.error('事件监听器错误:', error);
            }
        }
    }
    
    /**
     * 清空所有设备
     */
    clearAllDevices() {
        const deviceIds = Array.from(this.devices.keys());
        for (const deviceId of deviceIds) {
            this.removeDevice(deviceId);
        }
        console.log('🧹 所有设备已清空');
    }
    
    /**
     * 重置系统
     */
    reset() {
        this.resetDeviceStates();
        console.log('🔄 系统已重置');
    }
    
    /**
     * 重置设备状态
     */
    resetDeviceStates() {
        for (const device of this.devices.values()) {
            // 重置设备为离线状态
            device.enabled = false;
            // 重新启动数据模拟
            if (device.simulationInterval) {
                clearInterval(device.simulationInterval);
            }
            if (device.statusInterval) {
                clearInterval(device.statusInterval);
            }
            device.startDataSimulation();
        }
        console.log('🔄 所有设备状态已重置');
    }
}

module.exports = DeviceManager;
