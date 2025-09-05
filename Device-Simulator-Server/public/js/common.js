/**
 * 公共JavaScript工具函数
 * 用于所有页面的通用功能
 */

// 加载导航栏组件
function loadNavbar() {
    return fetch('/components/navbar.html')
        .then(response => response.text())
        .then(html => {
            const navbarContainer = document.getElementById('navbar-container');
            if (navbarContainer) {
                navbarContainer.innerHTML = html;
                
                // 执行导航栏中的脚本
                const scripts = navbarContainer.querySelectorAll('script');
                scripts.forEach(script => {
                    const newScript = document.createElement('script');
                    newScript.textContent = script.textContent;
                    document.head.appendChild(newScript);
                });
            }
        })
        .catch(error => {
            console.error('Failed to load navbar:', error);
        });
}

// 格式化时间戳
function formatTimestamp(timestamp) {
    return new Date(timestamp).toLocaleString('zh-CN');
}

// 格式化数字
function formatNumber(number, decimals = 2) {
    if (typeof number !== 'number') return 'N/A';
    return number.toFixed(decimals);
}

// 格式化百分比
function formatPercent(value, total) {
    if (!total || total === 0) return '0%';
    return ((value / total) * 100).toFixed(1) + '%';
}

// 显示成功消息
function showSuccess(message) {
    showToast(message, 'success');
}

// 显示错误消息
function showError(message) {
    showToast(message, 'error');
}

// 显示信息消息
function showInfo(message) {
    showToast(message, 'info');
}

// 通用Toast消息显示
function showToast(message, type = 'info') {
    // 创建toast容器（如果不存在）
    let toastContainer = document.getElementById('toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.className = 'position-fixed top-0 end-0 p-3';
        toastContainer.style.zIndex = '1050';
        document.body.appendChild(toastContainer);
    }
    
    // 创建toast元素
    const toastId = 'toast-' + Date.now();
    const bgClass = type === 'success' ? 'bg-success' : 
                   type === 'error' ? 'bg-danger' : 
                   type === 'warning' ? 'bg-warning' : 'bg-info';
    
    const toastHtml = `
        <div class="toast ${bgClass} text-white" id="${toastId}" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="toast-header ${bgClass} text-white">
                <i class="bi bi-${type === 'success' ? 'check-circle' : 
                                type === 'error' ? 'exclamation-circle' : 
                                type === 'warning' ? 'exclamation-triangle' : 'info-circle'} me-2"></i>
                <strong class="me-auto">${type === 'success' ? '成功' : 
                                        type === 'error' ? '错误' : 
                                        type === 'warning' ? '警告' : '信息'}</strong>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
            <div class="toast-body">
                ${message}
            </div>
        </div>
    `;
    
    toastContainer.insertAdjacentHTML('beforeend', toastHtml);
    
    // 显示toast
    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: type === 'error' ? 5000 : 3000
    });
    toast.show();
    
    // 自动移除toast元素
    toastElement.addEventListener('hidden.bs.toast', function() {
        toastElement.remove();
    });
}

// 发送API请求的通用函数
async function apiRequest(url, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
    };
    
    const mergedOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers,
        },
    };
    
    try {
        const response = await fetch(url, mergedOptions);
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.error?.message || `HTTP ${response.status}: ${response.statusText}`);
        }
        
        return data;
    } catch (error) {
        console.error('API request failed:', error);
        throw error;
    }
}

// 获取设备状态的显示样式
function getDeviceStatusClass(status) {
    switch (status) {
        case 'online':
            return 'text-success';
        case 'offline':
            return 'text-danger';
        case 'connecting':
            return 'text-warning';
        default:
            return 'text-secondary';
    }
}

// 获取设备状态的图标
function getDeviceStatusIcon(status) {
    switch (status) {
        case 'online':
            return 'bi-circle-fill';
        case 'offline':
            return 'bi-circle';
        case 'connecting':
            return 'bi-arrow-clockwise';
        default:
            return 'bi-question-circle';
    }
}

// 获取设备状态的中文描述
function getDeviceStatusText(status) {
    switch (status) {
        case 'online':
            return '在线';
        case 'offline':
            return '离线';
        case 'connecting':
            return '连接中';
        default:
            return '未知';
    }
}

// 防抖函数
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// 节流函数
function throttle(func, limit) {
    let inThrottle;
    return function() {
        const args = arguments;
        const context = this;
        if (!inThrottle) {
            func.apply(context, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    // 加载导航栏
    loadNavbar();
    
    // 初始化Bootstrap tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // 初始化Bootstrap popovers
    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
});

// 导出给全局使用
window.CommonUtils = {
    loadNavbar,
    formatTimestamp,
    formatNumber,
    formatPercent,
    showSuccess,
    showError,
    showInfo,
    showToast,
    apiRequest,
    getDeviceStatusClass,
    getDeviceStatusIcon,
    getDeviceStatusText,
    debounce,
    throttle
};
