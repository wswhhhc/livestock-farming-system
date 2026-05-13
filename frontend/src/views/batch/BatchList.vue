<template>
  <div class="page-container">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon :size="28"><Histogram /></el-icon>
        </div>
        <div class="header-text">
          <h1 class="page-title">存栏批次管理</h1>
          <p class="page-subtitle">管理您的养殖批次，追踪生长状态与养殖进度</p>
        </div>
      </div>
      <el-button type="primary" size="large" class="add-btn" @click="handleAdd">
        <el-icon :size="18"><Plus /></el-icon>
        <span>登记存栏</span>
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon bg-primary">
          <el-icon><Box /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-number">{{ list.filter(i => i.status === 1).length }}</span>
          <span class="stat-label">饲养中批次</span>
        </div>
        <div class="stat-trend up">
          <el-icon><ArrowUp /></el-icon>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-success">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-number">{{ list.filter(i => i.status === 2).length }}</span>
          <span class="stat-label">已完成批次</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-warning">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-number">{{ totalAnimals }}</span>
          <span class="stat-label">当前存栏总数</span>
        </div>
      </div>
    </div>

    <!-- 主内容卡片 -->
    <el-card class="data-card" shadow="never">
      <!-- 筛选栏 -->
      <div class="filter-section">
        <div class="filter-group">
          <el-form :model="filters" inline class="filter-form">
            <el-form-item label="种类筛选">
              <el-tree-select
                v-model="filters.categoryId"
                :data="categoryTree"
                :props="{ label: 'categoryName', value: 'id' }"
                placeholder="全部种类"
                clearable
                check-strictly
                class="filter-select"
                :prefix-icon="Collection"
              />
            </el-form-item>
            <el-form-item label="状态筛选">
              <el-select v-model="filters.status" placeholder="全部状态" clearable class="filter-select">
                <el-option label="饲养中" :value="1">
                  <div class="select-option">
                    <span class="option-dot success"></span>
                    <span>饲养中</span>
                  </div>
                </el-option>
                <el-option label="已结束" :value="2">
                  <div class="select-option">
                    <span class="option-dot info"></span>
                    <span>已结束</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="搜索">
              <el-input
                v-model="filters.keyword"
                placeholder="批次号 / 负责人"
                clearable
                class="filter-input"
                :prefix-icon="Search"
                @keyup.enter="loadList"
              />
            </el-form-item>
          </el-form>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="loadList" class="search-btn">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetFilters" class="reset-btn">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="list"
        v-loading="loading"
        class="data-table"
        :header-cell-style="headerStyle"
        row-class-name="table-row"
      >
        <el-table-column type="index" width="50" align="center" label="序号">
          <template #default="{ $index }">
            <span class="index-badge">{{ $index + 1 }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="batchNo" label="批次编号" width="170">
          <template #default="{ row }">
            <div class="batch-cell">
              <div class="batch-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="batch-info">
                <span class="batch-no">{{ row.batchNo }}</span>
                <el-tag size="small" :type="statusType(row.status)" effect="light" class="status-tag">
                  {{ statusText(row.status) }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="categoryName" label="家畜种类" width="110" align="center">
          <template #default="{ row }">
            <span class="category-badge">{{ row.categoryName }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="currentStage" label="生长阶段" width="110" align="center">
          <template #default="{ row }">
            <div class="stage-tag" :class="stageClass(row.currentStage)">
              <el-icon v-if="row.currentStage === 3"><Medal /></el-icon>
              <el-icon v-else-if="row.currentStage === 1"><Star /></el-icon>
              <el-icon v-else><TrendCharts /></el-icon>
              <span>{{ stageLabel(row.currentStage) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="存栏数量" width="130" align="center">
          <template #default="{ row }">
            <div class="quantity-cell">
              <div class="quantity-main" :class="{ 'low-stock': row.currentQuantity < row.initialQuantity * 0.5 }">
                <el-icon><UserFilled /></el-icon>
                <AnimatedNumber :value="row.currentQuantity" />
              </div>
              <div class="quantity-sub">
                入场: {{ row.initialQuantity }}头
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="entryDate" label="入场日期" width="120" align="center">
          <template #default="{ row }">
            <div class="date-cell">
              <el-icon><Calendar /></el-icon>
              <span>{{ row.entryDate }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="siteName" label="养殖场地" min-width="120">
          <template #default="{ row }">
            <div class="site-cell">
              <div class="site-icon">
                <el-icon><OfficeBuilding /></el-icon>
              </div>
              <span>{{ row.siteName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="responsiblePerson" label="负责人" width="100" align="center">
          <template #default="{ row }">
            <div v-if="row.responsiblePerson" class="person-cell">
              <div class="person-avatar">{{ row.responsiblePerson.charAt(0) }}</div>
              <span>{{ row.responsiblePerson }}</span>
            </div>
            <span v-else class="empty-text">未分配</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-tooltip content="编辑批次" placement="top">
                <button class="action-btn" @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip content="查看详情" placement="top">
                <button class="action-btn view" @click="$router.push('/batch/' + row.id)">
                  <el-icon><View /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip content="删除批次" placement="top">
                <button class="action-btn danger" @click="confirmDelete(row.id)">
                  <el-icon><Delete /></el-icon>
                </button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty v-if="list.length === 0 && !loading" description="暂无存栏批次">
        <template #image>
          <div class="empty-custom">
            <el-icon :size="64" color="var(--text-muted)"><Box /></el-icon>
          </div>
        </template>
        <el-button type="primary" @click="handleAdd">立即登记</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  Plus, Histogram, Search, Refresh, Collection,
  Box, CircleCheck, User, ArrowUp, Document,
  Edit, View, Delete, Calendar, OfficeBuilding,
  UserFilled, Medal, Star, TrendCharts
} from '@element-plus/icons-vue'
import { getBatchList, deleteBatch } from '../../api/batch'
import { getCategoryTree } from '../../api/category'
import AnimatedNumber from '../../components/AnimatedNumber.vue'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const categoryTree = ref([])

const filters = ref({
  categoryId: null,
  status: null,
  keyword: ''
})

// 计算属性：总存栏数
const totalAnimals = computed(() => {
  return list.value.reduce((sum, item) => sum + item.currentQuantity, 0)
})

// 状态相关函数
function statusType(status) {
  return status === 1 ? 'success' : 'info'
}

function statusText(status) {
  return status === 1 ? '饲养中' : '已结束'
}

// 阶段相关函数
function stageLabel(stage) {
  const labels = { 1: '苗种期', 2: '生长期', 3: '育肥期' }
  return labels[stage] || '未知'
}

function stageClass(stage) {
  return {
    1: 'seedling',
    2: 'growing',
    3: 'fattening'
  }[stage] || ''
}

const headerStyle = {
  background: 'rgba(93, 140, 93, 0.04)',
  fontWeight: 600,
  color: '#5A6B5A',
  fontSize: '0.85rem',
  textTransform: 'uppercase',
  letterSpacing: '0.5px'
}

async function loadList() {
  loading.value = true
  try {
    list.value = await getBatchList(filters.value)
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = { categoryId: null, status: null, keyword: '' }
  loadList()
}

function handleAdd() {
  router.push('/batch/add')
}

function handleEdit(row) {
  router.push({ path: '/batch/add', query: { id: row.id } })
}

async function confirmDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该批次吗？删除后将无法恢复', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    await deleteBatch(id)
    ElMessage.success('删除成功')
    await loadList()
  } catch {
    // cancelled
  }
}

onMounted(async () => {
  categoryTree.value = await getCategoryTree()
  await loadList()
})
</script>

<style scoped>
.page-container {
  animation: pageEnter 0.5s ease-out;
}

@keyframes pageEnter {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 4px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(93, 140, 93, 0.15), rgba(93, 140, 93, 0.05));
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
}

.page-title {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.3;
}

.page-subtitle {
  font-size: 0.9rem;
  color: var(--text-tertiary);
  margin: 4px 0 0;
}

.add-btn {
  padding: 0 24px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 统计行 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  backdrop-filter: blur(12px);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--glass-shadow-hover);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.bg-primary {
  background: linear-gradient(135deg, rgba(93, 140, 93, 0.15), rgba(93, 140, 93, 0.05));
  color: var(--primary);
}

.bg-success {
  background: linear-gradient(135deg, rgba(107, 158, 107, 0.15), rgba(107, 158, 107, 0.05));
  color: var(--success);
}

.bg-warning {
  background: linear-gradient(135deg, rgba(212, 165, 116, 0.15), rgba(212, 165, 116, 0.05));
  color: var(--warning);
}

.stat-content {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.stat-number {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.stat-label {
  font-size: 0.85rem;
  color: var(--text-tertiary);
  margin-top: 4px;
}

.stat-trend {
  padding: 6px 10px;
  border-radius: 8px;
  background: rgba(93, 140, 93, 0.1);
  color: var(--primary);
}

.stat-trend.up {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

/* 数据卡片 */
.data-card {
  border-radius: 20px;
  overflow: hidden;
}

/* 筛选栏 */
.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px 24px;
  background: rgba(93, 140, 93, 0.02);
  border-radius: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 16px;
}

.filter-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-secondary);
}

.filter-select,
.filter-input {
  width: 180px;
}

.select-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.option-dot.success { background: var(--success); }
.option-dot.info { background: var(--info); }

.filter-actions {
  display: flex;
  gap: 10px;
}

.search-btn, .reset-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 18px;
  height: 40px;
  border-radius: 10px;
}

/* 表格样式 */
.data-table {
  border-radius: 12px;
  overflow: hidden;
}

.data-table :deep(.el-table__header th) {
  padding: 14px 12px;
}

.data-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.data-table :deep(.el-table__row:hover) {
  background: rgba(93, 140, 93, 0.02) !important;
}

/* 序号 */
.index-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: rgba(139, 139, 122, 0.08);
  font-family: 'Space Grotesk', sans-serif;
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 0.85rem;
}

/* 批次单元格 */
.batch-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.batch-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(122, 154, 168, 0.15), rgba(122, 154, 168, 0.05));
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--info);
  flex-shrink: 0;
}

.batch-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.batch-no {
  font-family: 'Space Grotesk', sans-serif;
  font-weight: 600;
  color: var(--text-primary);
}

.status-tag {
  width: fit-content;
  border-radius: 6px;
}

/* 种类标签 */
.category-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 8px;
  background: rgba(93, 140, 93, 0.1);
  color: var(--primary);
  font-weight: 500;
  font-size: 0.85rem;
}

/* 阶段标签 */
.stage-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 500;
}

.stage-tag.seedling {
  background: rgba(122, 154, 168, 0.12);
  color: var(--info);
}

.stage-tag.growing {
  background: rgba(212, 165, 116, 0.12);
  color: var(--warning);
}

.stage-tag.fattening {
  background: rgba(93, 140, 93, 0.12);
  color: var(--primary);
}

/* 数量单元格 */
.quantity-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.quantity-main {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: 'Space Grotesk', sans-serif;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--success);
}

.quantity-main.low-stock {
  color: var(--danger);
}

.quantity-main .el-icon {
  font-size: 1rem;
}

.quantity-sub {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

/* 日期单元格 */
.date-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: var(--text-secondary);
}

.date-cell .el-icon {
  color: var(--primary);
}

/* 场地单元格 */
.site-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.site-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(212, 165, 116, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--warning);
  font-size: 0.9rem;
}

/* 负责人单元格 */
.person-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.person-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.8rem;
  font-weight: 600;
}

.empty-text {
  color: var(--text-muted);
  font-size: 0.85rem;
}

/* 操作按钮组 */
.action-group {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.action-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 10px;
  background: rgba(93, 140, 93, 0.08);
  color: var(--primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: rgba(93, 140, 93, 0.15);
  transform: scale(1.1);
}

.action-btn.view {
  background: rgba(122, 154, 168, 0.08);
  color: var(--info);
}

.action-btn.view:hover {
  background: rgba(122, 154, 168, 0.15);
}

.action-btn.danger {
  background: rgba(201, 123, 123, 0.08);
  color: var(--danger);
}

.action-btn.danger:hover {
  background: rgba(201, 123, 123, 0.15);
}

/* 空状态 */
.empty-custom {
  padding: 40px;
  opacity: 0.5;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .stats-row {
    grid-template-columns: 1fr;
  }

  .filter-section {
    flex-direction: column;
  }

  .filter-select,
  .filter-input {
    width: 100%;
  }
}
</style>
