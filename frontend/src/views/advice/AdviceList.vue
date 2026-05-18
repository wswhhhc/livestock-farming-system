<template>
  <div class="page-container">
    <PageHeader icon="ChatDotSquare" title="养殖建议" subtitle="AI 智能分析养殖数据，为您提供专业的养殖管理建议">
      <el-button type="primary" @click="handleGenerate" :loading="generating">
        <el-icon><Refresh /></el-icon>
        生成最新建议
      </el-button>
      <el-button @click="showTemplates = true">
        <el-icon><Setting /></el-icon>
        建议模板管理
      </el-button>
    </PageHeader>

    <div class="filter-section">
      <el-form :model="filters" inline>
        <el-form-item label="种类">
          <el-tree-select
            v-model="filters.categoryId"
            :data="categoryTree"
            :props="{ label: 'categoryName', value: 'id' }"
            placeholder="全部种类"
            clearable
            check-strictly
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.isRead" placeholder="全部" clearable style="width: 120px">
            <el-option label="未读" :value="0" />
            <el-option label="已读" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card class="data-card" shadow="never">
      <template v-if="list.length > 0">
        <div v-for="item in list" :key="item.id" class="advice-card" :class="{ unread: item.isRead === 0 }" @click="handleRead(item)">
          <div class="advice-header">
            <div class="advice-tags">
              <el-tag :type="triggerTagType(item.triggerType)" size="small" effect="dark">
                {{ triggerLabel(item.triggerType) }}
              </el-tag>
              <span class="advice-batch">{{ item.batchNo }} · {{ item.categoryName }}</span>
              <el-tag type="info" size="small" effect="plain" class="stage-tag">{{ stageLabel(item.growthStage) }}</el-tag>
            </div>
            <div class="advice-right">
              <span v-if="item.isRead === 0" class="unread-badge">新</span>
              <span class="advice-time">{{ item.createTime?.slice(0, 16) }}</span>
            </div>
          </div>
          <div class="advice-content">{{ item.adviceContent }}</div>
          <div class="advice-actions">
            <el-tooltip content="删除此建议" placement="top" :show-after="300">
              <el-button type="danger" link size="small" @click.stop="handleDelete(item.id)">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </template>
      <el-empty v-else description="暂无养殖建议，点击上方「生成最新建议」按钮生成" />
    </el-card>

    <el-dialog v-model="showTemplates" title="建议模板管理" width="720px" :close-on-click-modal="false" @opened="loadTemplates" class="enhanced-dialog">
      <div class="template-toolbar">
        <el-button type="primary" size="small" @click="handleAddTpl">
          <el-icon><Plus /></el-icon>
          新增模板
        </el-button>
      </div>
      <el-table :data="templates" border stripe size="small" v-loading="loadingTpl" class="enhanced-table">
        <el-table-column prop="categoryName" label="种类" width="100" />
        <el-table-column label="生长阶段" width="80" align="center">
          <template #default="{ row }">{{ stageLabel(row.growthStage) }}</template>
        </el-table-column>
        <el-table-column prop="adviceType" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="triggerTagType(row.adviceType)" size="small" effect="dark">{{ adviceTypeLabel(row.adviceType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adviceContent" label="建议内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="60" align="center" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-tooltip content="编辑此模板" placement="top" :show-after="300">
              <el-button type="primary" link size="small" @click="handleEditTpl(row)">编辑</el-button>
            </el-tooltip>
            <el-popconfirm title="确定删除该模板吗？" @confirm="handleDeleteTpl(row.id)">
              <template #reference>
                <el-tooltip content="删除此模板" placement="top" :show-after="300">
                  <el-button type="danger" link size="small">删除</el-button>
                </el-tooltip>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="showTemplates = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showTplForm" :title="isEditTpl ? '编辑模板' : '新增模板'" width="520px" @closed="resetTplForm" class="enhanced-dialog">
      <el-form ref="tplFormRef" :model="tplForm" :rules="tplRules" label-width="100px">
        <el-form-item label="种类" prop="categoryId">
          <el-tree-select
            v-model="tplForm.categoryId"
            :data="categoryTree"
            :props="{ label: 'categoryName', value: 'id' }"
            placeholder="请选择"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="生长阶段" prop="growthStage">
              <el-select v-model="tplForm.growthStage" placeholder="请选择" style="width: 100%">
                <el-option label="苗种" :value="1" />
                <el-option label="青年" :value="2" />
                <el-option label="成年" :value="3" />
                <el-option label="出栏前" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="建议类型" prop="adviceType">
              <el-select v-model="tplForm.adviceType" placeholder="请选择" style="width: 100%">
                <el-option label="出栏建议" value="slaughter" />
                <el-option label="补栏建议" value="stock" />
                <el-option label="成本建议" value="cost" />
                <el-option label="价格建议" value="price" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="建议内容" prop="adviceContent">
          <el-input v-model="tplForm.adviceContent" type="textarea" :rows="3" maxlength="500" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="tplForm.sortOrder" :min="0" :max="999" style="width: 100%" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTplForm = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTpl" :loading="savingTpl">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Setting, Search, Plus, Delete, ChatDotSquare } from '@element-plus/icons-vue'
import PageHeader from '../../components/PageHeader.vue'
import {
  getAdviceList, generateAdvice, markAdviceRead, deleteAdvice,
  getAdviceTemplates, createAdviceTemplate, updateAdviceTemplate, deleteAdviceTemplate
} from '../../api/advice'
import { getCategoryTree } from '../../api/category'
import { stageLabel } from '../../composables/useStage'

const loading = ref(false)
const list = ref([])
const categoryTree = ref([])
const generating = ref(false)

const filters = ref({
  categoryId: null,
  isRead: null
})

function triggerLabel(type) {
  const map = { 1: '出栏提醒', 2: '存栏预警', 3: '成本异常', 4: '价格利好', slaughter: '出栏建议', stock: '补栏建议', cost: '成本建议', price: '价格建议' }
  return map[type] || '系统建议'
}

function triggerTagType(type) {
  const map = { 1: 'warning', 2: 'danger', 3: 'danger', 4: 'success', slaughter: 'warning', stock: 'primary', cost: 'danger', price: 'success' }
  return map[type] || 'info'
}

function adviceTypeLabel(type) {
  const map = { slaughter: '出栏建议', stock: '补栏建议', cost: '成本建议', price: '价格建议' }
  return map[type] || type
}

async function loadList() {
  loading.value = true
  try {
    const params = {}
    if (filters.value.categoryId) params.categoryId = filters.value.categoryId
    if (filters.value.isRead != null) params.isRead = filters.value.isRead
    list.value = await getAdviceList(params)
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = { categoryId: null, isRead: null }
  loadList()
}

async function handleGenerate() {
  generating.value = true
  try {
    await generateAdvice()
    ElMessage.success('建议生成完成')
    await loadList()
  } finally {
    generating.value = false
  }
}

async function handleRead(item) {
  if (item.isRead === 0) {
    try {
      await markAdviceRead(item.id)
      item.isRead = 1
    } catch {
      // error handled by interceptor
    }
  }
}

async function handleDelete(id) {
  try {
    await deleteAdvice(id)
    ElMessage.success('已删除')
    await loadList()
  } catch {
    // error handled by interceptor
  }
}

// template management
const showTemplates = ref(false)
const loadingTpl = ref(false)
const templates = ref([])

const showTplForm = ref(false)
const isEditTpl = ref(false)
const savingTpl = ref(false)
const tplFormRef = ref(null)

const defaultTplForm = {
  categoryId: null,
  growthStage: null,
  adviceType: '',
  adviceContent: '',
  sortOrder: 0
}
const tplForm = ref({ ...defaultTplForm })

const tplRules = {
  categoryId: [{ required: true, message: '请选择种类', trigger: 'change' }],
  growthStage: [{ required: true, message: '请选择生长阶段', trigger: 'change' }],
  adviceType: [{ required: true, message: '请选择建议类型', trigger: 'change' }],
  adviceContent: [{ required: true, message: '请输入建议内容', trigger: 'blur' }]
}

async function loadTemplates() {
  loadingTpl.value = true
  try {
    templates.value = await getAdviceTemplates()
  } finally {
    loadingTpl.value = false
  }
}

function handleAddTpl() {
  isEditTpl.value = false
  tplForm.value = { ...defaultTplForm }
  showTplForm.value = true
}

function handleEditTpl(row) {
  isEditTpl.value = true
  tplForm.value = {
    id: row.id,
    categoryId: row.categoryId,
    growthStage: row.growthStage,
    adviceType: row.adviceType,
    adviceContent: row.adviceContent,
    sortOrder: row.sortOrder
  }
  showTplForm.value = true
}

function resetTplForm() {
  tplFormRef.value?.resetFields()
}

function cleanTplForm(data) {
  return {
    categoryId: data.categoryId,
    growthStage: data.growthStage,
    adviceType: data.adviceType,
    adviceContent: data.adviceContent,
    sortOrder: data.sortOrder
  }
}

async function handleSaveTpl() {
  const valid = await tplFormRef.value.validate().catch(() => false)
  if (!valid) return
  savingTpl.value = true
  try {
    const data = cleanTplForm(tplForm.value)
    if (isEditTpl.value) {
      await updateAdviceTemplate(tplForm.value.id, data)
      ElMessage.success('修改成功')
    } else {
      await createAdviceTemplate(data)
      ElMessage.success('新增成功')
    }
    showTplForm.value = false
    await loadTemplates()
  } finally {
    savingTpl.value = false
  }
}

async function handleDeleteTpl(id) {
  await deleteAdviceTemplate(id)
  ElMessage.success('删除成功')
  await loadTemplates()
}

onMounted(async () => {
  categoryTree.value = await getCategoryTree()
  await loadList()
})
</script>

<style scoped>
.advice-card {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 4px;
}
.advice-card:last-child { border-bottom: none; }
.advice-card:hover { background: #f4f8f4; transform: translateX(2px); }
.advice-card.unread {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.08), rgba(245, 108, 108, 0.03));
  border-left: 3px solid var(--danger);
  margin: 2px 0;
}
.advice-card.unread:hover { background: linear-gradient(135deg, rgba(245, 108, 108, 0.12), rgba(245, 108, 108, 0.05)); }

.advice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.advice-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}
.advice-batch { color: var(--text-regular); font-weight: 500; }
.stage-tag { font-size: 11px; }
.advice-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.advice-time { color: var(--text-placeholder); font-size: 12px; }
.advice-content {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.7;
  padding: 4px 0 4px 2px;
}
.advice-actions {
  text-align: right;
  opacity: 0;
  transition: opacity 0.2s;
}
.advice-card:hover .advice-actions { opacity: 1; }

.unread-badge {
  background: linear-gradient(135deg, #f56c6c, #f89898);
  color: #fff;
  font-size: 10px;
  padding: 2px 7px;
  border-radius: 10px;
  font-weight: 700;
  animation: pulse 2s infinite;
}
@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.4); }
  50% { box-shadow: 0 0 0 4px rgba(245, 108, 108, 0); }
}

.template-toolbar {
  margin-bottom: 12px;
}
</style>
