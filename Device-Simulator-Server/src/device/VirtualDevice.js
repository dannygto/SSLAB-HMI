/**
 * 虚拟设备类
 * 模拟ESP8266设备的行为和数据
 */

// 设备类型枚举 - SSLAB实验室设备类型定义
const DeviceType = {
    ENVIRONMENT_MONITOR: 'ENVIRONMENT_MONITOR',     // 环境检测装置
    STUDENT_POWER_TERMINAL: 'STUDENT_POWER_TERMINAL', // 学生电源终端  
    ENVIRONMENT_CONTROLLER: 'ENVIRONMENT_CONTROLLER', // 环境控制装置
    CURTAIN_CONTROLLER: 'CURTAIN_CONTROLLER',       // 窗帘控制装置
    LIGHTING_CONTROLLER: 'LIGHTING_CONTROLLER',     // 灯光控制装置
    LIFT_CONTROLLER: 'LIFT_CONTROLLER',             // 升降控制装置
    INTERACTIVE_STUDENT_TERMINAL: 'INTERACTIVE_STUDENT_TERMINAL', // 互动教学学生终端
    INTERACTIVE_DISPLAY: 'INTERACTIVE_DISPLAY',     // 互动教学显示设备
    INTERACTIVE_CONTROLLER: 'INTERACTIVE_CONTROLLER' // 互动教学控制器
};

// 设备状态枚举
const DeviceStatus = {
    ONLINE: 'ONLINE',
    OFFLINE: 'OFFLINE',
    ERROR: 'ERROR',
    MAINTENANCE: 'MAINTENANCE'
};

// SSLAB设备模型映射
const DeviceModels = {
    [DeviceType.ENVIRONMENT_MONITOR]: 'ESP8266-ENV-001',
    [DeviceType.STUDENT_POWER_TERMINAL]: 'ESP8266-PWR-002',
    [DeviceType.ENVIRONMENT_CONTROLLER]: 'ESP8266-CTRL-003',
    [DeviceType.CURTAIN_CONTROLLER]: 'ESP8266-CURTAIN-004',
    [DeviceType.LIGHTING_CONTROLLER]: 'ESP8266-LIGHT-005',
    [DeviceType.LIFT_CONTROLLER]: 'ESP8266-LIFT-006',
    [DeviceType.INTERACTIVE_STUDENT_TERMINAL]: 'ESP8266-INTERACTIVE-007',
    [DeviceType.INTERACTIVE_DISPLAY]: 'ESP8266-DISPLAY-008',
    [DeviceType.INTERACTIVE_CONTROLLER]: 'ESP8266-CTRL-009'
};

// SSLAB设备中文名称
const deviceNames = {
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

// SSLAB设备功能描述
const deviceDescriptions = {
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

class VirtualDevice {
    constructor(config) {
        // 如果传入的是对象配置，则解构；否则当作旧式参数处理
        if (typeof config === 'object' && config !== null) {
            this.id = config.id || this.generateId();
            this.name = config.name || this.generateDefaultName(config.type);
            this.type = config.type || DeviceType.ENVIRONMENT_MONITOR;
            this.groupId = config.groupId || null;
        } else {
            // 兼容旧式调用方式 (id, name, type, groupId)
            this.id = config || this.generateId();
            this.name = arguments[1] || this.generateDefaultName(arguments[2]);
            this.type = arguments[2] || DeviceType.ENVIRONMENT_MONITOR;
            this.groupId = arguments[3] || null;
        }
        
        this.model = DeviceModels[this.type] || 'ESP8266-UNKNOWN';
        this.macAddress = this.generateMacAddress();
        this.ipAddress = this.generateIPAddress();
        this.enabled = Math.random() > 0.3; // 70%概率在线
        this.status = this.enabled ? DeviceStatus.ONLINE : DeviceStatus.OFFLINE;
        this.lastSeen = new Date();
        this.createdAt = new Date();
        this.metadata = this.initializeMetadata();
        this.data = this.initializeData();
        this.capabilities = this.getCapabilities();
        
        // 启动数据模拟
        this.startDataSimulation();
    }

    /**
     * 生成设备ID
     */
    generateId() {
        return 'device_' + Math.random().toString(36).substr(2, 9);
    }

    /**
     * 生成默认设备名称
     */
    generateDefaultName(type) {
        const names = deviceNames;
        const baseName = names[type] || '未知设备';
        const randomNum = Math.floor(Math.random() * 100) + 1;
        return `${baseName}_${randomNum}`;
    }

    /**
     * 生成随机MAC地址
     */
    generateMacAddress() {
        const chars = '0123456789ABCDEF';
        let mac = '';
        for (let i = 0; i < 12; i++) {
            if (i > 0 && i % 2 === 0) mac += ':';
            mac += chars[Math.floor(Math.random() * 16)];
        }
        return mac;
    }

    /**
     * 生成随机IP地址
     */
    generateIPAddress() {
        // 生成192.168.1.x网段的IP地址
        const lastOctet = Math.floor(Math.random() * 200) + 50; // 50-249
        return `192.168.1.${lastOctet}`;
    }

    /**
     * 初始化设备元数据
     */
    initializeMetadata() {
        const baseMetadata = {
            manufacturer: 'SSLAB',
            model: this.model,
            firmware: '1.2.3',
            version: '1.0',
            description: this.getDeviceDescription(),
            serialNumber: this.generateSerialNumber(),
            created: this.createdAt,
            lastUpdate: new Date(),
            lastSeen: this.lastSeen
        };

        // 为互动教学设备添加特定的metadata
        if (this.type === DeviceType.INTERACTIVE_CONTROLLER) {
            baseMetadata.isActive = false;
            baseMetadata.currentQuestionId = null;
            baseMetadata.startTime = null;
            baseMetadata.timeLimit = 0;
            baseMetadata.autoNext = false;
            baseMetadata.totalStudents = 0;
            baseMetadata.answered = 0;
            baseMetadata.correct = 0;
            baseMetadata.incorrect = 0;
            baseMetadata.timeout = 0;
        } else if (this.type === DeviceType.INTERACTIVE_STUDENT_TERMINAL) {
            baseMetadata.seatId = null;
            baseMetadata.studentName = '';
            baseMetadata.status = 'EMPTY';
            baseMetadata.lastAnswer = null;
            baseMetadata.answerTime = null;
            baseMetadata.currentQuestionId = null;
            baseMetadata.selectedAnswer = null;
            baseMetadata.submitTime = null;
            baseMetadata.responseTime = null;
            baseMetadata.isCorrect = null;
        } else if (this.type === DeviceType.INTERACTIVE_DISPLAY) {
            baseMetadata.displayMode = 'QUESTION';
            baseMetadata.brightness = 80;
            baseMetadata.isFullscreen = false;
            baseMetadata.currentQuestionId = null;
            baseMetadata.questionContent = '';
            baseMetadata.options = [];
            baseMetadata.timeRemaining = 0;
            baseMetadata.isActive = false;
        }

        return baseMetadata;
    }
    
    /**
     * 获取设备描述
     */
    getDeviceDescription() {
        return deviceDescriptions[this.type] || '未知设备类型';
    }
    
    /**
     * 生成设备序列号
     */
    generateSerialNumber() {
        const timestamp = Date.now().toString(36);
        const random = Math.random().toString(36).substr(2, 4);
        return `SSLAB-${this.type}-${timestamp}-${random}`.toUpperCase();
    }

    /**
     * 初始化设备数据
     */
    initializeData() {
        // 如果metadata未初始化，先初始化它
        if (!this.metadata) {
            this.metadata = this.initializeMetadata();
        }
        
        const baseData = {
            model: this.model,
            macAddress: this.macAddress,
            ipAddress: this.ipAddress,
            firmware: '1.2.3',
            uptime: Math.floor(Math.random() * 86400000), // 随机运行时间(毫秒)
            rssi: Math.floor(Math.random() * -30) - 40, // WiFi信号强度 -70 到 -40 dBm
            freeHeap: Math.floor(Math.random() * 20000) + 10000, // 可用内存 10KB-30KB
            temperature: 25 + Math.random() * 20, // 芯片温度 25-45°C
            lastUpdate: new Date().toISOString(),
            network: {
                ipAddress: this.ipAddress,
                port: 80,
                gateway: '192.168.1.1',
                subnet: '255.255.255.0',
                dns: '8.8.8.8'
            }
        };

        // 根据SSLAB设备类型返回特定数据
        switch (this.type) {
            case DeviceType.ENVIRONMENT_MONITOR:
                return {
                    ...baseData,
                    sensors: {
                        temperature: 20 + Math.random() * 15, // 20-35°C
                        humidity: 40 + Math.random() * 40, // 40-80%
                        pressure: 1000 + Math.random() * 50, // 1000-1050 hPa
                        co2: 400 + Math.random() * 1000, // 400-1400 ppm
                        voc: Math.random() * 500, // 0-500 ppb
                        pm25: Math.random() * 50, // 0-50 μg/m³
                        pm10: Math.random() * 100, // 0-100 μg/m³
                        light: Math.random() * 1000, // 0-1000 lux
                        noise: 30 + Math.random() * 40 // 30-70 dB
                    },
                    display: {
                        enabled: true,
                        brightness: Math.floor(Math.random() * 100),
                        mode: 'auto', // auto, manual, off
                        rotation: 0 // 0, 90, 180, 270
                    },
                    alerts: {
                        temperatureHigh: false,
                        humidityHigh: false,
                        co2High: false,
                        pm25High: false
                    }
                };

            case DeviceType.STUDENT_POWER_TERMINAL:
                return {
                    ...baseData,
                    power: {
                        enabled: Math.random() > 0.5,
                        voltage: 12 + Math.random() * 3, // 12-15V (低压)
                        current: Math.random() * 2, // 0-2A
                        power: 0,
                        maxVoltage: 15,
                        maxCurrent: 2.5,
                        emergency: false,
                        locked: false // 是否被教师锁定
                    },
                    safety: {
                        overVoltage: false,
                        overCurrent: false,
                        shortCircuit: false,
                        emergencyStop: false
                    },
                    student: {
                        id: null,
                        name: null,
                        group: null
                    },
                    experiments: {
                        current: null,
                        history: []
                    }
                };

            case DeviceType.ENVIRONMENT_CONTROLLER:
                return {
                    ...baseData,
                    ventilation: {
                        enabled: Math.random() > 0.5,
                        speed: Math.floor(Math.random() * 100), // 0-100%
                        mode: 'auto', // auto, manual, timer
                        direction: 'in' // in, out, both
                    },
                    water: {
                        supply: Math.random() > 0.7,
                        drain: Math.random() > 0.8,
                        pressure: 2 + Math.random() * 3, // 2-5 bar
                        flow: Math.random() * 10 // 0-10 L/min
                    },
                    air: {
                        purifier: Math.random() > 0.6,
                        filter: Math.floor(Math.random() * 100), // 滤网寿命 0-100%
                        ionizer: Math.random() > 0.5
                    }
                };

            case DeviceType.CURTAIN_CONTROLLER:
                return {
                    ...baseData,
                    curtain: {
                        position: Math.floor(Math.random() * 100), // 0-100% (0=全开, 100=全关)
                        moving: false,
                        direction: 'stop', // open, close, stop
                        speed: 50, // 移动速度 1-100%
                        auto: false
                    },
                    motor: {
                        steps: Math.floor(Math.random() * 10000),
                        maxSteps: 10000,
                        torque: Math.floor(Math.random() * 100),
                        temperature: 30 + Math.random() * 20
                    },
                    schedule: {
                        enabled: false,
                        openTime: '07:00',
                        closeTime: '18:00',
                        lightSensor: true
                    }
                };

            case DeviceType.LIGHTING_CONTROLLER:
                return {
                    ...baseData,
                    lighting: {
                        enabled: Math.random() > 0.4,
                        brightness: Math.floor(Math.random() * 100), // 0-100%
                        colorTemperature: 3000 + Math.random() * 3000, // 3000K-6000K
                        mode: 'manual', // manual, auto, scene
                        scene: 'normal' // normal, reading, presentation, rest
                    },
                    zones: Array.from({length: 4}, (_, i) => ({
                        id: i + 1,
                        name: `灯光区域${i + 1}`,
                        enabled: Math.random() > 0.3,
                        brightness: Math.floor(Math.random() * 100),
                        colorTemperature: 3000 + Math.random() * 3000
                    })),
                    energy: {
                        power: Math.random() * 200, // 0-200W
                        daily: Math.random() * 5, // 每日用电 0-5 kWh
                        efficiency: 85 + Math.random() * 10 // 85-95%
                    }
                };

            case DeviceType.INTERACTIVE_STUDENT_TERMINAL:
                return {
                    ...baseData,
                    student: {
                        seatId: (this.metadata && this.metadata.seatId) || 'A1', // A1-D4
                        studentName: (this.metadata && this.metadata.studentName) || null,
                        status: (this.metadata && this.metadata.status) || 'EMPTY' // EMPTY, WAITING, ANSWERED, CORRECT, INCORRECT, TIMEOUT
                    },
                    answer: {
                        currentQuestionId: (this.metadata && this.metadata.currentQuestionId) || null,
                        selectedAnswer: (this.metadata && this.metadata.selectedAnswer) || null,
                        submitTime: (this.metadata && this.metadata.submitTime) || null,
                        responseTime: (this.metadata && this.metadata.responseTime) || null,
                        isCorrect: (this.metadata && this.metadata.isCorrect) || null
                    },
                    device: {
                        screenOn: true,
                        buttonLights: {
                            A: false,
                            B: false,
                            C: false,
                            D: false
                        },
                        buzzer: false,
                        networkSignal: 80 + Math.random() * 20 // 80-100%
                    }
                };

            case DeviceType.INTERACTIVE_DISPLAY:
                return {
                    ...baseData,
                    display: {
                        currentQuestionId: (this.metadata && this.metadata.currentQuestionId) || null,
                        questionContent: (this.metadata && this.metadata.questionContent) || '',
                        options: (this.metadata && this.metadata.options) || [],
                        timeRemaining: (this.metadata && this.metadata.timeRemaining) || 0,
                        isActive: (this.metadata && this.metadata.isActive) || false,
                        brightness: 85 + Math.random() * 15, // 85-100%
                        resolution: '1920x1080'
                    },
                    statistics: {
                        totalStudents: (this.metadata && this.metadata.totalStudents) || 0,
                        answered: (this.metadata && this.metadata.answered) || 0,
                        correct: (this.metadata && this.metadata.correct) || 0,
                        incorrect: (this.metadata && this.metadata.incorrect) || 0,
                        timeout: (this.metadata && this.metadata.timeout) || 0
                    }
                };

            case DeviceType.INTERACTIVE_CONTROLLER:
                return {
                    ...baseData,
                    session: {
                        isActive: (this.metadata && this.metadata.isActive) || false,
                        currentQuestionId: (this.metadata && this.metadata.currentQuestionId) || null,
                        startTime: (this.metadata && this.metadata.startTime) || null,
                        timeLimit: (this.metadata && this.metadata.timeLimit) || 0,
                        autoNext: (this.metadata && this.metadata.autoNext) || false
                    },
                    questions: {
                        total: (this.metadata && this.metadata.totalQuestions) || 0,
                        current: (this.metadata && this.metadata.currentIndex) || 0,
                        bank: (this.metadata && this.metadata.questionBank) || []
                    },
                    network: {
                        connectedTerminals: (this.metadata && this.metadata.connectedTerminals) || 0,
                        maxTerminals: 16,
                        signalQuality: 'excellent' // poor, fair, good, excellent
                    }
                };

            case DeviceType.LIFT_CONTROLLER:
                return {
                    ...baseData,
                    lift: {
                        position: Math.floor(Math.random() * 100), // 0-100% (0=最低, 100=最高)
                        moving: false,
                        direction: 'stop', // up, down, stop
                        speed: 10, // 移动速度 mm/s
                        load: Math.random() * 50 // 当前负载 0-50kg
                    },
                    motor: {
                        type: 'stepper',
                        steps: Math.floor(Math.random() * 20000),
                        maxSteps: 20000,
                        current: Math.random() * 2, // 0-2A
                        temperature: 25 + Math.random() * 25
                    },
                    safety: {
                        upperLimit: false,
                        lowerLimit: false,
                        overload: false,
                        emergency: false
                    },
                    precision: {
                        accuracy: 0.1, // mm
                        repeatability: 0.05 // mm
                    }
                };

            default:
                return baseData;
        }
    }

    /**
     * 获取设备能力
     */
    getCapabilities() {
        const base = ['status', 'restart', 'update'];
        
        switch (this.type) {
            case DeviceType.ENVIRONMENT_MONITOR:
                return [...base, 'readSensors', 'setDisplay', 'calibrate'];
            
            case DeviceType.STUDENT_POWER_TERMINAL:
                return [...base, 'turnOn', 'turnOff', 'setVoltage', 'setCurrent', 'emergency', 'lock', 'unlock'];
            
            case DeviceType.ENVIRONMENT_CONTROLLER:
                return [...base, 'setVentilation', 'controlWater', 'controlAir'];
            
            case DeviceType.CURTAIN_CONTROLLER:
                return [...base, 'open', 'close', 'setPosition', 'schedule'];
            
            case DeviceType.LIGHTING_CONTROLLER:
                return [...base, 'turnOn', 'turnOff', 'setBrightness', 'setColorTemperature', 'setScene'];
            
            case DeviceType.LIFT_CONTROLLER:
                return [...base, 'moveUp', 'moveDown', 'setPosition', 'stop', 'home'];
            
            case DeviceType.INTERACTIVE_STUDENT_TERMINAL:
                return [...base, 'assignStudent', 'submitAnswer', 'clearAnswer', 'setButtonLight', 'buzzer'];
            
            case DeviceType.INTERACTIVE_DISPLAY:
                return [...base, 'showQuestion', 'hideQuestion', 'updateStatistics', 'setBrightness'];
            
            case DeviceType.INTERACTIVE_CONTROLLER:
                return [...base, 'startSession', 'stopSession', 'publishQuestion', 'clearAnswers', 'getStatistics'];
            
            default:
                return base;
        }
    }

    /**
     * 启动数据模拟
     */
    startDataSimulation() {
        // 每30秒更新一次数据
        this.simulationInterval = setInterval(() => {
            if (this.enabled) {
                this.updateSimulatedData();
            }
        }, 30000);

        // 每5秒更新一次基础状态
        this.statusInterval = setInterval(() => {
            if (this.enabled) {
                this.updateBasicStatus();
            }
        }, 5000);
    }

    /**
     * 更新模拟数据
     */
    updateSimulatedData() {
        const now = new Date();
        this.lastSeen = now;
        this.data.lastUpdate = now.toISOString();
        this.data.uptime += 30000;
        this.data.rssi = Math.floor(Math.random() * -30) - 40;
        this.data.freeHeap = Math.floor(Math.random() * 20000) + 10000;
        this.data.temperature = 25 + Math.random() * 20;

        switch (this.type) {
            case DeviceType.SMART_SWITCH:
                if (this.data.switch.state === 'ON') {
                    this.data.switch.current = Math.random() * 5;
                    this.data.switch.power = this.data.switch.voltage * this.data.switch.current;
                    this.data.switch.energy += this.data.switch.power / 3600 / 1000; // 累计用电量
                } else {
                    this.data.switch.current = 0;
                    this.data.switch.power = 0;
                }
                this.data.switch.voltage = 220 + Math.random() * 20 - 10;
                break;

            case DeviceType.LED_CONTROLLER:
                if (this.data.led.mode === 'breathing') {
                    this.data.led.brightness = Math.floor(Math.sin(Date.now() / 1000) * 50) + 50;
                } else if (this.data.led.mode === 'rainbow') {
                    const hue = (Date.now() / 100) % 360;
                    const rgb = this.hslToRgb(hue / 360, 1, 0.5);
                    this.data.led.color = { red: rgb[0], green: rgb[1], blue: rgb[2] };
                }
                break;

            case DeviceType.SENSOR_NODE:
                // 模拟温湿度变化
                this.data.sensors.temperature.value += (Math.random() - 0.5) * 2;
                this.data.sensors.humidity.value += (Math.random() - 0.5) * 5;
                this.data.sensors.light.value = Math.max(0, this.data.sensors.light.value + (Math.random() - 0.5) * 100);
                
                // 确保数值在合理范围内
                this.data.sensors.temperature.value = Math.max(0, Math.min(50, this.data.sensors.temperature.value));
                this.data.sensors.humidity.value = Math.max(0, Math.min(100, this.data.sensors.humidity.value));
                
                // 随机运动检测
                if (Math.random() > 0.95) {
                    this.data.sensors.motion.detected = true;
                    this.data.sensors.motion.lastDetection = now.toISOString();
                } else if (Math.random() > 0.9) {
                    this.data.sensors.motion.detected = false;
                }
                break;

            case DeviceType.RELAY_MODULE:
                // 随机切换继电器状态（低概率）
                this.data.relays.forEach(relay => {
                    if (Math.random() > 0.98) {
                        relay.state = relay.state === 'ON' ? 'OFF' : 'ON';
                    }
                });
                break;
        }
    }

    /**
     * 更新基础状态
     */
    updateBasicStatus() {
        // 随机离线（很低概率）
        if (Math.random() > 0.999) {
            this.enabled = false;
            this.status = DeviceStatus.OFFLINE;
            setTimeout(() => {
                this.enabled = true;
                this.status = DeviceStatus.ONLINE;
            }, Math.random() * 60000); // 1分钟内恢复
        }
        
        // 更新最后seen时间
        if (this.enabled) {
            this.lastSeen = new Date();
        }
    }

    /**
     * HSL转RGB
     */
    hslToRgb(h, s, l) {
        let r, g, b;
        if (s === 0) {
            r = g = b = l;
        } else {
            const hue2rgb = (p, q, t) => {
                if (t < 0) t += 1;
                if (t > 1) t -= 1;
                if (t < 1/6) return p + (q - p) * 6 * t;
                if (t < 1/2) return q;
                if (t < 2/3) return p + (q - p) * (2/3 - t) * 6;
                return p;
            };
            const q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            const p = 2 * l - q;
            r = hue2rgb(p, q, h + 1/3);
            g = hue2rgb(p, q, h);
            b = hue2rgb(p, q, h - 1/3);
        }
        return [Math.round(r * 255), Math.round(g * 255), Math.round(b * 255)];
    }

    /**
     * 设备控制方法
     */
    control(command, params = {}) {
        const result = { success: false, message: '', data: null };

        if (!this.enabled) {
            result.message = '设备离线，无法执行命令';
            return result;
        }

        try {
            switch (command) {
                case 'turnOn':
                    this.turnOn();
                    result.success = true;
                    result.message = '设备已开启';
                    break;

                case 'turnOff':
                    this.turnOff();
                    result.success = true;
                    result.message = '设备已关闭';
                    break;

                case 'toggle':
                    this.toggle();
                    result.success = true;
                    result.message = '设备状态已切换';
                    break;

                case 'restart':
                    this.restart();
                    result.success = true;
                    result.message = '设备重启成功';
                    break;

                case 'setBrightness':
                    if (this.type === DeviceType.LED_CONTROLLER && params.brightness !== undefined) {
                        this.setBrightness(params.brightness);
                        result.success = true;
                        result.message = `亮度已设置为${params.brightness}%`;
                    } else {
                        result.message = '不支持的命令或参数错误';
                    }
                    break;

                case 'setColor':
                    if (this.type === DeviceType.LED_CONTROLLER && params.color) {
                        this.setColor(params.color);
                        result.success = true;
                        result.message = '颜色已设置';
                    } else {
                        result.message = '不支持的命令或参数错误';
                    }
                    break;

                case 'relayToggle':
                    if (this.type === DeviceType.RELAY_MODULE && params.relayId !== undefined) {
                        this.toggleRelay(params.relayId);
                        result.success = true;
                        result.message = `继电器${params.relayId}状态已切换`;
                    } else {
                        result.message = '不支持的命令或参数错误';
                    }
                    break;

                // 智能开关专用命令
                case 'setVoltageThreshold':
                    if (this.type === DeviceType.SMART_SWITCH && params.minVoltage && params.maxVoltage) {
                        this.setVoltageThreshold(params.minVoltage, params.maxVoltage);
                        result.success = true;
                        result.message = `电压阈值已设置: ${params.minVoltage}V - ${params.maxVoltage}V`;
                    } else {
                        result.message = '不支持的命令或参数错误';
                    }
                    break;

                case 'setSchedule':
                    if (this.type === DeviceType.SMART_SWITCH && params.onTime && params.offTime) {
                        this.setSchedule(params.onTime, params.offTime, params.weekdays);
                        result.success = true;
                        result.message = `定时计划已设置`;
                    } else {
                        result.message = '不支持的命令或参数错误';
                    }
                    break;

                case 'setCountdown':
                    if (this.type === DeviceType.SMART_SWITCH && params.minutes !== undefined) {
                        this.setCountdown(params.minutes);
                        result.success = true;
                        result.message = `倒计时已设置: ${params.minutes}分钟`;
                    } else {
                        result.message = '不支持的命令或参数错误';
                    }
                    break;

                // LED控制器专用命令
                case 'setColorTemperature':
                    if (this.type === DeviceType.LED_CONTROLLER && params.temperature !== undefined) {
                        this.setColorTemperature(params.temperature);
                        result.success = true;
                        result.message = `色温已设置: ${params.temperature}K`;
                    } else {
                        result.message = '不支持的命令或参数错误';
                    }
                    break;

                case 'setZone':
                    if (this.type === DeviceType.LED_CONTROLLER && params.zoneId && params.settings) {
                        this.setZone(params.zoneId, params.settings);
                        result.success = true;
                        result.message = `区域${params.zoneId}设置已更新`;
                    } else {
                        result.message = '不支持的命令或参数错误';
                    }
                    break;

                case 'setEffect':
                    if (this.type === DeviceType.LED_CONTROLLER && params.effect) {
                        this.setEffect(params.effect, params.duration);
                        result.success = true;
                        result.message = `特效已设置: ${params.effect}`;
                    } else {
                        result.message = '不支持的命令或参数错误';
                    }
                    break;

                // SSLAB学生电源终端专用命令
                case 'lock':
                    if (this.type === DeviceType.STUDENT_POWER_TERMINAL) {
                        this.data.power.locked = true;
                        result.success = true;
                        result.message = '设备已被教师锁定';
                    } else {
                        result.message = '不支持的命令';
                    }
                    break;

                case 'unlock':
                    if (this.type === DeviceType.STUDENT_POWER_TERMINAL) {
                        this.data.power.locked = false;
                        result.success = true;
                        result.message = '设备锁定已解除';
                    } else {
                        result.message = '不支持的命令';
                    }
                    break;

                case 'emergency':
                    if (this.type === DeviceType.STUDENT_POWER_TERMINAL) {
                        this.data.power.emergency = true;
                        this.data.power.enabled = false;
                        this.data.safety.emergencyStop = true;
                        result.success = true;
                        result.message = '紧急停止已激活';
                    } else {
                        result.message = '不支持的命令';
                    }
                    break;

                case 'setVoltage':
                    if (this.type === DeviceType.STUDENT_POWER_TERMINAL && params.voltage !== undefined) {
                        if (params.voltage <= this.data.power.maxVoltage) {
                            this.data.power.voltage = params.voltage;
                            result.success = true;
                            result.message = `电压已设置为 ${params.voltage}V`;
                        } else {
                            result.message = `电压超出限制 (最大 ${this.data.power.maxVoltage}V)`;
                        }
                    } else {
                        result.message = '不支持的命令或参数错误';
                    }
                    break;

                case 'setCurrent':
                    if (this.type === DeviceType.STUDENT_POWER_TERMINAL && params.current !== undefined) {
                        if (params.current <= this.data.power.maxCurrent) {
                            this.data.power.current = params.current;
                            result.success = true;
                            result.message = `电流已设置为 ${params.current}A`;
                        } else {
                            result.message = `电流超出限制 (最大 ${this.data.power.maxCurrent}A)`;
                        }
                    } else {
                        result.message = '不支持的命令或参数错误';
                    }
                    break;

                // SSLAB环境控制装置专用命令
                case 'setVentilation':
                    if (this.type === DeviceType.ENVIRONMENT_CONTROLLER && params.speed !== undefined) {
                        this.data.ventilation.speed = Math.max(0, Math.min(100, params.speed));
                        this.data.ventilation.enabled = params.speed > 0;
                        result.success = true;
                        result.message = `通风速度已设置为 ${params.speed}%`;
                    } else {
                        result.message = '不支持的命令或参数错误';
                    }
                    break;

                case 'controlWater':
                    if (this.type === DeviceType.ENVIRONMENT_CONTROLLER && params.action !== undefined) {
                        if (params.action === 'supply') {
                            this.data.water.supply = true;
                            result.message = '供水已开启';
                        } else if (params.action === 'drain') {
                            this.data.water.drain = true;
                            result.message = '排水已开启';
                        }
                        result.success = true;
                    } else {
                        result.message = '不支持的命令或参数错误';
                    }
                    break;

                // SSLAB窗帘控制器专用命令
                case 'open':
                    if (this.type === DeviceType.CURTAIN_CONTROLLER) {
                        this.data.curtain.position = 0;
                        this.data.curtain.direction = 'open';
                        result.success = true;
                        result.message = '窗帘已打开';
                    } else {
                        result.message = '不支持的命令';
                    }
                    break;

                case 'close':
                    if (this.type === DeviceType.CURTAIN_CONTROLLER) {
                        this.data.curtain.position = 100;
                        this.data.curtain.direction = 'close';
                        result.success = true;
                        result.message = '窗帘已关闭';
                    } else {
                        result.message = '不支持的命令';
                    }
                    break;

                case 'setPosition':
                    if (this.type === DeviceType.CURTAIN_CONTROLLER && params.position !== undefined) {
                        this.data.curtain.position = Math.max(0, Math.min(100, params.position));
                        result.success = true;
                        result.message = `窗帘位置已设置为 ${params.position}%`;
                    } else if (this.type === DeviceType.LIFT_CONTROLLER && params.position !== undefined) {
                        this.data.lift.position = Math.max(0, Math.min(100, params.position));
                        result.success = true;
                        result.message = `升降台位置已设置为 ${params.position}%`;
                    } else {
                        result.message = '不支持的命令或参数错误';
                    }
                    break;

                // SSLAB升降控制器专用命令
                case 'moveUp':
                    if (this.type === DeviceType.LIFT_CONTROLLER) {
                        this.data.lift.direction = 'up';
                        this.data.lift.moving = true;
                        result.success = true;
                        result.message = '升降台开始上升';
                    } else {
                        result.message = '不支持的命令';
                    }
                    break;

                case 'moveDown':
                    if (this.type === DeviceType.LIFT_CONTROLLER) {
                        this.data.lift.direction = 'down';
                        this.data.lift.moving = true;
                        result.success = true;
                        result.message = '升降台开始下降';
                    } else {
                        result.message = '不支持的命令';
                    }
                    break;

                case 'stop':
                    if (this.type === DeviceType.LIFT_CONTROLLER) {
                        this.data.lift.direction = 'stop';
                        this.data.lift.moving = false;
                        result.success = true;
                        result.message = '升降台已停止';
                    } else {
                        result.message = '不支持的命令';
                    }
                    break;

                case 'home':
                    if (this.type === DeviceType.LIFT_CONTROLLER) {
                        this.data.lift.position = 0;
                        this.data.lift.direction = 'stop';
                        this.data.lift.moving = false;
                        result.success = true;
                        result.message = '升降台已归位';
                    } else {
                        result.message = '不支持的命令';
                    }
                    break;

                default:
                    result.message = `未知命令: ${command}`;
            }

            if (result.success) {
                this.lastSeen = new Date();
                this.data.lastUpdate = new Date().toISOString();
            }

        } catch (error) {
            result.message = `执行命令失败: ${error.message}`;
        }

        return result;
    }

    /**
     * 开启设备
     */
    turnOn() {
        switch (this.type) {
            case DeviceType.SMART_SWITCH:
                this.data.switch.state = 'ON';
                break;
            case DeviceType.LED_CONTROLLER:
                this.data.led.state = 'ON';
                break;
        }
    }

    /**
     * 关闭设备
     */
    turnOff() {
        switch (this.type) {
            case DeviceType.SMART_SWITCH:
                this.data.switch.state = 'OFF';
                break;
            case DeviceType.LED_CONTROLLER:
                this.data.led.state = 'OFF';
                break;
        }
    }

    /**
     * 切换设备状态
     */
    toggle() {
        switch (this.type) {
            case DeviceType.SMART_SWITCH:
                this.data.switch.state = this.data.switch.state === 'ON' ? 'OFF' : 'ON';
                break;
            case DeviceType.LED_CONTROLLER:
                this.data.led.state = this.data.led.state === 'ON' ? 'OFF' : 'ON';
                break;
        }
    }

    /**
     * 重启设备
     */
    restart() {
        this.data.uptime = 0;
        this.data.freeHeap = Math.floor(Math.random() * 20000) + 10000;
        // 模拟重启过程
        setTimeout(() => {
            this.data = this.initializeData();
        }, 2000);
    }

    /**
     * 设置LED亮度
     */
    setBrightness(brightness) {
        if (this.type === DeviceType.LED_CONTROLLER) {
            this.data.led.brightness = Math.max(0, Math.min(100, brightness));
        }
    }

    /**
     * 设置开关电压阈值
     */
    setVoltageThreshold(minVoltage, maxVoltage) {
        if (this.type === DeviceType.SMART_SWITCH) {
            this.data.switch.minVoltage = Math.max(100, Math.min(300, minVoltage));
            this.data.switch.maxVoltage = Math.max(200, Math.min(400, maxVoltage));
            console.log(`设备 ${this.name} 电压阈值设置为: ${this.data.switch.minVoltage}V - ${this.data.switch.maxVoltage}V`);
        }
    }

    /**
     * 设置开关定时
     */
    setSchedule(onTime, offTime, weekdays = [1,2,3,4,5,6,7]) {
        if (this.type === DeviceType.SMART_SWITCH) {
            this.data.schedule = {
                enabled: true,
                onTime,
                offTime,
                weekdays,
                countdown: 0
            };
            console.log(`设备 ${this.name} 定时设置: ${onTime} - ${offTime}, 工作日: ${weekdays.join(',')}`);
        }
    }

    /**
     * 设置倒计时关闭
     */
    setCountdown(minutes) {
        if (this.type === DeviceType.SMART_SWITCH) {
            this.data.schedule.countdown = minutes;
            console.log(`设备 ${this.name} 倒计时关闭: ${minutes}分钟`);
            
            if (minutes > 0) {
                setTimeout(() => {
                    this.turnOff();
                    console.log(`设备 ${this.name} 倒计时关闭执行`);
                }, minutes * 60 * 1000);
            }
        }
    }

    /**
     * 设置LED颜色温度
     */
    setColorTemperature(temperature) {
        if (this.type === DeviceType.LED_CONTROLLER) {
            this.data.led.colorTemperature = Math.max(2700, Math.min(6500, temperature));
            console.log(`LED控制器 ${this.name} 色温设置为: ${this.data.led.colorTemperature}K`);
        }
    }

    /**
     * 设置LED分区控制
     */
    setZone(zoneId, settings) {
        if (this.type === DeviceType.LED_CONTROLLER) {
            const zone = this.data.zones.find(z => z.id === zoneId);
            if (zone) {
                if (settings.brightness !== undefined) zone.brightness = Math.max(0, Math.min(100, settings.brightness));
                if (settings.color) zone.color = settings.color;
                if (settings.enabled !== undefined) zone.enabled = settings.enabled;
                console.log(`LED控制器 ${this.name} 区域${zoneId} 设置更新`);
            }
        }
    }

    /**
     * 设置LED特效
     */
    setEffect(effectName, duration = 1000) {
        if (this.type === DeviceType.LED_CONTROLLER) {
            if (this.data.effects.available.includes(effectName)) {
                this.data.effects.current = effectName;
                this.data.effects.duration = duration;
                console.log(`LED控制器 ${this.name} 特效设置为: ${effectName}, 持续${duration}ms`);
            }
        }
    }
    setColor(color) {
        if (this.type === DeviceType.LED_CONTROLLER) {
            this.data.led.color = {
                red: Math.max(0, Math.min(255, color.red || 0)),
                green: Math.max(0, Math.min(255, color.green || 0)),
                blue: Math.max(0, Math.min(255, color.blue || 0))
            };
        }
    }

    /**
     * 切换继电器状态
     */
    toggleRelay(relayId) {
        if (this.type === DeviceType.RELAY_MODULE) {
            const relay = this.data.relays.find(r => r.id === relayId);
            if (relay) {
                relay.state = relay.state === 'ON' ? 'OFF' : 'ON';
            }
        }
    }

    /**
     * 停止数据模拟
     */
    stopSimulation() {
        if (this.simulationInterval) {
            clearInterval(this.simulationInterval);
            this.simulationInterval = null;
        }
        if (this.statusInterval) {
            clearInterval(this.statusInterval);
            this.statusInterval = null;
        }
    }

    /**
     * 获取设备状态
     */
    getStatus() {
        return {
            id: this.id,
            name: this.name,
            type: this.type,
            model: this.model,
            macAddress: this.macAddress,
            groupId: this.groupId,
            enabled: this.enabled,
            status: this.status,
            lastSeen: this.lastSeen,
            createdAt: this.createdAt,
            data: this.data,
            capabilities: this.capabilities,
            ipAddress: this.data.network?.ipAddress || '192.168.1.100',
            port: this.data.network?.port || 80,
            // 为前端兼容性添加properties和metadata字段
            properties: this.data,
            metadata: this.metadata
        };
    }

    /**
     * 获取设备状态（getStatus的别名）
     */
    getState() {
        return this.getStatus();
    }

    /**
     * 更新设备信息
     */
    updateInfo(updates) {
        if (updates.name) this.name = updates.name;
        if (updates.groupId !== undefined) this.groupId = updates.groupId;
        if (updates.enabled !== undefined) this.enabled = updates.enabled;
        
        // 更新metadata中的lastUpdate时间
        this.metadata.lastUpdate = new Date();
        if (this.enabled) {
            this.metadata.lastSeen = new Date();
        }
        
        return this.getStatus();
    }
}

module.exports = { VirtualDevice, DeviceType, DeviceStatus, DeviceModels };
