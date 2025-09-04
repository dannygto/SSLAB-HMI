const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');
const bodyParser = require('body-parser');
const path = require('path');

const DeviceManager = require('./device/DeviceManager');
const ApiRouter = require('./api/ApiRouter');
const WebSocketHandler = require('./websocket/WebSocketHandler');
const DiscoveryService = require('./discovery/DiscoveryService');

/**
 * SSLAB智能硬件设备模拟器服务器
 * 提供Web界面管理设备和REST API接口
 */
class DeviceSimulatorServer {
    constructor() {
        this.app = express();
        this.server = http.createServer(this.app);
        this.io = socketIo(this.server, {
            cors: {
                origin: "*",
                methods: ["GET", "POST"]
            }
        });
        
        this.port = process.env.PORT || 8080;
        this.deviceManager = new DeviceManager();
        this.discoveryService = new DiscoveryService(this.deviceManager);
        
        // 统计信息
        this.connectedClients = 0;
        this.apiCallsCount = 0;
        
        this.setupMiddleware();
        this.setupRoutes();
        this.setupWebSocket();
        this.setupDiscovery();
    }
    
    /**
     * 设置中间件
     */
    setupMiddleware() {
        this.app.use(cors());
        this.app.use(bodyParser.json());
        this.app.use(bodyParser.urlencoded({ extended: true }));
        
        // 静态文件服务
        this.app.use(express.static(path.join(__dirname, '../public')));
        
        // 日志中间件
        this.app.use((req, res, next) => {
            console.log(`${new Date().toISOString()} - ${req.method} ${req.url}`);
            
            // 统计API调用次数
            if (req.url.startsWith('/api/')) {
                this.apiCallsCount++;
            }
            
            next();
        });
    }
    
    /**
     * 设置路由
     */
    setupRoutes() {
        // API路由
        const apiRouter = new ApiRouter(this.deviceManager, this);
        this.app.use('/api', apiRouter.getRouter());
        
        // 主页路由
        this.app.get('/', (req, res) => {
            res.sendFile(path.join(__dirname, '../public/index.html'));
        });
        
        // 设备管理页面
        this.app.get('/devices', (req, res) => {
            res.sendFile(path.join(__dirname, '../public/devices.html'));
        });
        
        // 分组管理页面
        this.app.get('/groups', (req, res) => {
            res.sendFile(path.join(__dirname, '../public/groups.html'));
        });
        
        // 监控页面
        this.app.get('/monitor', (req, res) => {
            res.sendFile(path.join(__dirname, '../public/monitor.html'));
        });
        
        // 系统管理页面
        this.app.get('/admin', (req, res) => {
            res.sendFile(path.join(__dirname, '../public/admin.html'));
        });
        
        // 导航栏组件路由
        this.app.get('/components/navbar.html', (req, res) => {
            res.sendFile(path.join(__dirname, '../public/components/navbar.html'));
        });
    }
    
    /**
     * 设置WebSocket处理
     */
    setupWebSocket() {
        // 跟踪连接的客户端
        this.io.on('connection', (socket) => {
            this.connectedClients++;
            console.log(`客户端连接: ${socket.id}, 总连接数: ${this.connectedClients}`);
            
            socket.on('disconnect', () => {
                this.connectedClients--;
                console.log(`客户端断开: ${socket.id}, 总连接数: ${this.connectedClients}`);
            });
        });
        
        const wsHandler = new WebSocketHandler(this.io, this.deviceManager);
        wsHandler.initialize();
    }

    /**
     * 获取连接的客户端数量
     */
    getConnectedClientsCount() {
        return this.connectedClients;
    }

    /**
     * 获取API调用次数
     */
    getApiCallsCount() {
        return this.apiCallsCount;
    }
    
    /**
     * 设置设备发现服务
     */
    setupDiscovery() {
        this.discoveryService.start();
    }
    
    /**
     * 启动服务器
     */
    start() {
        this.server.listen(this.port, () => {
            console.log(`\nSSLAB设备模拟器服务器启动成功`);
            console.log(`Web界面: http://localhost:${this.port}`);
            console.log(`API接口: http://localhost:${this.port}/api`);
            console.log(`WebSocket: ws://localhost:${this.port}`);
            console.log(`mDNS发现服务已启动`);
            console.log(`\n可用页面:`);
            console.log(`   主页:     http://localhost:${this.port}/`);
            console.log(`   设备管理: http://localhost:${this.port}/devices`);
            console.log(`   分组管理: http://localhost:${this.port}/groups`);
            console.log(`   实时监控: http://localhost:${this.port}/monitor`);
            console.log(`   系统管理: http://localhost:${this.port}/admin`);
            console.log(`\n提示: 请通过Web界面手动添加设备`);
        });
    }
    
    /**
     * 优雅关闭
     */
    stop() {
        console.log('\n正在关闭服务器...');
        
        this.discoveryService.stop();
        this.server.close(() => {
            console.log('服务器已关闭');
            process.exit(0);
        });
    }
}

// 处理退出信号
const server = new DeviceSimulatorServer();

process.on('SIGINT', () => {
    console.log('\n收到退出信号 (SIGINT)');
    server.stop();
});

process.on('SIGTERM', () => {
    console.log('\n收到退出信号 (SIGTERM)');
    server.stop();
});

// 启动服务器
server.start();

module.exports = DeviceSimulatorServer;
