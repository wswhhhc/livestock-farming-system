<template>
  <div class="page-container">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon :size="28"><Collection /></el-icon>
        </div>
        <div class="header-text">
          <h1 class="page-title">家畜种类管理</h1>
          <p class="page-subtitle">管理您的养殖品种分类与养殖参数</p>
        </div>
      </div>
      <el-button type="primary" size="large" class="add-btn" @click="handleAdd">
        <el-icon :size="18"><Plus /></el-icon>
        <span>新增种类</span>
      </el-button>
    </div>

    <!-- 主内容卡片 -->
    <el-card class="data-card" shadow="never">
      <!-- 统计概览 -->
      <div class="stats-bar">
        <div class="stat-item">
          <div class="stat-icon primary">
            <el-icon><Collection /></el-icon>
          </div>
          <div class="stat-data">
            <span class="stat-value">{{ totalCount }}</span>
            <span class="stat-label">总种类数</span>
          </div>
        </div>
        <div class="divider"></div>
        <div class="stat-item">
          <div class="stat-icon success">
            <el-icon><FolderOpened /></el-icon>
          </div>
          <div class="stat-data">
            <span class="stat-value">{{ topLevelCount }}</span>
            <span class="stat-label">一级分类</span>
          </div>
        </div>
        <div class="divider"></div>
        <div class="stat-item">
          <div class="stat-icon warning">
            <el-icon><Grid /></el-icon>
          </div>
          <div class="stat-data">
            <span class="stat-value">{{ subCount }}</span>
            <span class="stat-label">子分类</span>
          </div>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="treeData"
        row-key="id"
        :tree-props="{ children: 'children' }"
        default-expand-all
        v-loading="loading"
        class="data-table"
        :header-cell-style="headerStyle"
      >
        <el-table-column prop="categoryName" label="种类名称" min-width="180">
          <template #default="{ row }">
            <div class="category-cell">
              <div class="category-icon" :class="row.parentId ? 'sub' : 'main'">
                <el-icon v-if="row.parentId"><Document /></el-icon>
                <el-icon v-else><Collection /></el-icon>
              </div>
              <div class="category-info">
                <span class="category-name">{{ row.categoryName }}</span>
                <span v-if="row.parentId" class="category-tag">子类</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="growthCycle" label="生长周期" width="110" align="center">
          <template #default="{ row }">
            <span v-if="row.growthCycle" class="cycle-tag">{{ row.growthCycle }}天</span>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="slaughterWeight" label="出栏体重" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.slaughterWeight" class="weight-value">{{ row.slaughterWeight }}kg</span>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="slaughterMonthAge" label="出栏月龄" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.slaughterMonthAge" class="age-value">{{ row.slaughterMonthAge }}月</span>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="breedMode" label="养殖模式" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.breedMode === 2 ? 'primary' : 'success'"
              size="small"
              effect="light"
              class="mode-tag"
              v-if="row.breedMode"
            >
              {{ row.breedMode === 1 ? '散养' : '圈养' }}
            </el-tag>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="feedType" label="饲料类型" min-width="140">
          <template #default="{ row }">
            <div class="feed-info">
              <el-icon v-if="row.feedType"><Bowl /></el-icon>
              <span>{{ feedTypeLabel(row.feedType) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="dailyMgmtFee" label="管理费" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.dailyMgmtFee" class="price-value">
              <small>¥</small>{{ row.dailyMgmtFee }}<small>/天</small>
            </span>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="basePrice" label="基准价" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.basePrice" class="price-value primary">
              <small>¥</small>{{ row.basePrice }}<small>/kg</small>
            </span>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="estimatedSlaughterRate" label="出栏率" width="80" align="center">
          <template #default="{ row }">
            <div v-if="row.estimatedSlaughterRate" class="rate-tag">
              <el-icon><TrendCharts /></el-icon>
              <span>{{ row.estimatedSlaughterRate }}%</span>
            </div>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="sortOrder" label="排序" width="70" align="center">
          <template #default="{ row }">
            <span class="sort-value">{{ row.sortOrder }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <button class="action-btn edit" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                <span>编辑</span>
              </button>
              <button class="action-btn add-child" @click="handleAddChild(row)">
                <el-icon><CirclePlus /></el-icon>
                <span>子类</span>
              </button>
              <button class="action-btn delete" @click="confirmDelete(row.id)">
                <el-icon><Delete /></el-icon>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑种类' : '新增种类'"
      width="700px"
      :close-on-click-modal="false"
      class="form-dialog"
      destroy-on-close
    >
      <div class="form-wrapper">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="130px"
          label-position="right"
          class="category-form"
        >
          <div class="form-section">
            <div class="section-title">
              <el-icon><Document /></el-icon>
              <span>基本信息</span>
            </div>
            <el-form-item label="种类名称" prop="categoryName">
              <el-input
                v-model="form.categoryName"
                placeholder="请输入种类名称"
                maxlength="50"
                class="form-input"
              />
            </el-form-item>
            <el-form-item label="上级种类" prop="parentId">
              <el-tree-select
                v-model="form.parentId"
                :data="treeData"
                :props="{ label: 'categoryName', value: 'id' }"
                placeholder="留空则为顶级种类"
                clearable
                check-strictly
                class="form-select"
              />
            </el-form-item>
          </div>

          <div class="form-section">
            <div class="section-title">
              <el-icon><Clock /></el-icon>
              <span>生长参数</span>
            </div>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="生长周期(天)" prop="growthCycle">
                  <el-input-number
                    v-model="form.growthCycle"
                    :min="0"
                    controls-position="right"
                    class="form-number"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="出栏体重(kg)" prop="slaughterWeight">
                  <el-input-number
                    v-model="form.slaughterWeight"
                    :min="0"
                    :precision="2"
                    controls-position="right"
                    class="form-number"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="出栏月龄" prop="slaughterMonthAge">
                  <el-input-number
                    v-model="form.slaughterMonthAge"
                    :min="0"
                    controls-position="right"
                    class="form-number"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="养殖模式" prop="breedMode">
                  <el-select v-model="form.breedMode" placeholder="请选择" clearable class="form-select">
                    <el-option label="散养" :value="1">
                      <el-icon><Open /></el-icon> 散养
                    </el-option>
                    <el-option label="圈养" :value="2">
                      <el-icon><Folder /></el-icon> 圈养
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <div class="form-section">
            <div class="section-title">
              <el-icon><Bowl /></el-icon>
              <span>饲料配置</span>
            </div>
            <el-form-item label="饲料类型" prop="feedType">
              <el-input
                v-model="form.feedType"
                placeholder="如：玉米-豆粕型"
                maxlength="100"
                class="form-input"
              />
            </el-form-item>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="苗种饲料(kg)" prop="feedConsumptionSeedling">
                  <el-input-number
                    v-model="form.feedConsumptionSeedling"
                    :min="0"
                    :precision="2"
                    controls-position="right"
                    class="form-number compact"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="青年饲料(kg)" prop="feedConsumptionYoung">
                  <el-input-number
                    v-model="form.feedConsumptionYoung"
                    :min="0"
                    :precision="2"
                    controls-position="right"
                    class="form-number compact"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="成年饲料(kg)" prop="feedConsumptionAdult">
                  <el-input-number
                    v-model="form.feedConsumptionAdult"
                    :min="0"
                    :precision="2"
                    controls-position="right"
                    class="form-number compact"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="饲料单价(元/kg)" prop="feedPrice">
                  <el-input-number
                    v-model="form.feedPrice"
                    :min="0"
                    :precision="2"
                    :step="0.5"
                    controls-position="right"
                    class="form-number"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="每日管理费(元/头/天)" prop="dailyMgmtFee">
                  <el-input-number
                    v-model="form.dailyMgmtFee"
                    :min="0"
                    :precision="4"
                    :step="0.01"
                    controls-position="right"
                    class="form-number"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <div class="form-section">
            <div class="section-title">
              <el-icon><FirstAidKit /></el-icon>
              <span>其他信息</span>
            </div>
            <el-form-item label="常见病害" prop="commonDiseases">
              <el-input
                v-model="form.commonDiseases"
                type="textarea"
                :rows="2"
                placeholder="多个用逗号分隔"
                maxlength="500"
                class="form-textarea"
              />
            </el-form-item>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="基准价(元/kg)" prop="basePrice">
                  <el-input-number
                    v-model="form.basePrice"
                    :min="0"
                    :precision="2"
                    controls-position="right"
                    class="form-number"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="预估出栏率(%)" prop="estimatedSlaughterRate">
                  <el-input-number
                    v-model="form.estimatedSlaughterRate"
                    :min="0"
                    :max="100"
                    :precision="2"
                    controls-position="right"
                    class="form-number"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="排序号" prop="sortOrder">
              <el-input-number
                v-model="form.sortOrder"
                :min="0"
                controls-position="right"
                class="form-number"
                style="width: 150px"
              />
            </el-form-item>
          </div>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" size="large">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="saving" size="large" class="save-btn">
            <el-icon><Check /></el-icon>
            <span>保存</span>
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Collection, Document, Edit, CirclePlus, Delete,
  Clock, Bowl, FirstAidKit, Open, Folder, Check,
  FolderOpened, Grid, TrendCharts
} from '@element-plus/icons-vue'
import { getCategoryTree, createCategory, updateCategory, deleteCategory } from '../../api/category'

const loading = ref(false)
const treeData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)

// 统计数据
const totalCount = computed(() => {
  let count = 0
  const traverse = (items) => {
    items.forEach(item => {
      count++
      if (item.children) traverse(item.children)
    })
  }
  traverse(treeData.value)
  return count
})

const topLevelCount = computed(() => treeData.value.length)

const subCount = computed(() => {
  let count = 0
  const traverse = (items) => {
    items.forEach(item => {
      if (item.children) {
        count += item.children.length
        traverse(item.children)
      }
    })
  }
  traverse(treeData.value)
  return count
})

const defaultForm = {
  categoryName: '',
  parentId: null,
  growthCycle: 0,
  slaughterWeight: null,
  slaughterMonthAge: null,
  feedConsumptionSeedling: null,
  feedConsumptionYoung: null,
  feedConsumptionAdult: null,
  feedPrice: null,
  dailyMgmtFee: null,
  feedType: '',
  commonDiseases: '',
  breedMode: null,
  basePrice: null,
  estimatedSlaughterRate: null,
  sortOrder: 0
}

const form = ref({ ...defaultForm })

const rules = {
  categoryName: [{ required: true, message: '请输入种类名称', trigger: 'blur' }]
}

const feedTypeMap = {
  'compound': '配合饲料',
  'concentrate': '浓缩饲料',
  'premix': '预混合饲料',
  'premixed': '预混合饲料',
  'roughage': '粗饲料',
  'silage': '青贮饲料',
  'green_fodder': '青绿饲料',
  'green': '青绿饲料',
  'corn_soybean': '玉米-豆粕型',
  'corn-soybean': '玉米-豆粕型',
  'corn_soybean_meal': '玉米-豆粕型',
  'full_price': '全价料',
  'full_feed': '全价饲料',
  'self_mixed': '自配料',
  'self_mix': '自配料'
}

function feedTypeLabel(val) {
  if (!val) return '-'
  const lower = val.toLowerCase()
  return feedTypeMap[lower] || val
}

const headerStyle = {
  background: 'rgba(93, 140, 93, 0.04)',
  fontWeight: 600,
  color: '#5A6B5A'
}

async function loadTree() {
  loading.value = true
  try {
    const res = await getCategoryTree()
    treeData.value = res
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  form.value = { ...defaultForm }
  dialogVisible.value = true
}

function handleAddChild(row) {
  isEdit.value = false
  form.value = { ...defaultForm, parentId: row.id }
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function confirmDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该种类吗？删除后将无法恢复', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    await deleteCategory(id)
    ElMessage.success('删除成功')
    await loadTree()
  } catch {
    // cancelled
  }
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEdit.value) {
      await updateCategory(form.value.id, form.value)
      ElMessage.success('修改成功')
    } else {
      await createCategory(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadTree()
  } finally {
    saving.value = false
  }
}

onMounted(loadTree)
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

/* 数据卡片 */
.data-card {
  border-radius: 20px;
  overflow: hidden;
}

/* 统计栏 */
.stats-bar {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 20px 24px;
  background: linear-gradient(135deg, rgba(93, 140, 93, 0.04), rgba(212, 165, 116, 0.02));
  border-radius: 16px;
  margin-bottom: 24px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.primary {
  background: rgba(93, 140, 93, 0.12);
  color: var(--primary);
}

.stat-icon.success {
  background: rgba(107, 158, 107, 0.12);
  color: var(--success);
}

.stat-icon.warning {
  background: rgba(212, 165, 116, 0.12);
  color: var(--warning);
}

.stat-data {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.stat-label {
  font-size: 0.8rem;
  color: var(--text-tertiary);
  margin-top: 4px;
}

.divider {
  width: 1px;
  height: 40px;
  background: rgba(139, 139, 122, 0.15);
}

/* 表格样式 */
.data-table {
  border-radius: 12px;
  overflow: hidden;
}

.data-table :deep(.el-table__header th) {
  font-weight: 600;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 14px 12px;
}

.data-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.data-table :deep(.el-table__row:hover) {
  background: rgba(93, 140, 93, 0.02) !important;
}

/* 种类单元格 */
.category-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.category-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.category-icon.main {
  background: linear-gradient(135deg, rgba(93, 140, 93, 0.15), rgba(93, 140, 93, 0.05));
  color: var(--primary);
}

.category-icon.sub {
  background: rgba(139, 139, 122, 0.08);
  color: var(--text-tertiary);
}

.category-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-name {
  font-weight: 600;
  color: var(--text-primary);
}

.category-tag {
  font-size: 0.7rem;
  padding: 2px 8px;
  border-radius: 100px;
  background: rgba(139, 139, 122, 0.1);
  color: var(--text-tertiary);
}

/* 数据标签 */
.cycle-tag {
  padding: 4px 10px;
  border-radius: 100px;
  background: rgba(122, 154, 168, 0.1);
  color: var(--info);
  font-size: 0.85rem;
  font-weight: 500;
}

.weight-value, .age-value {
  font-family: 'Space Grotesk', sans-serif;
  font-weight: 600;
  color: var(--text-secondary);
}

.mode-tag {
  border-radius: 6px;
}

.feed-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
}

.feed-info .el-icon {
  color: var(--warning);
}

.price-value {
  font-family: 'Space Grotesk', sans-serif;
  font-weight: 600;
  color: var(--warning);
}

.price-value.primary {
  color: var(--primary);
}

.price-value small {
  font-size: 0.75rem;
  opacity: 0.7;
  margin: 0 1px;
}

.rate-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 100px;
  background: rgba(93, 140, 93, 0.1);
  color: var(--primary);
  font-size: 0.85rem;
  font-weight: 500;
}

.sort-value {
  font-family: 'Space Grotesk', sans-serif;
  font-weight: 600;
  color: var(--text-secondary);
}

.empty-text {
  color: var(--text-muted);
}

/* 操作按钮组 */
.action-group {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: none;
  border-radius: 8px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s ease;
  background: transparent;
}

.action-btn.edit {
  color: var(--primary);
  background: rgba(93, 140, 93, 0.08);
}

.action-btn.edit:hover {
  background: rgba(93, 140, 93, 0.15);
}

.action-btn.add-child {
  color: var(--info);
  background: rgba(122, 154, 168, 0.08);
}

.action-btn.add-child:hover {
  background: rgba(122, 154, 168, 0.15);
}

.action-btn.delete {
  color: var(--danger);
  background: rgba(201, 123, 123, 0.08);
  padding: 6px 10px;
}

.action-btn.delete:hover {
  background: rgba(201, 123, 123, 0.15);
}

/* 表单对话框 */
.form-dialog :deep(.el-dialog__header) {
  padding: 24px 28px 0;
  margin: 0;
}

.form-dialog :deep(.el-dialog__title) {
  font-family: 'Space Grotesk', sans-serif;
  font-weight: 700;
  font-size: 1.25rem;
  color: var(--text-primary);
}

.form-dialog :deep(.el-dialog__body) {
  padding: 20px 28px;
}

.form-dialog :deep(.el-dialog__footer) {
  padding: 0 28px 24px;
}

.form-wrapper {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

.form-wrapper::-webkit-scrollbar {
  width: 6px;
}

.form-wrapper::-webkit-scrollbar-thumb {
  background: rgba(139, 139, 122, 0.2);
  border-radius: 3px;
}

.form-section {
  margin-bottom: 24px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(139, 139, 122, 0.1);
}

.section-title .el-icon {
  color: var(--primary);
}

.category-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.category-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-secondary);
}

.form-input :deep(.el-input__wrapper),
.form-select :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 14px;
}

.form-number :deep(.el-input__wrapper) {
  padding-left: 14px !important;
}

.form-number.compact :deep(.el-input-number__decrease),
.form-number.compact :deep(.el-input-number__increase) {
  width: 28px;
}

.form-textarea :deep(.el-textarea__inner) {
  border-radius: 10px;
  padding: 10px 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.save-btn {
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 6px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .stats-bar {
    flex-wrap: wrap;
    gap: 16px;
  }

  .divider {
    display: none;
  }
}
</style>
