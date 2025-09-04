/**
 * 统一导航栏加载器
 * 确保所有页面使用相同的导航栏组件
 */

async function loadNavbar() {
    try {
        const response = await fetch('/components/navbar.html');
        const navbarHTML = await response.text();
        
        // 查找导航栏容器
        const navbarContainer = document.getElementById('navbar-container');
        if (navbarContainer) {
            navbarContainer.innerHTML = navbarHTML;
        } else {
            // 如果没有容器，在body开头插入
            document.body.insertAdjacentHTML('afterbegin', navbarHTML);
        }
        
        // 初始化导航栏功能
        initNavbar();
    } catch (error) {
        console.error('加载导航栏失败:', error);
    }
}

function initNavbar() {
    // 设置当前页面的active状态
    const currentPage = getCurrentPageName();
    const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
    
    navLinks.forEach(link => {
        const page = link.getAttribute('data-page');
        if (page === currentPage) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}

function getCurrentPageName() {
    const path = window.location.pathname;
    if (path === '/' || path === '/index.html') return 'index';
    if (path.includes('devices')) return 'devices';
    if (path.includes('groups')) return 'groups';
    if (path.includes('monitor')) return 'monitor';
    if (path.includes('admin')) return 'admin';
    return 'index';
}

// 页面加载完成后自动加载导航栏
document.addEventListener('DOMContentLoaded', loadNavbar);
