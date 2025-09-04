#!/usr/bin/env python3
"""
SSLAB 设备模拟器服务器
用于模拟实验室设备的HTTP API服务器
支持设备状态查询、控制操作和环境监测
"""

from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import urllib.parse
import time
import random
from datetime import datetime

class DeviceSimulatorHandler(BaseHTTPRequestHandler):
    
    # 模拟设备状态数据
    device_status = {
        "power_main": True,
        "power_module1": True,
        "power_module2": False,
        "power_module3": True,
        "temperature": 22.5,
        "humidity": 45.0,
        "air_quality": "良好",
        "ventilation": "正常",
        "safety_level": "A级",
        "devices_online": 12,
        "devices_total": 12
    }
    
    def _set_headers(self):
        """设置响应头，支持CORS"""
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        self.end_headers()
    
    def _send_json_response(self, data):
        """发送JSON响应"""
        self._set_headers()
        self.wfile.write(json.dumps(data, ensure_ascii=False).encode('utf-8'))
    
    def do_OPTIONS(self):
        """处理CORS预检请求"""
        self._set_headers()
    
    def do_GET(self):
        """处理GET请求"""
        try:
            # 解析URL路径
            path = urllib.parse.urlparse(self.path).path
            query = urllib.parse.parse_qs(urllib.parse.urlparse(self.path).query)
            
            print(f"GET请求: {path}")
            
            if path == '/api/status':
                # 返回设备状态
                # 模拟温度变化
                self.device_status["temperature"] = round(22.0 + random.uniform(-0.5, 1.0), 1)
                self.device_status["humidity"] = round(45.0 + random.uniform(-2.0, 2.0), 1)
                
                response = {
                    "status": "success",
                    "timestamp": datetime.now().isoformat(),
                    "data": self.device_status
                }
                self._send_json_response(response)
                
            elif path == '/api/devices':
                # 返回设备列表
                devices = []
                for i in range(1, 13):
                    devices.append({
                        "id": f"device_{i:02d}",
                        "name": f"实验设备 {i}",
                        "type": "teaching_equipment",
                        "status": "online" if random.random() > 0.1 else "offline",
                        "power": random.choice([True, False]),
                        "temperature": round(22.0 + random.uniform(-2.0, 3.0), 1)
                    })
                
                response = {
                    "status": "success",
                    "data": {
                        "devices": devices,
                        "total": len(devices),
                        "online": len([d for d in devices if d["status"] == "online"])
                    }
                }
                self._send_json_response(response)
                
            elif path == '/api/environment':
                # 返回环境监测数据
                response = {
                    "status": "success",
                    "data": {
                        "temperature": self.device_status["temperature"],
                        "humidity": self.device_status["humidity"],
                        "air_pressure": round(1013.2 + random.uniform(-5.0, 5.0), 1),
                        "co2_level": round(420 + random.uniform(-20, 30)),
                        "air_quality": self.device_status["air_quality"],
                        "ventilation": self.device_status["ventilation"],
                        "timestamp": datetime.now().isoformat()
                    }
                }
                self._send_json_response(response)
                
            elif path == '/api/safety':
                # 返回安全检查数据
                response = {
                    "status": "success",
                    "data": {
                        "safety_level": self.device_status["safety_level"],
                        "emergency_stop": "可用",
                        "fire_system": "正常",
                        "ventilation": "正常",
                        "door_access": "正常",
                        "surveillance": "在线",
                        "gas_detection": "未检出",
                        "radiation_level": "正常",
                        "timestamp": datetime.now().isoformat()
                    }
                }
                self._send_json_response(response)
                
            else:
                # 404错误
                self.send_response(404)
                self.end_headers()
                self.wfile.write(b'Not Found')
                
        except Exception as e:
            print(f"GET请求错误: {e}")
            self.send_response(500)
            self.end_headers()
            self.wfile.write(json.dumps({"error": str(e)}).encode('utf-8'))
    
    def do_POST(self):
        """处理POST请求（设备控制）"""
        try:
            content_length = int(self.headers['Content-Length'])
            post_data = self.rfile.read(content_length)
            data = json.loads(post_data.decode('utf-8'))
            
            path = urllib.parse.urlparse(self.path).path
            print(f"POST请求: {path}, 数据: {data}")
            
            if path == '/api/control':
                action = data.get('action', '')
                device = data.get('device', '')
                value = data.get('value', None)
                
                # 模拟设备控制操作
                if action == 'power_on':
                    if device == 'all':
                        self.device_status.update({
                            "power_main": True,
                            "power_module1": True,
                            "power_module2": True,
                            "power_module3": True
                        })
                        message = "所有电源已开启"
                    else:
                        self.device_status[f"power_{device}"] = True
                        message = f"{device}电源已开启"
                        
                elif action == 'power_off':
                    if device == 'all':
                        self.device_status.update({
                            "power_main": False,
                            "power_module1": False,
                            "power_module2": False,
                            "power_module3": False
                        })
                        message = "所有电源已关闭"
                    else:
                        self.device_status[f"power_{device}"] = False
                        message = f"{device}电源已关闭"
                        
                elif action == 'reset':
                    message = "设备重置完成"
                    
                elif action == 'calibrate':
                    message = "设备校准完成"
                    
                else:
                    message = f"执行操作: {action}"
                
                # 模拟操作延迟
                time.sleep(0.5)
                
                response = {
                    "status": "success",
                    "message": message,
                    "timestamp": datetime.now().isoformat(),
                    "device_status": self.device_status
                }
                self._send_json_response(response)
                
            else:
                self.send_response(404)
                self.end_headers()
                self.wfile.write(b'Not Found')
                
        except Exception as e:
            print(f"POST请求错误: {e}")
            self.send_response(500)
            self.end_headers()
            self.wfile.write(json.dumps({"error": str(e)}).encode('utf-8'))

def run_server(port=8080):
    """启动模拟器服务器"""
    server_address = ('', port)
    httpd = HTTPServer(server_address, DeviceSimulatorHandler)
    
    print(f"""
╔════════════════════════════════════════════╗
║        SSLAB 设备模拟器服务器已启动         ║
║                                            ║
║  🌐 服务地址: http://0.0.0.0:{port}        ║
║  📱 Android设备访问: http://192.168.0.145:{port}  ║
║                                            ║
║  📡 API端点:                               ║
║  GET  /api/status      - 设备状态          ║
║  GET  /api/devices     - 设备列表          ║
║  GET  /api/environment - 环境监测          ║
║  GET  /api/safety      - 安全检查          ║
║  POST /api/control     - 设备控制          ║
║                                            ║
║  🛑 按 Ctrl+C 停止服务器                   ║
╚════════════════════════════════════════════╝
    """)
    
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\n🛑 服务器已停止")
        httpd.shutdown()

if __name__ == '__main__':
    run_server()
