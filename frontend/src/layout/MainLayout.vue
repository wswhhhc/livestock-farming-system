<template>
  <div class="layout-root">
    <!-- 有机流动背景 -->
    <div class="organic-bg">
      <canvas ref="canvasRef" class="organic-canvas"></canvas>
      <div class="bg-gradient-overlay"></div>
    </div>

    <el-container class="layout-container">
      <!-- 侧边栏 - 深色有机主题 -->
      <el-aside :width="isCollapse ? '64px' : '260px'" class="sidebar">
        <div class="sidebar-inner">
          <!-- Logo区域 -->
          <div class="sidebar-header">
            <div class="logo-container" :class="{ 'collapsed': isCollapse }">
              <svg class="logo-svg" viewBox="0 0 80 80" fill="none">
                <!-- 外圆环 -->
                <circle cx="40" cy="40" r="36" stroke="url(#logoStroke)" stroke-width="1.5" fill="none" opacity="0.5">
                  <animate attributeName="r" values="36;38;36" dur="4s" repeatCount="indefinite"/>
                </circle>
                <!-- 内部叶子形状 -->
                <path d="M40 15 C32 15 25 22 25 32 C25 40 30 44 34 47 C30 50 25 54 25 62 C25 72 32 78 40 78 C48 78 55 72 55 62 C55 54 50 50 46 47 C50 44 55 40 55 32 C55 22 48 15 40 15Z"
                      fill="url(#logoFill)" opacity="0.9"/>
                <!-- 叶脉 -->
                <path d="M40 42 L40 70" stroke="rgba(245,241,235,0.3)" stroke-width="1.5" stroke-linecap="round"/>
                <path d="M34 52 C36 55 38 56 40 56 C42 56 44 55 46 52" stroke="rgba(245,241,235,0.2)" stroke-width="1.5" stroke-linecap="round"/>
                <defs>
                  <linearGradient id="logoStroke" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0%" stop-color="#9CAF88"/>
                    <stop offset="100%" stop-color="#5D8C5D"/>
                  </linearGradient>
                  <linearGradient id="logoFill" x1="25" y1="15" x2="55" y2="78">
                    <stop offset="0%" stop-color="#9CAF88"/>
                    <stop offset="50%" stop-color="#7DB97D"/>
                    <stop offset="100%" stop-color="#5D8C5D"/>
                  </linearGradient>
                </defs>
              </svg>
              <transition name="fade">
                <div class="logo-text" v-show="!isCollapse">
                  <span class="logo-title">养殖管理系统</span>
                  <span class="logo-subtitle">Livestock Farm</span>
                </div>
              </transition>
            </div>
          </div>

          <!-- 导航菜单 -->
          <el-menu
            :default-active="route.path"
            :collapse="isCollapse"
            :collapse-transition="false"
            background-color="transparent"
            text-color="rgba(245,241,235,0.65)"
            active-text-color="#9CAF88"
            router
            class="sidebar-menu"
          >
            <!-- 概览菜单组 -->
            <div class="menu-group" v-show="!isCollapse">
              <span class="group-label">概览</span>
              <div class="group-line"></div>
            </div>
            <el-tooltip content="首页仪表盘" placement="right" :disabled="!isCollapse" :show-after="400">
              <el-menu-item index="/dashboard" class="menu-item-animated">
                <div class="menu-icon-wrap">
                  <el-icon><Monitor /></el-icon>
                </div>
                <template #title>
                  <span class="menu-title">首页仪表盘</span>
                  <span class="menu-badge" v-if="pendingCount > 0">{{ pendingCount }}</span>
                </template>
              </el-menu-item>
            </el-tooltip>

            <!-- 养殖管理菜单组 -->
            <div class="menu-group" v-show="!isCollapse">
              <span class="group-label">养殖管理</span>
              <div class="group-line"></div>
            </div>
            <el-tooltip content="家畜种类" placement="right" :disabled="!isCollapse" :show-after="400">
              <el-menu-item index="/category" class="menu-item-animated">
                <div class="menu-icon-wrap">
                  <el-icon><Collection /></el-icon>
                </div>
                <span class="menu-title">家畜种类</span>
              </el-menu-item>
            </el-tooltip>
            <el-tooltip content="养殖场地" placement="right" :disabled="!isCollapse" :show-after="400">
              <el-menu-item index="/site" class="menu-item-animated">
                <div class="menu-icon-wrap">
                  <el-icon><OfficeBuilding /></el-icon>
                </div>
                <span class="menu-title">养殖场地</span>
              </el-menu-item>
            </el-tooltip>
            <el-tooltip content="存栏列表" placement="right" :disabled="!isCollapse" :show-after="400">
              <el-menu-item index="/batch" class="menu-item-animated">
                <div class="menu-icon-wrap">
                  <el-icon><Histogram /></el-icon>
                </div>
                <span class="menu-title">存栏列表</span>
              </el-menu-item>
            </el-tooltip>

            <!-- 财务管理菜单组 -->
            <div class="menu-group" v-show="!isCollapse">
              <span class="group-label">财务管理</span>
              <div class="group-line"></div>
            </div>
            <el-tooltip content="成本管理" placement="right" :disabled="!isCollapse" :show-after="400">
              <el-menu-item index="/cost" class="menu-item-animated">
                <div class="menu-icon-wrap">
                  <el-icon><Wallet /></el-icon>
                </div>
                <span class="menu-title">成本管理</span>
              </el-menu-item>
            </el-tooltip>
            <el-tooltip content="价格规则" placement="right" :disabled="!isCollapse" :show-after="400">
              <el-menu-item index="/rule" class="menu-item-animated">
                <div class="menu-icon-wrap">
                  <el-icon><SetUp /></el-icon>
                </div>
                <span class="menu-title">价格规则</span>
              </el-menu-item>
            </el-tooltip>
            <el-tooltip content="收益预估" placement="right" :disabled="!isCollapse" :show-after="400">
              <el-menu-item index="/revenue" class="menu-item-animated">
                <div class="menu-icon-wrap">
                  <el-icon><TrendCharts /></el-icon>
                </div>
                <span class="menu-title">收益预估</span>
              </el-menu-item>
            </el-tooltip>

            <!-- 智能应用菜单组 -->
            <div class="menu-group" v-show="!isCollapse">
              <span class="group-label">智能应用</span>
              <div class="group-line"></div>
            </div>
            <el-tooltip content="养殖建议" placement="right" :disabled="!isCollapse" :show-after="400">
              <el-menu-item index="/advice" class="menu-item-animated">
                <div class="menu-icon-wrap">
                  <el-icon><ChatDotSquare /></el-icon>
                </div>
                <template #title>
                  <span class="menu-title">养殖建议</span>
                  <span class="menu-badge new" v-if="hasNewAdvice">NEW</span>
                </template>
              </el-menu-item>
            </el-tooltip>
          </el-menu>

          <!-- 用户卡片 -->
          <div class="sidebar-footer">
            <div class="user-card" :class="{ 'collapsed': isCollapse }" v-show="!isCollapse">
              <div class="user-avatar">
                <span class="avatar-text">{{ getUserInitial() }}</span>
                <div class="avatar-status"></div>
              </div>
              <div class="user-info">
                <div class="user-name">{{ user.realName || user.username }}</div>
                <div class="user-role">系统管理员</div>
              </div>
            </div>
            <div class="user-avatar-collapsed" v-show="isCollapse">
              <span>{{ getUserInitial() }}</span>
              <div class="avatar-status-collapsed"></div>
            </div>
          </div>
        </div>
      </el-aside>

      <!-- 主内容区 -->
      <el-container class="main-container">
        <!-- 顶部导航栏 -->
        <el-header class="top-header">
          <div class="header-left">
            <el-tooltip :content="isCollapse ? '展开菜单' : '收起菜单'" placement="bottom" :show-after="300">
              <button class="collapse-btn" @click="toggleCollapse">
                <el-icon :size="18">
                  <Fold v-if="!isCollapse" />
                  <Expand v-else />
                </el-icon>
              </button>
            </el-tooltip>
            <div class="breadcrumb-section">
              <span class="page-title">{{ route.meta.title || '仪表盘' }}</span>
              <span class="page-path">{{ getPagePath() }}</span>
            </div>
          </div>

          <div class="header-right">
            <!-- 通知按钮 -->
            <el-tooltip content="通知消息" placement="bottom" :show-after="300">
              <button class="header-btn">
                <el-icon :size="18"><Bell /></el-icon>
                <span class="notification-dot" v-if="pendingCount > 0"></span>
              </button>
            </el-tooltip>

            <!-- 用户下拉 -->
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-dropdown-trigger">
                <div class="mini-avatar">{{ getUserInitial() }}</div>
                <span class="user-name-text" v-if="!isMobile">{{ user.realName || user.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown-menu">
                  <div class="dropdown-header">
                    <div class="dropdown-avatar">{{ getUserInitial() }}</div>
                    <div class="dropdown-info">
                      <div class="dropdown-name">{{ user.realName || user.username }}</div>
                      <div class="dropdown-role">系统管理员</div>
                    </div>
                  </div>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    <span>个人资料</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <el-icon><Setting /></el-icon>
                    <span>系统设置</span>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon style="color: var(--danger);"><SwitchButton /></el-icon>
                    <span style="color: var(--danger);">退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 页面内容 -->
        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="page" mode="out-in">
              <component :is="Component" :key="route.path" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Monitor, Collection, OfficeBuilding, Histogram,
  Wallet, SetUp, TrendCharts, ChatDotSquare,
  Fold, Expand, Bell, ArrowDown, User, Setting,
  SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const isMobile = ref(window.innerWidth < 768)

const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const pendingCount = ref(0)
const hasNewAdvice = ref(false)

// Canvas背景
const canvasRef = ref(null)
let animationId = null
let ctx = null

// 有机流动动画
function initOrganicBg() {
  const canvas = canvasRef.value
  if (!canvas) return

  ctx = canvas.getContext('2d')
  resizeCanvas()

  const waves = []
  const waveCount = 3

  for (let i = 0; i < waveCount; i++) {
    waves.push({
      y: canvas.height * (0.3 + i * 0.2),
      amplitude: 30 + i * 10,
      frequency: 0.002 + i * 0.001,
      speed: 0.02 + i * 0.01,
      offset: i * Math.PI / 2,
      color: i === 0 ? 'rgba(157, 175, 136, 0.03)' :
             i === 1 ? 'rgba(212, 165, 116, 0.03)' :
                       'rgba(122, 154, 168, 0.02)'
    })
  }

  function animate() {
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    waves.forEach((wave, i) => {
      wave.offset += wave.speed

      ctx.beginPath()
      ctx.moveTo(0, canvas.height)

      for (let x = 0; x <= canvas.width; x += 5) {
        const y = wave.y + Math.sin(x * wave.frequency + wave.offset) * wave.amplitude
        if (x === 0) {
          ctx.moveTo(x, y)
        } else {
          ctx.lineTo(x, y)
        }
      }

      ctx.lineTo(canvas.width, canvas.height)
      ctx.lineTo(0, canvas.height)
      ctx.closePath()

      ctx.fillStyle = wave.color
      ctx.fill()
    })

    animationId = requestAnimationFrame(animate)
  }

  animate()

  window.addEventListener('resize', resizeCanvas)
}

function resizeCanvas() {
  const canvas = canvasRef.value
  if (!canvas) return
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
}

function getUserInitial() {
  const name = user.value.realName || user.value.username || 'U'
  return name.charAt(0).toUpperCase()
}

function getPagePath() {
  const paths = {
    '/dashboard': '概览 / 仪表盘',
    '/category': '养殖管理 / 家畜种类',
    '/site': '养殖管理 / 养殖场地',
    '/batch': '养殖管理 / 存栏列表',
    '/cost': '财务管理 / 成本管理',
    '/rule': '财务管理 / 价格规则',
    '/revenue': '财务管理 / 收益预估',
    '/advice': '智能应用 / 养殖建议'
  }
  return paths[route.path] || ''
}

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function handleCommand(command) {
  switch (command) {
    case 'logout':
      handleLogout()
      break
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
  }
}

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

onMounted(() => {
  initOrganicBg()
  window.addEventListener('resize', () => {
    isMobile.value = window.innerWidth < 768
  })
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
})
</script>

<style scoped>
.layout-root {
  position: relative;
  min-height: 100vh;
}

/* ===== 有机流动背景 ===== */
.organic-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
}

.organic-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.bg-gradient-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg,
    rgba(247, 243, 237, 0) 0%,
    rgba(247, 243, 237, 0.5) 50%,
    rgba(247, 243, 237, 0.9) 100%
  );
  pointer-events: none;
}

/* ===== 布局容器 ===== */
.layout-container {
  position: relative;
  z-index: 1;
  height: 100vh;
}

/* ===== 侧边栏 ===== */
.sidebar {
  background: linear-gradient(180deg, #1A261A 0%, #152015 50%, #111811 100%);
  border-right: 1px solid rgba(157, 175, 136, 0.08);
  transition: width 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  overflow: hidden;
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at top, rgba(157, 175, 136, 0.05) 0%, transparent 50%),
    radial-gradient(ellipse at bottom, rgba(93, 140, 93, 0.03) 0%, transparent 50%);
  pointer-events: none;
}

.sidebar-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
  z-index: 1;
}

/* Logo区域 */
.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid rgba(157, 175, 136, 0.1);
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 14px;
  transition: all 0.4s ease;
}

.logo-container.collapsed {
  justify-content: center;
}

.logo-svg {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  filter: drop-shadow(0 4px 12px rgba(157, 175, 136, 0.3));
}

.logo-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
}

.logo-title {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 1.1rem;
  font-weight: 600;
  color: rgba(245, 241, 235, 0.95);
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.logo-subtitle {
  font-size: 0.7rem;
  color: rgba(245, 241, 235, 0.4);
  letter-spacing: 2px;
  text-transform: uppercase;
  white-space: nowrap;
}

/* 菜单组 - 优化对齐 */
.menu-group {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 16px 10px;
  margin: 0 2px;
}

.group-label {
  font-size: 0.7rem;
  font-weight: 700;
  color: rgba(245, 241, 235, 0.35);
  text-transform: uppercase;
  letter-spacing: 2px;
  white-space: nowrap;
}

.group-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, rgba(157, 175, 136, 0.2), transparent 80%);
}

/* 菜单 */
.sidebar-menu {
  border-right: none !important;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 14px;
}

/* 菜单项基础样式 - 优化对齐 */
.sidebar-menu :deep(.el-menu-item) {
  height: 52px;
  line-height: 52px;
  margin: 6px 0;
  border-radius: 12px;
  padding: 0 16px !important;
  position: relative;
  overflow: visible;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  display: flex;
  align-items: center;
}

/* 左侧指示条 */
.sidebar-menu :deep(.el-menu-item::before) {
  content: '';
  position: absolute;
  left: -14px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 0;
  background: linear-gradient(180deg, #9CAF88, #5D8C5D);
  border-radius: 0 4px 4px 0;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  opacity: 0;
}

/* 选中状态 - 左移效果 */
.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(157, 175, 136, 0.15) !important;
  transform: translateX(8px);
  box-shadow: 0 4px 20px rgba(157, 175, 136, 0.2);
}

.sidebar-menu :deep(.el-menu-item.is-active::before) {
  height: 28px;
  opacity: 1;
  left: -14px;
}

/* 悬停效果 */
.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(157, 175, 136, 0.1) !important;
  color: rgba(245, 241, 235, 0.95) !important;
  transform: translateX(4px);
}

.sidebar-menu :deep(.el-menu-item.is-active:hover) {
  transform: translateX(8px);
}

/* 图标容器 - 优化对齐 */
.menu-icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(245, 241, 235, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  flex-shrink: 0;
}

/* 悬停时图标放大 */
.sidebar-menu :deep(.el-menu-item:hover) .menu-icon-wrap {
  background: rgba(157, 175, 136, 0.2);
  color: #9CAF88;
  transform: scale(1.15);
  box-shadow: 0 4px 12px rgba(157, 175, 136, 0.3);
}

/* 选中时图标效果 */
.sidebar-menu :deep(.el-menu-item.is-active) .menu-icon-wrap {
  background: linear-gradient(135deg, rgba(157, 175, 136, 0.25), rgba(93, 140, 93, 0.15)) !important;
  color: #9CAF88;
  transform: scale(1.1);
  box-shadow: 0 4px 16px rgba(157, 175, 136, 0.4);
}

/* 菜单标题 */
.menu-title {
  font-weight: 500;
  font-size: 0.92rem;
  flex: 1;
  transition: all 0.3s ease;
}

/* 选中时文字加粗 */
.sidebar-menu :deep(.el-menu-item.is-active) .menu-title {
  font-weight: 600;
  color: #9CAF88;
  transform: translateX(2px);
}

/* 角标样式 */
.menu-badge {
  margin-left: auto;
  padding: 3px 10px;
  border-radius: 100px;
  font-size: 0.7rem;
  font-weight: 600;
  background: rgba(201, 123, 123, 0.9);
  color: white;
  transition: all 0.3s ease;
  animation: badgePulse 2s ease-in-out infinite;
}

@keyframes badgePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.menu-badge.new {
  background: linear-gradient(135deg, #9CAF88, #5D8C5D);
}

/* 选中时角标也跟随移动 */
.sidebar-menu :deep(.el-menu-item.is-active) .menu-badge {
  transform: translateX(2px);
}

/* 折叠状态菜单 */
.sidebar-menu:deep(.el-menu--collapse) {
  padding: 10px 0;
  width: 100%;
}

/* 菜单项 - 水平垂直居中，整体往左移 */
.sidebar-menu:deep(.el-menu--collapse) .el-menu-item {
  margin: 5px 0;
  width: 40px;
  height: 40px;
  padding: 0 !important;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  transform-origin: center center;
  /* 整体往左移，避免超出右边 */
  margin-left: 8px;
  margin-right: auto;
}

/* 折叠状态下的悬停 */
.sidebar-menu:deep(.el-menu--collapse) .el-menu-item:hover {
  background: rgba(157, 175, 136, 0.15) !important;
}

/* 折叠状态下的选中效果 - 使用边框发光而非缩放 */
.sidebar-menu:deep(.el-menu--collapse) .el-menu-item.is-active {
  background: rgba(157, 175, 136, 0.22) !important;
  box-shadow:
    inset 0 0 0 1px rgba(157, 175, 136, 0.4),
    0 0 20px rgba(157, 175, 136, 0.3);
}

/* 图标容器 */
.sidebar-menu:deep(.el-menu--collapse) .menu-icon-wrap {
  margin: 0;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

/* 折叠状态下的图标效果 */
.sidebar-menu:deep(.el-menu--collapse) .el-menu-item:hover .menu-icon-wrap {
  background: rgba(157, 175, 136, 0.3);
  transform: scale(1.08);
}

.sidebar-menu:deep(.el-menu--collapse) .el-menu-item.is-active .menu-icon-wrap {
  background: rgba(157, 175, 136, 0.35);
  color: #B8D4A8;
}

/* 侧边栏底部用户卡片 */
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(157, 175, 136, 0.1);
}

.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(245, 241, 235, 0.03);
  border: 1px solid rgba(157, 175, 136, 0.08);
  transition: all 0.3s ease;
}

.user-card:hover {
  background: rgba(245, 241, 235, 0.06);
  border-color: rgba(157, 175, 136, 0.12);
}

.user-card.collapsed {
  justify-content: center;
}

.user-avatar {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #9CAF88 0%, #5D8C5D 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-text {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 1.1rem;
  font-weight: 600;
  color: white;
}

.avatar-status {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #5D8C5D;
  border: 2px solid #152015;
}

.user-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.user-name {
  font-weight: 600;
  color: rgba(245, 241, 235, 0.9);
  font-size: 0.9rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 0.75rem;
  color: rgba(245, 241, 235, 0.4);
  margin-top: 2px;
}

.user-avatar-collapsed {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #9CAF88 0%, #5D8C5D 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  color: white;
  font-weight: 600;
  font-size: 1rem;
}

.avatar-status-collapsed {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #5D8C5D;
  border: 2px solid #152015;
}

/* ===== 主内容区 ===== */
.main-container {
  background: transparent;
}

/* 顶部导航栏 */
.top-header {
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(247, 243, 237, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(139, 139, 122, 0.1);
  position: relative;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: none;
  background: rgba(139, 139, 122, 0.08);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.collapse-btn:hover {
  background: rgba(93, 140, 93, 0.12);
  color: var(--primary);
  transform: scale(1.05);
}

.breadcrumb-section {
  display: flex;
  flex-direction: column;
}

.page-title {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
}

.page-path {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.3s ease;
}

.header-btn:hover {
  background: rgba(93, 140, 93, 0.08);
  color: var(--primary);
}

.notification-dot {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--danger);
  border: 2px solid rgba(247, 243, 237, 0.7);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.7; }
}

.user-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 6px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-dropdown-trigger:hover {
  background: rgba(93, 140, 93, 0.08);
}

.mini-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #9CAF88 0%, #5D8C5D 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.9rem;
  font-weight: 600;
}

.user-name-text {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-primary);
}

/* 下拉菜单样式 */
:deep(.user-dropdown-menu) {
  padding: 8px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(139, 139, 122, 0.15);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.1);
  min-width: 200px;
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-bottom: 1px solid rgba(139, 139, 122, 0.1);
  margin-bottom: 8px;
}

.dropdown-avatar {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #9CAF88 0%, #5D8C5D 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
}

.dropdown-info {
  flex: 1;
}

.dropdown-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.95rem;
}

.dropdown-role {
  font-size: 0.8rem;
  color: var(--text-tertiary);
  margin-top: 2px;
}

:deep(.el-dropdown-menu__item) {
  padding: 10px 12px;
  border-radius: 10px;
  margin: 2px 0;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.9rem;
  color: var(--text-secondary);
  transition: all 0.2s ease;
}

:deep(.el-dropdown-menu__item:hover) {
  background: rgba(93, 140, 93, 0.08);
  color: var(--primary);
}

/* 主内容区域 */
.main-content {
  padding: 24px;
  overflow-y: auto;
  position: relative;
}

/* 页面过渡动画 */
.page-enter-active {
  animation: pageEnter 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.page-leave-active {
  animation: pageLeave 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes pageEnter {
  0% { opacity: 0; transform: translateY(20px) scale(0.98); }
  100% { opacity: 1; transform: translateY(0) scale(1); }
}

@keyframes pageLeave {
  0% { opacity: 1; transform: translateY(0) scale(1); }
  100% { opacity: 0; transform: translateY(-10px) scale(0.98); }
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
