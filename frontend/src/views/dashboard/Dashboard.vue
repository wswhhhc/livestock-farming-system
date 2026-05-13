<template>
  <div class="dashboard-container">
    <!-- 动态背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-blob blob-1"></div>
      <div class="bg-blob blob-2"></div>
      <div class="bg-blob blob-3"></div>
    </div>

    <!-- 顶部问候区 - 有机卡片设计 -->
    <div class="welcome-section">
      <div class="welcome-card">
        <div class="welcome-left">
          <div class="welcome-icon">
            <el-icon :size="28"><Sunny /></el-icon>
          </div>
          <div class="welcome-text">
            <h1 class="greeting-title">{{ greeting }}，{{ user.realName || user.username }}</h1>
            <p class="greeting-subtitle">今天您的养殖场运转良好，让我们一起看看数据吧</p>
          </div>
        </div>
        <div class="welcome-right">
          <div class="date-display">
            <span class="date-day">{{ currentDay }}</span>
            <span class="date-month">{{ currentMonth }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计卡片 - 有机现代主义风格 -->
    <div class="stats-section">
      <div class="stat-card" :class="[`stat-${stat.type}`]" v-for="(stat, index) in stats" :key="stat.id" :style="{ animationDelay: `${index * 0.1}s` }">
        <div class="stat-glow"></div>
        <div class="stat-content">
          <div class="stat-icon-wrap">
            <el-icon :size="24">
              <component :is="stat.icon" />
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-value">
              <AnimatedNumber :value="stat.value" :prefix="stat.prefix" :decimal="stat.decimal" />
            </div>
            <div class="stat-trend" v-if="stat.trend">
              <el-icon :size="12"><ArrowUp v-if="stat.trend > 0" /><ArrowDown v-else /></el-icon>
              <span>{{ Math.abs(stat.trend) }}%</span>
            </div>
          </div>
        </div>
        <div class="stat-decoration">
          <svg viewBox="0 0 100 100" class="organic-shape">
            <path :d="stat.shapePath" fill="currentColor" opacity="0.08"/>
          </svg>
        </div>
      </div>
    </div>

    <!-- 主图表区域 -->
    <div class="main-charts">
      <el-row :gutter="20">
        <!-- 品种分布 - 圆环图 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="8">
          <div class="chart-card-wrapper" v-intersection-observer="onChartVisible">
            <el-card class="chart-card">
              <template #header>
                <div class="chart-header">
                  <span class="chart-title">品种存栏分布</span>
                  <el-tag size="small" effect="plain">实时</el-tag>
                </div>
              </template>
              <div ref="pieChartRef" class="chart-container pie-chart"></div>
              <div class="chart-legend" v-if="categoryDist.length > 0">
                <div class="legend-item" v-for="(item, i) in categoryDist.slice(0, 4)" :key="i">
                  <span class="legend-dot" :style="{ background: getChartColor(i) }"></span>
                  <span class="legend-name">{{ item.name }}</span>
                  <span class="legend-value">{{ item.quantity }}头</span>
                </div>
              </div>
            </el-card>
          </div>
        </el-col>

        <!-- 生长阶段 - 柱状图 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="8">
          <div class="chart-card-wrapper" style="animation-delay: 0.1s">
            <el-card class="chart-card">
              <template #header>
                <div class="chart-header">
                  <span class="chart-title">生长阶段分布</span>
                  <el-tag size="small" effect="plain" type="success">健康</el-tag>
                </div>
              </template>
              <div ref="barChartRef" class="chart-container"></div>
            </el-card>
          </div>
        </el-col>

        <!-- 场地利用率 - 列表式进度条 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="8">
          <div class="chart-card-wrapper" style="animation-delay: 0.2s">
            <el-card class="chart-card">
              <template #header>
                <div class="chart-header">
                  <span class="chart-title">场地利用率</span>
                  <el-tag size="small" effect="plain" type="warning">监控中</el-tag>
                </div>
              </template>
              <div class="site-list">
                <div v-for="item in siteUtil" :key="item.siteName" class="site-item">
                  <div class="site-info">
                    <div class="site-name">{{ item.siteName }}</div>
                    <div class="site-capacity">{{ item.currentStock }} / {{ item.capacity }} 头</div>
                  </div>
                  <div class="site-progress">
                    <div class="progress-track">
                      <div
                        class="progress-fill"
                        :style="{ width: `${Math.min(item.usageRate, 100)}%`, background: getUtilColor(item.usageRate) }"
                      ></div>
                    </div>
                    <span class="progress-text" :style="{ color: getUtilColor(item.usageRate) }">{{ item.usageRate }}%</span>
                  </div>
                </div>
                <el-empty v-if="siteUtil.length === 0" description="暂无场地数据" :image-size="60" />
              </div>
            </el-card>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 成本趋势 - 折线图 -->
    <div class="trend-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="24" :md="16">
          <div class="chart-card-wrapper large">
            <el-card class="chart-card trend-card">
              <template #header>
                <div class="chart-header">
                  <div class="chart-title-group">
                    <span class="chart-title">成本趋势分析</span>
                    <span class="chart-subtitle">近{{ costMonths }}个月支出情况</span>
                  </div>
                  <el-radio-group v-model="costMonths" size="small" @change="loadCostTrend">
                    <el-radio-button :value="3">3月</el-radio-button>
                    <el-radio-button :value="6">6月</el-radio-button>
                    <el-radio-button :value="12">12月</el-radio-button>
                  </el-radio-group>
                </div>
              </template>
              <div ref="lineChartRef" class="chart-container trend-chart"></div>
            </el-card>
          </div>
        </el-col>

        <!-- 快捷操作 - 有机按钮设计 -->
        <el-col :xs="24" :sm="24" :md="8">
          <div class="chart-card-wrapper">
            <el-card class="chart-card action-card">
              <template #header>
                <div class="chart-header">
                  <span class="chart-title">快捷操作</span>
                </div>
              </template>
              <div class="action-grid">
                <div class="action-item" v-for="action in quickActions" :key="action.id" @click="action.handler">
                  <div class="action-icon" :class="`action-${action.type}`">
                    <el-icon :size="22">
                      <component :is="action.icon" />
                    </el-icon>
                  </div>
                  <span class="action-name">{{ action.name }}</span>
                  <span class="action-desc">{{ action.desc }}</span>
                </div>
              </div>
            </el-card>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 底部区域 - 待办与数据概览 -->
    <div class="bottom-section">
      <el-row :gutter="20">
        <!-- 近期待办 -->
        <el-col :xs="24" :sm="24" :md="16">
          <div class="chart-card-wrapper">
            <el-card class="chart-card todo-card">
              <template #header>
                <div class="chart-header">
                  <div class="chart-title-group">
                    <span class="chart-title">近期待办</span>
                    <el-badge :value="pendingCount" v-if="pendingCount > 0" class="todo-badge" />
                  </div>
                  <el-button type="primary" link size="small">查看全部</el-button>
                </div>
              </template>
              <div class="todo-list">
                <div v-for="item in pendingItems" :key="item.label" class="todo-item" :class="`todo-${item.type}`">
                  <div class="todo-indicator"></div>
                  <div class="todo-icon" :class="item.type">
                    <el-icon :size="18">
                      <component :is="item.icon" />
                    </el-icon>
                  </div>
                  <div class="todo-content">
                    <div class="todo-title">{{ item.label }}</div>
                    <div class="todo-meta">
                      <el-tag :type="item.type" size="small" effect="dark">{{ item.count }} 项</el-tag>
                      <span class="todo-time">{{ item.time }}</span>
                    </div>
                  </div>
                  <el-button type="primary" link size="small" @click="item.to && $router.push(item.to)">
                    处理 <el-icon><ArrowRight /></el-icon>
                  </el-button>
                </div>
                <el-empty v-if="pendingItems.length === 0" description="暂无待办事项" :image-size="60" />
              </div>
            </el-card>
          </div>
        </el-col>

        <!-- 基础数据概览 -->
        <el-col :xs="24" :sm="24" :md="8">
          <div class="chart-card-wrapper">
            <el-card class="chart-card summary-card">
              <template #header>
                <div class="chart-header">
                  <span class="chart-title">基础数据</span>
                </div>
              </template>
              <div class="summary-list">
                <div class="summary-item">
                  <div class="summary-icon" style="background: rgba(107, 158, 107, 0.12); color: var(--success);">
                    <el-icon :size="20"><Collection /></el-icon>
                  </div>
                  <div class="summary-info">
                    <div class="summary-label">家畜种类</div>
                    <div class="summary-value">
                      <AnimatedNumber :value="data?.summary?.categoryCount ?? 0" />
                      <span class="unit">种</span>
                    </div>
                  </div>
                </div>
                <div class="summary-item">
                  <div class="summary-icon" style="background: rgba(212, 167, 116, 0.12); color: var(--warning);">
                    <el-icon :size="20"><HomeFilled /></el-icon>
                  </div>
                  <div class="summary-info">
                    <div class="summary-label">养殖场地</div>
                    <div class="summary-value">
                      <AnimatedNumber :value="data?.summary?.siteCount ?? 0" />
                      <span class="unit">个</span>
                    </div>
                  </div>
                </div>
                <div class="summary-item">
                  <div class="summary-icon" style="background: rgba(201, 123, 123, 0.12); color: var(--danger);">
                    <el-icon :size="20"><Money /></el-icon>
                  </div>
                  <div class="summary-info">
                    <div class="summary-label">累计成本</div>
                    <div class="summary-value">
                      <span class="prefix">¥</span>
                      <AnimatedNumber :value="data?.revenue?.totalCost" decimal="2" />
                    </div>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import AnimatedNumber from '../../components/AnimatedNumber.vue'
import { useRouter } from 'vue-router'
import { getDashboard, getCategoryDist, getCostTrend, getSiteUtil, getStageDist } from '../../api/dashboard'
import { generateAdvice } from '../../api/advice'
import * as echarts from 'echarts'
import {
  UserFilled, Box, Coin, TrendCharts,
  ArrowUp, ArrowDown, Sunny, Warning,
  ChatDotRound, Calendar, Money, Plus,
  ChatDotSquare, Collection, HomeFilled,
  ArrowRight, Bell
} from '@element-plus/icons-vue'

const router = useRouter()

// ===== 数据状态 =====
const data = ref(null)
const categoryDist = ref([])
const costTrendData = ref([])
const siteUtil = ref([])
const stageDist = ref([])
const costMonths = ref(6)
const generating = ref(false)
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))

// ===== 图表引用 =====
const pieChartRef = ref(null)
const barChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let barChart = null
let lineChart = null

// ===== 图表颜色方案 - 有机大地色 =====
const chartColors = [
  '#5D8C5D',  // 森林绿
  '#D4A574',  // 暖土色
  '#7A9AA8',  // 雾霾蓝
  '#9CAF88',  // 鼠尾草绿
  '#C4A882',  // 陶土色
  '#8B8B7A',  // 岩石灰
  '#6B9E6B',  // 苔藓绿
  '#B8936A',  // 沙棕色
]

// ===== 计算属性 =====
const currentDay = computed(() => new Date().getDate())
const currentMonth = computed(() => {
  const months = ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月']
  return months[new Date().getMonth()]
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const stats = computed(() => {
  if (!data.value?.summary) return []
  const profit = data.value.revenue?.estimatedProfit || 0
  return [
    {
      id: 1,
      icon: 'UserFilled',
      label: '当前存栏总数',
      value: data.value.summary.totalQuantity,
      prefix: '',
      decimal: 0,
      type: 'primary',
      trend: 5.2,
      shapePath: 'M20,50 Q10,30 30,20 Q50,10 70,20 Q90,30 80,50 Q70,70 50,80 Q30,70 20,50'
    },
    {
      id: 2,
      icon: 'Box',
      label: '饲养中批次',
      value: data.value.summary.activeBatchCount,
      prefix: '',
      decimal: 0,
      type: 'success',
      trend: 3.8,
      shapePath: 'M30,30 Q50,10 70,30 Q90,50 70,70 Q50,90 30,70 Q10,50 30,30'
    },
    {
      id: 3,
      icon: 'Coin',
      label: '预估总收入',
      value: data.value.revenue?.estimatedRevenue || 0,
      prefix: '¥',
      decimal: 2,
      type: 'warning',
      shapePath: 'M25,50 Q20,30 40,25 Q60,20 75,35 Q85,50 75,65 Q60,80 40,75 Q20,70 25,50'
    },
    {
      id: 4,
      icon: 'TrendCharts',
      label: '预估利润',
      value: profit,
      prefix: '¥',
      decimal: 2,
      type: profit >= 0 ? 'info' : 'danger',
      trend: profit >= 0 ? 12.5 : -8.3,
      shapePath: 'M40,20 Q60,20 70,40 Q80,60 60,75 Q40,85 25,70 Q15,50 40,20'
    }
  ]
})

const pendingCount = computed(() => data.value?.pending
  ? Object.values(data.value.pending).reduce((a, b) => a + b, 0)
  : 0
)

const pendingItems = computed(() => {
  if (!data.value?.pending) return []
  const p = data.value.pending
  const items = []
  if (p.unreadAdvice > 0) items.push({
    label: '未读养殖建议',
    count: p.unreadAdvice,
    type: 'warning',
    icon: 'ChatDotSquare',
    time: '需要关注',
    to: '/advice'
  })
  if (p.slaughterReady > 0) items.push({
    label: '批次已达出栏阶段',
    count: p.slaughterReady,
    type: 'success',
    icon: 'Box',
    time: '可销售',
    to: '/batch'
  })
  if (p.overdueBatches > 0) items.push({
    label: '超过预计出栏日',
    count: p.overdueBatches,
    type: 'danger',
    icon: 'Warning',
    time: '需处理',
    to: '/batch'
  })
  return items
})

const quickActions = [
  {
    id: 1,
    name: '登记存栏',
    desc: '新增批次',
    icon: 'Plus',
    type: 'success',
    handler: () => router.push('/batch/add')
  },
  {
    id: 2,
    name: '录入成本',
    desc: '记录支出',
    icon: 'Money',
    type: 'warning',
    handler: () => router.push('/cost')
  },
  {
    id: 3,
    name: '生成建议',
    desc: 'AI 分析',
    icon: 'ChatDotSquare',
    type: 'primary',
    handler: handleGenerateAdvice
  },
  {
    id: 4,
    name: '收益预估',
    desc: '财务预测',
    icon: 'TrendCharts',
    type: 'info',
    handler: () => router.push('/revenue')
  }
]

// ===== 方法 =====
function getChartColor(index) {
  return chartColors[index % chartColors.length]
}

function getUtilColor(rate) {
  if (rate >= 90) return '#C97B7B'
  if (rate >= 70) return '#D4A574'
  return '#5D8C5D'
}

function onChartVisible(entries) {
  entries.forEach(entry => {
    if (entry.isIntersecting) {
      entry.target.classList.add('visible')
    }
  })
}

// ===== 图表初始化 =====
function initCharts() {
  initPieChart()
  initBarChart()
  initLineChart()
}

function initPieChart() {
  if (!pieChartRef.value) return
  pieChart?.dispose()
  pieChart = echarts.init(pieChartRef.value, null, { renderer: 'canvas' })

  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: 'rgba(139, 139, 122, 0.2)',
      borderWidth: 1,
      padding: [12, 16],
      textStyle: { color: '#2C3E2C', fontSize: 13 },
      formatter: '{b}: {c}头 ({d}%)'
    },
    series: [{
      type: 'pie',
      radius: ['55%', '80%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 3
      },
      label: { show: false },
      emphasis: {
        scale: true,
        scaleSize: 8,
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold',
          color: '#2C3E2C',
          formatter: '{b}\n{c}头'
        }
      },
      data: categoryDist.value.length > 0
        ? categoryDist.value.map((d, i) => ({
            value: d.quantity,
            name: d.name,
            itemStyle: { color: chartColors[i % chartColors.length] }
          }))
        : [{ value: 1, name: '暂无数据', itemStyle: { color: 'rgba(139, 139, 122, 0.15)' } }]
    }]
  }
  pieChart.setOption(option)
}

function initBarChart() {
  if (!barChartRef.value) return
  barChart?.dispose()
  barChart = echarts.init(barChartRef.value, null, { renderer: 'canvas' })

  const stages = stageDist.value.map(d => d.stage)
  const counts = stageDist.value.map(d => d.count)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: 'rgba(139, 139, 122, 0.2)',
      padding: [12, 16],
      textStyle: { color: '#2C3E2C' }
    },
    grid: {
      left: 20,
      right: 20,
      top: 20,
      bottom: 20,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: stages,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: '#8A9A8A',
        fontSize: 12,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      splitLine: {
        lineStyle: {
          color: 'rgba(139, 139, 122, 0.1)',
          type: 'dashed'
        }
      },
      axisLabel: { show: false }
    },
    series: [{
      type: 'bar',
      barWidth: '50%',
      itemStyle: {
        borderRadius: [8, 8, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#5D8C5D' },
          { offset: 1, color: 'rgba(93, 140, 93, 0.2)' }
        ])
      },
      emphasis: {
        itemStyle: { color: '#7DB97D' }
      },
      data: counts
    }]
  }
  barChart.setOption(option)
}

function initLineChart() {
  if (!lineChartRef.value) return
  lineChart?.dispose()
  lineChart = echarts.init(lineChartRef.value, null, { renderer: 'canvas' })
  updateLineChart()
}

function updateLineChart() {
  if (!lineChart) return
  const months = costTrendData.value.map(d => d.month)
  const amounts = costTrendData.value.map(d => Number(d.totalAmount))

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: 'rgba(139, 139, 122, 0.2)',
      padding: [12, 16],
      textStyle: { color: '#2C3E2C' },
      formatter: params => {
        const p = params[0]
        return `<div style="font-weight:600;margin-bottom:4px">${p.axisValue}</div>
                <div style="color:#5D8C5D">支出: ¥${Number(p.value).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</div>`
      }
    },
    grid: {
      left: 20,
      right: 20,
      top: 30,
      bottom: 20,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months,
      boundaryGap: false,
      axisLine: {
        lineStyle: { color: 'rgba(139, 139, 122, 0.2)' }
      },
      axisLabel: {
        color: '#8A9A8A',
        fontSize: 12
      },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      splitLine: {
        lineStyle: {
          color: 'rgba(139, 139, 122, 0.1)',
          type: 'dashed'
        }
      },
      axisLabel: {
        color: '#8A9A8A',
        fontSize: 11,
        formatter: v => v >= 10000 ? (v/10000).toFixed(0) + '万' : v
      }
    },
    series: [{
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        width: 3,
        color: '#5D8C5D',
        shadowColor: 'rgba(93, 140, 93, 0.3)',
        shadowBlur: 10,
        shadowOffsetY: 5
      },
      itemStyle: {
        color: '#5D8C5D',
        borderColor: '#fff',
        borderWidth: 2
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(93, 140, 93, 0.3)' },
          { offset: 1, color: 'rgba(93, 140, 93, 0.02)' }
        ])
      },
      data: amounts
    }]
  }
  lineChart.setOption(option)
}

async function handleGenerateAdvice() {
  generating.value = true
  try {
    await generateAdvice()
    ElMessage.success('建议生成成功')
  } finally {
    generating.value = false
  }
}

// ===== 数据加载 =====
async function loadAll() {
  data.value = await getDashboard()
  const [catDist, trend, util, stages] = await Promise.all([
    getCategoryDist(),
    getCostTrend(costMonths.value),
    getSiteUtil(),
    getStageDist()
  ])
  categoryDist.value = catDist
  costTrendData.value = trend
  siteUtil.value = util
  stageDist.value = stages
  await nextTick()
  initCharts()
}

async function loadCostTrend() {
  costTrendData.value = await getCostTrend(costMonths.value)
  await nextTick()
  updateLineChart()
}

// ===== 生命周期 =====
onMounted(() => {
  loadAll()
  window.addEventListener('resize', () => {
    pieChart?.resize()
    barChart?.resize()
    lineChart?.resize()
  })
})

onBeforeUnmount(() => {
  pieChart?.dispose()
  barChart?.dispose()
  lineChart?.dispose()
})

// 自定义指令：交叉观察器
const vIntersectionObserver = {
  mounted(el, binding) {
    const observer = new IntersectionObserver(binding.value, { threshold: 0.1 })
    observer.observe(el)
    el._observer = observer
  },
  unmounted(el) {
    el._observer?.disconnect()
  }
}
</script>

<script>
export default {
  directives: {
    intersectionObserver: {
      mounted(el, binding) {
        const observer = new IntersectionObserver(binding.value, { threshold: 0.1 })
        observer.observe(el)
        el._observer = observer
      },
      unmounted(el) {
        el._observer?.disconnect()
      }
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  position: relative;
  padding-bottom: 40px;
}

/* ===== 背景装饰 ===== */
.bg-decoration {
  position: fixed;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
}

.bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  animation: blobFloat 20s ease-in-out infinite;
}

.blob-1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, rgba(157, 175, 136, 0.3), rgba(212, 197, 181, 0.2));
  top: -10%;
  right: -10%;
  animation-delay: 0s;
}

.blob-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, rgba(212, 165, 116, 0.2), rgba(157, 175, 136, 0.15));
  bottom: 10%;
  left: -5%;
  animation-delay: -7s;
}

.blob-3 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, rgba(122, 154, 168, 0.15), rgba(212, 197, 181, 0.1));
  bottom: -5%;
  right: 20%;
  animation-delay: -14s;
}

@keyframes blobFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

/* ===== 欢迎区域 ===== */
.welcome-section {
  margin-bottom: 28px;
  animation: fadeUp 0.8s var(--ease-out-expo) both;
}

.welcome-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, rgba(93, 140, 93, 0.08) 0%, rgba(212, 165, 116, 0.05) 100%);
  border: 1px solid rgba(139, 139, 122, 0.15);
  border-radius: var(--radius-xl);
  padding: 24px 32px;
  backdrop-filter: blur(12px);
  position: relative;
  overflow: hidden;
}

.welcome-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--primary), var(--warning), var(--primary));
}

.welcome-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.welcome-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-light) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(93, 140, 93, 0.3);
  animation: iconPulse 3s ease-in-out infinite;
}

@keyframes iconPulse {
  0%, 100% { box-shadow: 0 8px 24px rgba(93, 140, 93, 0.3); }
  50% { box-shadow: 0 8px 32px rgba(93, 140, 93, 0.5); }
}

.greeting-title {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.greeting-subtitle {
  color: var(--text-tertiary);
  font-size: 0.95rem;
}

.welcome-right {
  text-align: right;
}

.date-display {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.date-day {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 3rem;
  font-weight: 700;
  line-height: 1;
  color: var(--primary);
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.date-month {
  font-size: 0.9rem;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 2px;
}

/* ===== 统计卡片区域 ===== */
.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

@media (max-width: 1200px) {
  .stats-section { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 640px) {
  .stats-section { grid-template-columns: 1fr; }
  .welcome-card { flex-direction: column; gap: 20px; text-align: center; }
  .welcome-left { flex-direction: column; }
  .welcome-right { align-items: center; }
  .date-display { align-items: center; }
}

.stat-card {
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  padding: 24px;
  position: relative;
  overflow: hidden;
  animation: slideUp 0.6s var(--ease-out-expo) both;
  transition: all var(--transition-normal);
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--glass-shadow-hover);
}

.stat-glow {
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  opacity: 0.15;
  filter: blur(40px);
  transition: opacity var(--transition-normal);
}

.stat-primary .stat-glow { background: var(--primary); }
.stat-success .stat-glow { background: var(--success); }
.stat-warning .stat-glow { background: var(--warning); }
.stat-danger .stat-glow { background: var(--danger); }
.stat-info .stat-glow { background: var(--info); }

.stat-card:hover .stat-glow {
  opacity: 0.25;
}

.stat-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  position: relative;
  z-index: 1;
}

.stat-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-base);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all var(--transition-fast);
}

.stat-primary .stat-icon-wrap {
  background: linear-gradient(135deg, rgba(93, 140, 93, 0.15), rgba(93, 140, 93, 0.08));
  color: var(--primary);
}
.stat-success .stat-icon-wrap {
  background: linear-gradient(135deg, rgba(107, 158, 107, 0.15), rgba(107, 158, 107, 0.08));
  color: var(--success);
}
.stat-warning .stat-icon-wrap {
  background: linear-gradient(135deg, rgba(212, 165, 116, 0.15), rgba(212, 165, 116, 0.08));
  color: var(--warning);
}
.stat-danger .stat-icon-wrap {
  background: linear-gradient(135deg, rgba(201, 123, 123, 0.15), rgba(201, 123, 123, 0.08));
  color: var(--danger);
}
.stat-info .stat-icon-wrap {
  background: linear-gradient(135deg, rgba(122, 154, 168, 0.15), rgba(122, 154, 168, 0.08));
  color: var(--info);
}

.stat-card:hover .stat-icon-wrap {
  transform: scale(1.1);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 0.85rem;
  color: var(--text-tertiary);
  margin-bottom: 6px;
}

.stat-value {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-trend {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 6px;
  padding: 2px 8px;
  background: rgba(93, 140, 93, 0.08);
  border-radius: 100px;
  font-size: 0.75rem;
  color: var(--success);
  font-weight: 500;
}

.stat-decoration {
  position: absolute;
  bottom: -20px;
  right: -20px;
  width: 120px;
  height: 120px;
  color: var(--primary);
  opacity: 0.6;
  pointer-events: none;
}

/* ===== 图表卡片 ===== */
.main-charts,
.trend-section,
.bottom-section {
  margin-bottom: 20px;
}

.chart-card-wrapper {
  animation: fadeUp 0.6s var(--ease-out-expo) both;
  animation-delay: 0.2s;
  opacity: 0;
}

.chart-card-wrapper.visible,
.chart-card-wrapper {
  opacity: 1;
}

.chart-card {
  height: 100%;
  min-height: 360px;
}

.chart-card :deep(.el-card__body) {
  padding: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-title {
  font-family: 'Space Grotesk', sans-serif;
  font-weight: 600;
  font-size: 1rem;
  color: var(--text-primary);
}

.chart-subtitle {
  font-size: 0.8rem;
  color: var(--text-tertiary);
}

.chart-container {
  width: 100%;
  height: 260px;
}

.pie-chart {
  height: 200px;
}

.chart-legend {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-name {
  color: var(--text-secondary);
  flex: 1;
}

.legend-value {
  color: var(--text-primary);
  font-weight: 600;
  font-family: 'Space Grotesk', sans-serif;
}

/* ===== 场地利用率 ===== */
.site-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.site-item {
  padding: 4px 0;
}

.site-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.site-name {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 0.9rem;
}

.site-capacity {
  font-size: 0.8rem;
  color: var(--text-tertiary);
  font-family: 'Space Grotesk', sans-serif;
}

.site-progress {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-track {
  flex: 1;
  height: 8px;
  background: rgba(139, 139, 122, 0.1);
  border-radius: 100px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 100px;
  transition: width 0.8s var(--ease-out-expo);
}

.progress-text {
  font-size: 0.85rem;
  font-weight: 600;
  font-family: 'Space Grotesk', sans-serif;
  min-width: 40px;
  text-align: right;
}

/* ===== 快捷操作 ===== */
.action-card {
  min-height: 360px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px 16px;
  border-radius: var(--radius-base);
  background: rgba(139, 139, 122, 0.03);
  border: 1px solid rgba(139, 139, 122, 0.08);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-item:hover {
  background: var(--glass-bg-strong);
  border-color: var(--glass-border-strong);
  transform: translateY(-2px);
  box-shadow: var(--glass-shadow);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-base);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.action-success { background: rgba(107, 158, 107, 0.12); color: var(--success); }
.action-warning { background: rgba(212, 165, 116, 0.12); color: var(--warning); }
.action-primary { background: rgba(93, 140, 93, 0.12); color: var(--primary); }
.action-info { background: rgba(122, 154, 168, 0.12); color: var(--info); }

.action-item:hover .action-icon {
  transform: scale(1.1);
}

.action-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.95rem;
}

.action-desc {
  font-size: 0.8rem;
  color: var(--text-tertiary);
}

/* ===== 趋势图表 ===== */
.trend-card {
  min-height: 420px;
}

.trend-chart {
  height: 320px;
}

/* ===== 待办列表 ===== */
.todo-card {
  min-height: 360px;
}

.todo-badge :deep(.el-badge__content) {
  background: var(--danger);
  border: none;
  font-weight: 600;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: var(--radius-base);
  background: rgba(139, 139, 122, 0.03);
  border: 1px solid rgba(139, 139, 122, 0.08);
  transition: all var(--transition-fast);
}

.todo-item:hover {
  background: var(--glass-bg);
  border-color: var(--glass-border);
  transform: translateX(4px);
}

.todo-indicator {
  width: 4px;
  height: 40px;
  border-radius: 2px;
  flex-shrink: 0;
}

.todo-warning .todo-indicator { background: var(--warning); }
.todo-success .todo-indicator { background: var(--success); }
.todo-danger .todo-indicator { background: var(--danger); }

.todo-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.todo-icon.warning { background: rgba(212, 165, 116, 0.12); color: var(--warning); }
.todo-icon.success { background: rgba(107, 158, 107, 0.12); color: var(--success); }
.todo-icon.danger { background: rgba(201, 123, 123, 0.12); color: var(--danger); }

.todo-content {
  flex: 1;
}

.todo-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.todo-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.todo-time {
  font-size: 0.8rem;
  color: var(--text-tertiary);
}

/* ===== 数据概览 ===== */
.summary-card {
  min-height: 360px;
}

.summary-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: var(--radius-base);
  background: rgba(139, 139, 122, 0.03);
  border: 1px solid rgba(139, 139, 122, 0.08);
  transition: all var(--transition-fast);
}

.summary-item:hover {
  background: var(--glass-bg);
  border-color: var(--glass-border);
}

.summary-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.summary-info {
  flex: 1;
}

.summary-label {
  font-size: 0.85rem;
  color: var(--text-tertiary);
  margin-bottom: 4px;
}

.summary-value {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.summary-value .unit,
.summary-value .prefix {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-tertiary);
}

.summary-value .prefix {
  color: var(--danger);
}

/* ===== 动画 ===== */
@keyframes fadeUp {
  0% { opacity: 0; transform: translateY(20px); }
  100% { opacity: 1; transform: translateY(0); }
}

@keyframes slideUp {
  0% { opacity: 0; transform: translateY(40px) scale(0.95); }
  100% { opacity: 1; transform: translateY(0) scale(1); }
}
</style>
