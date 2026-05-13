<template>
  <div class="login-page">
    <!-- 动态有机背景 -->
    <div class="organic-background">
      <canvas ref="bgCanvas" class="bg-canvas"></canvas>
      <div class="gradient-overlay"></div>
      <!-- 漂浮的装饰元素 -->
      <div class="floating-elements">
        <div class="float-orb orb-1"></div>
        <div class="float-orb orb-2"></div>
        <div class="float-orb orb-3"></div>
        <div class="float-leaf leaf-1"></div>
        <div class="float-leaf leaf-2"></div>
      </div>
    </div>

    <div class="login-container">
      <!-- 左侧品牌展示区 -->
      <div class="brand-section">
        <div class="brand-content">
          <!-- 品牌标志动画 -->
          <div class="brand-logo">
            <svg viewBox="0 0 120 120" class="logo-animated">
              <!-- 外圈动画 -->
              <circle cx="60" cy="60" r="55" stroke="url(#brandStroke)" stroke-width="1" fill="none" opacity="0.3">
                <animateTransform attributeName="transform" type="rotate" from="0 60 60" to="360 60 60" dur="60s" repeatCount="indefinite"/>
              </circle>
              <circle cx="60" cy="60" r="48" stroke="url(#brandStroke)" stroke-width="0.5" fill="none" opacity="0.2">
                <animateTransform attributeName="transform" type="rotate" from="360 60 60" to="0 60 60" dur="40s" repeatCount="indefinite"/>
              </circle>
              <!-- 主图标 - 叶子形状 -->
              <g transform="translate(60, 60)">
                <path d="M0,-35 C-15,-35 -30,-20 -30,0 C-30,18 -20,28 -10,35 C-20,42 -30,52 -30,70 C-30,90 -15,100 0,100 C15,100 30,90 30,70 C30,52 20,42 10,35 C20,28 30,18 30,0 C30,-20 15,-35 0,-35Z"
                      fill="url(#brandFill)" opacity="0.95">
                  <animateTransform attributeName="transform" type="scale" values="1;1.02;1" dur="4s" repeatCount="indefinite"/>
                </path>
                <!-- 叶脉 -->
                <path d="M0,-5 L0,85" stroke="rgba(255,255,255,0.3)" stroke-width="1.5" stroke-linecap="round"/>
                <path d="M-12,30 C-8,36 -4,38 0,38 C4,38 8,36 12,30" stroke="rgba(255,255,255,0.2)" stroke-width="1.5" stroke-linecap="round"/>
              </g>
              <defs>
                <linearGradient id="brandStroke" x1="0" y1="0" x2="1" y2="1">
                  <stop offset="0%" stop-color="#9CAF88"/>
                  <stop offset="100%" stop-color="#5D8C5D"/>
                </linearGradient>
                <linearGradient id="brandFill" x1="0" y1="-35" x2="0" y2="100">
                  <stop offset="0%" stop-color="#B8D4A8"/>
                  <stop offset="30%" stop-color="#9CAF88"/>
                  <stop offset="100%" stop-color="#5D8C5D"/>
                </linearGradient>
              </defs>
            </svg>
          </div>

          <!-- 品牌文字 -->
          <div class="brand-text">
            <h1 class="brand-title">
              <span class="title-main">养殖管理系统</span>
              <span class="title-line"></span>
            </h1>
            <p class="brand-subtitle">Livestock Farm Management</p>
            <p class="brand-desc">智能化养殖 · 数据驱动决策 · 可持续发展</p>
          </div>

          <!-- 特性展示 -->
          <div class="features-list">
            <div class="feature-item" v-for="(feature, i) in features" :key="i" :style="{ animationDelay: `${0.8 + i * 0.15}s` }">
              <div class="feature-icon">
                <el-icon :size="20">
                  <component :is="feature.icon" />
                </el-icon>
              </div>
              <span class="feature-text">{{ feature.text }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单区 -->
      <div class="form-section">
        <div class="form-wrapper">
          <!-- 装饰背景 -->
          <div class="form-bg-decoration">
            <div class="deco-circle c1"></div>
            <div class="deco-circle c2"></div>
            <div class="deco-circle c3"></div>
          </div>

          <!-- 登录卡片 -->
          <div class="login-card">
            <div class="card-inner">
              <!-- 头部 -->
              <div class="card-header">
                <h2 class="card-title">欢迎回来</h2>
                <p class="card-subtitle">请登录您的账号继续</p>
              </div>

              <!-- 表单 -->
              <el-form
                ref="formRef"
                :model="form"
                :rules="rules"
                class="login-form"
                @keyup.enter="handleLogin"
              >
                <el-form-item prop="username" class="form-item-animated">
                  <div class="input-label">
                    <el-icon><User /></el-icon>
                    <span>用户名</span>
                  </div>
                  <el-input
                    v-model="form.username"
                    placeholder="请输入用户名"
                    size="large"
                    class="custom-input"
                  >
                    <template #prefix>
                      <el-icon><UserFilled /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item prop="password" class="form-item-animated">
                  <div class="input-label">
                    <el-icon><Lock /></el-icon>
                    <span>密码</span>
                  </div>
                  <el-input
                    v-model="form.password"
                    type="password"
                    show-password
                    placeholder="请输入密码"
                    size="large"
                    class="custom-input"
                  >
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>

                <div class="form-options">
                  <el-checkbox v-model="rememberMe" class="remember-check">
                    <span class="check-label">记住密码</span>
                  </el-checkbox>
                  <el-button link type="primary" class="forgot-link">忘记密码？</el-button>
                </div>

                <el-form-item class="submit-item">
                  <button
                    type="button"
                    class="login-btn"
                    :class="{ 'loading': loading }"
                    :disabled="loading"
                    @click="handleLogin"
                  >
                    <span class="btn-text">登 录</span>
                    <span class="btn-loader" v-if="loading">
                      <span class="loader-dot"></span>
                      <span class="loader-dot"></span>
                      <span class="loader-dot"></span>
                    </span>
                  </button>
                </el-form-item>
              </el-form>

              <!-- 演示账号提示 -->
              <div class="demo-hint">
                <el-icon><InfoFilled /></el-icon>
                <span>演示账号: <strong>admin</strong> / <strong>admin123</strong></span>
              </div>
            </div>
          </div>

          <!-- 底部版权 -->
          <div class="login-footer">
            <p>© 2024 养殖管理系统 · 智能化养殖解决方案</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 登录成功过渡动画 -->
    <Transition name="success">
      <div v-if="showTransition" class="success-overlay">
        <div class="success-content">
          <div class="success-ring"></div>
          <div class="success-ring ring-2"></div>
          <svg viewBox="0 0 80 80" class="success-icon">
            <circle cx="40" cy="40" r="38" stroke="url(#successStroke)" stroke-width="2" fill="none"/>
            <path d="M25 40 L35 50 L55 30" stroke="url(#successStroke)" stroke-width="3" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
            <defs>
              <linearGradient id="successStroke" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="#9CAF88"/>
                <stop offset="100%" stop-color="#5D8C5D"/>
              </linearGradient>
            </defs>
          </svg>
          <h3 class="success-title">登录成功</h3>
          <p class="success-text">正在进入系统...</p>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth'
import {
  User, UserFilled, Lock, InfoFilled,
  TrendCharts, DataLine, Cloudy, Bell
} from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)
const showTransition = ref(false)
const bgCanvas = ref(null)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const features = [
  { icon: 'TrendCharts', text: '实时数据监控与分析' },
  { icon: 'DataLine', text: '智能成本核算系统' },
  { icon: 'Cloudy', text: 'AI 驱动的养殖建议' },
  { icon: 'Bell', text: '自动化预警与提醒' }
]

// 背景粒子动画
let animationId = null

function initBgAnimation() {
  const canvas = bgCanvas.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  const resize = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }
  resize()
  window.addEventListener('resize', resize)

  const particles = []
  const particleCount = 60

  for (let i = 0; i < particleCount; i++) {
    particles.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      size: Math.random() * 3 + 1,
      speedX: (Math.random() - 0.5) * 0.3,
      speedY: (Math.random() - 0.5) * 0.3,
      opacity: Math.random() * 0.3 + 0.1
    })
  }

  function animate() {
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    particles.forEach(p => {
      p.x += p.speedX
      p.y += p.speedY

      if (p.x < 0) p.x = canvas.width
      if (p.x > canvas.width) p.x = 0
      if (p.y < 0) p.y = canvas.height
      if (p.y > canvas.height) p.y = 0

      ctx.beginPath()
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(157, 175, 136, ${p.opacity})`
      ctx.fill()
    })

    // 连线
    particles.forEach((p1, i) => {
      particles.slice(i + 1).forEach(p2 => {
        const dx = p1.x - p2.x
        const dy = p1.y - p2.y
        const dist = Math.sqrt(dx * dx + dy * dy)

        if (dist < 150) {
          ctx.beginPath()
          ctx.moveTo(p1.x, p1.y)
          ctx.lineTo(p2.x, p2.y)
          ctx.strokeStyle = `rgba(157, 175, 136, ${0.1 * (1 - dist / 150)})`
          ctx.stroke()
        }
      })
    })

    animationId = requestAnimationFrame(animate)
  }
  animate()
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const result = await login(form)

    if (rememberMe.value) {
      localStorage.setItem('remembered_user', form.username)
      localStorage.setItem('remembered_pass', btoa(form.password))
    } else {
      localStorage.removeItem('remembered_user')
      localStorage.removeItem('remembered_pass')
    }

    localStorage.setItem('token', result.token)
    localStorage.setItem('user', JSON.stringify(result.user))

    showTransition.value = true
    await new Promise(r => setTimeout(r, 1800))
    router.push('/dashboard')
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initBgAnimation()

  // 恢复记住的账号
  const savedUser = localStorage.getItem('remembered_user')
  const savedPass = localStorage.getItem('remembered_pass')
  if (savedUser) {
    form.username = savedUser
    form.password = savedPass ? atob(savedPass) : ''
    rememberMe.value = true
  }
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

/* ===== 有机背景 ===== */
.organic-background {
  position: fixed;
  inset: 0;
  z-index: 0;
}

.bg-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.gradient-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg,
    rgba(247, 243, 237, 0.97) 0%,
    rgba(237, 243, 232, 0.95) 30%,
    rgba(232, 237, 227, 0.93) 60%,
    rgba(245, 241, 235, 0.95) 100%
  );
}

/* 漂浮元素 */
.floating-elements {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.float-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.5;
  animation: float 20s ease-in-out infinite;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, rgba(157, 175, 136, 0.4), rgba(212, 197, 181, 0.2));
  top: -10%;
  right: 10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, rgba(212, 165, 116, 0.3), rgba(157, 175, 136, 0.2));
  bottom: 20%;
  left: -5%;
  animation-delay: -7s;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, rgba(122, 154, 168, 0.25), rgba(212, 197, 181, 0.15));
  bottom: -5%;
  right: 30%;
  animation-delay: -14s;
}

.float-leaf {
  position: absolute;
  width: 60px;
  height: 60px;
  opacity: 0.08;
  animation: leafFloat 25s ease-in-out infinite;
}

.leaf-1 {
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='%235D8C5D' d='M12 2C7.5 2 4 6.5 4 12s4.5 10 8 10c.5 0 1-.1 1.5-.2C11.5 19 10 16 10 12s1.5-7 3.5-9.8C12.9 2.1 12.5 2 12 2z'/%3E%3C/svg%3E") no-repeat center;
  background-size: contain;
  top: 30%;
  left: 15%;
  animation-delay: -5s;
}

.leaf-2 {
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='%239CAF88' d='M17 8C8 10 5.9 16.17 3.82 21.34l1.89.66.95-2.3c.48.17.98.3 1.34.3C19 20 22 3 22 3c-1 2-8 2.25-13 3.25S2 11.5 2 13.5s1.75 3.75 1.75 3.75C7 8 17 8 17 8z'/%3E%3C/svg%3E") no-repeat center;
  background-size: contain;
  top: 60%;
  right: 20%;
  animation-delay: -12s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg) scale(1); }
  33% { transform: translate(30px, -30px) rotate(5deg) scale(1.05); }
  66% { transform: translate(-20px, 20px) rotate(-3deg) scale(0.95); }
}

@keyframes leafFloat {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  25% { transform: translate(20px, -40px) rotate(15deg); }
  50% { transform: translate(-30px, -20px) rotate(-10deg); }
  75% { transform: translate(10px, 30px) rotate(5deg); }
}

/* ===== 登录容器 ===== */
.login-container {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: flex;
}

/* ===== 品牌区域 ===== */
.brand-section {
  flex: 1.2;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.brand-content {
  max-width: 480px;
  animation: brandEnter 1s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.brand-logo {
  width: 120px;
  height: 120px;
  margin-bottom: 40px;
  filter: drop-shadow(0 8px 24px rgba(93, 140, 93, 0.2));
}

.logo-animated {
  width: 100%;
  height: 100%;
  animation: logoFloat 6s ease-in-out infinite;
}

@keyframes logoFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes brandEnter {
  0% { opacity: 0; transform: translateX(-40px); }
  100% { opacity: 1; transform: translateX(0); }
}

.brand-text {
  margin-bottom: 48px;
}

.brand-title {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.title-main {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 2.75rem;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.02em;
  line-height: 1.2;
  animation: textReveal 0.8s cubic-bezier(0.16, 1, 0.3, 1) 0.2s both;
}

.title-line {
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, var(--primary), var(--primary-light));
  border-radius: 2px;
  animation: lineGrow 0.6s cubic-bezier(0.16, 1, 0.3, 1) 0.5s both;
}

@keyframes lineGrow {
  0% { transform: scaleX(0); transform-origin: left; }
  100% { transform: scaleX(1); }
}

@keyframes textReveal {
  0% { opacity: 0; transform: translateY(20px); }
  100% { opacity: 1; transform: translateY(0); }
}

.brand-subtitle {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 1rem;
  color: var(--text-tertiary);
  letter-spacing: 3px;
  text-transform: uppercase;
  margin-bottom: 12px;
  animation: fadeUp 0.6s ease 0.4s both;
}

.brand-desc {
  font-size: 1.1rem;
  color: var(--text-secondary);
  animation: fadeUp 0.6s ease 0.5s both;
}

@keyframes fadeUp {
  0% { opacity: 0; transform: translateY(10px); }
  100% { opacity: 1; transform: translateY(0); }
}

/* 特性列表 */
.features-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  border-radius: 12px;
  background: rgba(93, 140, 93, 0.04);
  border: 1px solid rgba(93, 140, 93, 0.08);
  animation: slideRight 0.5s cubic-bezier(0.16, 1, 0.3, 1) both;
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(93, 140, 93, 0.08);
  border-color: rgba(93, 140, 93, 0.15);
  transform: translateX(4px);
}

@keyframes slideRight {
  0% { opacity: 0; transform: translateX(-20px); }
  100% { opacity: 1; transform: translateX(0); }
}

.feature-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(157, 175, 136, 0.15), rgba(93, 140, 93, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
}

.feature-text {
  font-size: 0.95rem;
  color: var(--text-secondary);
  font-weight: 500;
}

/* ===== 表单区域 ===== */
.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
}

.form-wrapper {
  width: 100%;
  max-width: 440px;
  position: relative;
  animation: formEnter 0.8s cubic-bezier(0.16, 1, 0.3, 1) 0.3s both;
}

@keyframes formEnter {
  0% { opacity: 0; transform: translateX(40px); }
  100% { opacity: 1; transform: translateX(0); }
}

/* 表单装饰背景 */
.form-bg-decoration {
  position: absolute;
  inset: -60px;
  pointer-events: none;
  z-index: 0;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(157, 175, 136, 0.1), transparent);
  animation: circlePulse 8s ease-in-out infinite;
}

.c1 {
  width: 200px;
  height: 200px;
  top: -40px;
  right: -60px;
  animation-delay: 0s;
}

.c2 {
  width: 150px;
  height: 150px;
  bottom: 40px;
  left: -40px;
  animation-delay: -3s;
}

.c3 {
  width: 100px;
  height: 100px;
  bottom: -20px;
  right: 20%;
  animation-delay: -6s;
}

@keyframes circlePulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

/* 登录卡片 */
.login-card {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.08), 0 8px 24px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.login-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg,
    rgba(255, 255, 255, 0.6) 0%,
    transparent 50%
  );
  pointer-events: none;
}

.card-inner {
  padding: 40px 36px 32px;
  position: relative;
  z-index: 1;
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-title {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.card-subtitle {
  font-size: 0.95rem;
  color: var(--text-tertiary);
}

/* 表单样式 */
.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.form-item-animated {
  animation: formItemEnter 0.5s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.form-item-animated:nth-child(1) { animation-delay: 0.4s; }
.form-item-animated:nth-child(2) { animation-delay: 0.5s; }

@keyframes formItemEnter {
  0% { opacity: 0; transform: translateY(15px); }
  100% { opacity: 1; transform: translateY(0); }
}

.input-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--text-secondary);
}

.input-label .el-icon {
  color: var(--primary);
}

.custom-input :deep(.el-input__wrapper) {
  padding: 4px 16px !important;
  border-radius: 12px !important;
  background: rgba(255, 255, 255, 0.9) !important;
  box-shadow: 0 0 0 1px rgba(139, 139, 122, 0.15) inset,
              0 2px 8px rgba(0, 0, 0, 0.02) !important;
  transition: all 0.3s ease;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(93, 140, 93, 0.3) inset,
              0 4px 12px rgba(0, 0, 0, 0.04) !important;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(93, 140, 93, 0.4) inset,
              0 0 0 4px rgba(93, 140, 93, 0.08) !important;
}

.custom-input :deep(.el-input__inner) {
  height: 48px;
  font-size: 0.95rem;
}

.custom-input :deep(.el-input__prefix) {
  color: var(--text-tertiary);
  margin-right: 10px;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 8px 0 24px;
}

.remember-check :deep(.el-checkbox__label) {
  font-size: 0.85rem;
  color: var(--text-secondary);
  padding-left: 6px;
}

.remember-check :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: var(--primary);
}

.remember-check :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: var(--primary);
  border-color: var(--primary);
}

.forgot-link {
  font-size: 0.85rem;
  color: var(--text-tertiary);
  transition: color 0.3s ease;
}

.forgot-link:hover {
  color: var(--primary);
}

/* 登录按钮 */
.submit-item {
  margin-bottom: 0 !important;
}

.login-btn {
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  color: white;
  font-size: 1rem;
  font-weight: 600;
  letter-spacing: 4px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 8px 24px rgba(93, 140, 93, 0.35);
}

.login-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, transparent 0%, rgba(255,255,255,0.2) 50%, transparent 100%);
  transform: translateX(-100%);
  transition: transform 0.6s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(93, 140, 93, 0.45);
}

.login-btn:hover::before {
  transform: translateX(100%);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn.loading {
  pointer-events: none;
}

.login-btn.loading .btn-text {
  opacity: 0;
}

.btn-loader {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.loader-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: white;
  animation: loaderBounce 1.4s ease-in-out infinite both;
}

.loader-dot:nth-child(1) { animation-delay: -0.32s; }
.loader-dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes loaderBounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 演示提示 */
.demo-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 24px;
  padding: 12px 16px;
  background: rgba(212, 165, 116, 0.08);
  border-radius: 10px;
  border: 1px solid rgba(212, 165, 116, 0.15);
  font-size: 0.85rem;
  color: var(--text-secondary);
  animation: fadeUp 0.5s ease 0.7s both;
}

.demo-hint .el-icon {
  color: var(--warning);
}

.demo-hint strong {
  color: var(--primary);
  font-weight: 600;
}

/* 底部版权 */
.login-footer {
  text-align: center;
  margin-top: 32px;
  font-size: 0.8rem;
  color: var(--text-tertiary);
  opacity: 0.8;
  animation: fadeUp 0.5s ease 0.8s both;
}

/* ===== 成功过渡动画 ===== */
.success-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(247, 243, 237, 0.98);
  backdrop-filter: blur(10px);
}

.success-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  position: relative;
}

.success-ring {
  position: absolute;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  border: 2px solid rgba(157, 175, 136, 0.2);
  animation: ringExpand 1.5s ease-out infinite;
}

.success-ring.ring-2 {
  width: 200px;
  height: 200px;
  animation-delay: 0.3s;
}

@keyframes ringExpand {
  0% { transform: scale(0.8); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

.success-icon {
  width: 80px;
  height: 80px;
  position: relative;
  z-index: 1;
  animation: iconPop 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) 0.2s both;
}

@keyframes iconPop {
  0% { transform: scale(0); }
  100% { transform: scale(1); }
}

.success-title {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  animation: fadeUp 0.5s ease 0.4s both;
}

.success-text {
  font-size: 0.95rem;
  color: var(--text-secondary);
  animation: fadeUp 0.5s ease 0.5s both;
}

/* 过渡动画 */
.success-enter-active {
  transition: all 0.4s ease;
}

.success-leave-active {
  transition: all 0.3s ease;
}

.success-enter-from,
.success-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

/* ===== 响应式 ===== */
@media (max-width: 992px) {
  .login-container {
    flex-direction: column;
  }

  .brand-section {
    flex: none;
    padding: 40px 30px;
    min-height: auto;
  }

  .brand-content {
    text-align: center;
  }

  .brand-logo {
    margin: 0 auto 30px;
    width: 80px;
    height: 80px;
  }

  .title-main {
    font-size: 2rem;
  }

  .title-line {
    margin: 0 auto;
  }

  .features-list {
    display: none;
  }

  .form-section {
    padding: 30px;
  }

  .form-bg-decoration {
    display: none;
  }
}

@media (max-width: 576px) {
  .brand-section {
    padding: 30px 20px;
  }

  .title-main {
    font-size: 1.5rem;
  }

  .brand-subtitle {
    font-size: 0.8rem;
  }

  .card-inner {
    padding: 30px 24px 24px;
  }

  .card-title {
    font-size: 1.4rem;
  }
}
</style>
