<template>
  <div class="page-container">
    <PageHeader icon="Wallet" title="成本管理" subtitle="记录与分析养殖成本，支持自动计算饲料费与管理费">
      <el-button plain @click="handleAutoFeed" :loading="feedLoading" :disabled="calculating">
        <el-icon><Refresh /></el-icon>
        自动计算饲料费
      </el-button>
      <el-button plain @click="handleAutoMgmt" :loading="mgmtLoading" :disabled="calculating">
        <el-icon><Refresh /></el-icon>
        自动计算管理费
      </el-button>
      <el-button @click="handleBatchDelete" :loading="batchDeleting" :disabled="selectedIds.length === 0" type="danger">
        <el-icon><Delete /></el-icon>
        批量删除({{ selectedIds.length }})
      </el-button>
      <el-button type="primary" size="large" class="add-btn" @click="showAdd = true">
        <el-icon :size="18"><Plus /></el-icon>
        <span>录入成本</span>
      </el-button>
    </PageHeader>

    <el-card class="data-card" shadow="never">

      <div class="filter-bar">
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
          <el-form-item label="成本类型">
            <el-select v-model="filters.costType" placeholder="全部" clearable style="width: 120px">
              <el-option v-for="t in costTypes" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="filters.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 240px"
            />
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

      <div v-loading="calculating" :element-loading-text="calcText" element-loading-background="rgba(255,255,255,0.7)">
      <el-table :data="list" v-loading="loading" class="data-table" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="44" align="center" />
        <el-table-column prop="batchNo" label="单号" width="170">
          <template #default="{ row }">
            <span v-if="row.batchNo" class="batch-no">{{ row.batchNo }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="costDate" label="日期" width="110" align="center" />
        <el-table-column prop="categoryName" label="种类" width="110" />
        <el-table-column prop="batchNum" label="关联批次" width="150">
          <template #default="{ row }">
            <span v-if="row.batchNum">{{ row.batchNum }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="costType" label="成本类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="costTagType(row.costType)" size="small" effect="dark">
              {{ costTypeLabel(row.costType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="140" align="right">
          <template #default="{ row }">
            <span class="amount-value">¥{{ row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160">
          <template #default="{ row }">
            {{ row.remark || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确定删除该记录吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-tooltip content="删除此成本记录" placement="top" :show-after="300">
                  <el-button type="danger" link size="small">删除</el-button>
                </el-tooltip>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="list.length === 0 && !loading && !calculating" description="暂无成本记录" />
      </div>
    </el-card>

    <el-dialog v-model="showAdd" title="录入成本" width="520px" :close-on-click-modal="false" @closed="resetForm" class="enhanced-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属种类" prop="categoryId">
          <el-tree-select
            v-model="form.categoryId"
            :data="categoryTree"
            :props="{ label: 'categoryName', value: 'id' }"
            placeholder="请选择种类"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="关联批次" prop="batchId">
          <el-select v-model="form.batchId" placeholder="可不选" clearable filterable style="width: 100%">
            <el-option v-for="b in batchList" :key="b.id" :label="b.batchNo" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="成本类型" prop="costType">
              <el-select v-model="form.costType" placeholder="请选择" style="width: 100%">
                <el-option v-for="t in addCostTypes" :key="t.value" :label="t.label" :value="t.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="金额" prop="amount">
              <el-input-number v-model="form.amount" :min="0.01" :precision="2" :step="10" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="日期" prop="costDate">
          <el-date-picker v-model="form.costDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Delete, Plus, Wallet } from '@element-plus/icons-vue'
import PageHeader from '../../components/PageHeader.vue'
import { getCostList, createCost, deleteCost, autoFeedCost, autoMgmtCost, batchDeleteCost } from '../../api/cost'
import { getCategoryTree } from '../../api/category'
import { getBatchList } from '../../api/batch'

const loading = ref(false)
const list = ref([])
const categoryTree = ref([])
const batchList = ref([])
const showAdd = ref(false)
const saving = ref(false)
const feedLoading = ref(false)
const mgmtLoading = ref(false)
const calculating = ref(false)
const calcText = ref('')
const formRef = ref(null)

const selectedIds = ref([])
const batchDeleting = ref(false)

// 完整成本类型（用于筛选和列表显示）
const costTypes = [
  { value: 1, label: '饲料费' },
  { value: 2, label: '医药费' },
  { value: 3, label: '人工费(旧)' },
  { value: 4, label: '水电费(旧)' },
  { value: 5, label: '设备费(旧)' },
  { value: 6, label: '其他' },
  { value: 7, label: '日常管理费' }
]

// 录入表单可选类型（不包含自动计算的类型）
const addCostTypes = costTypes.filter(t => t.value === 2 || t.value === 6)

function costTypeLabel(type) {
  return costTypes.find(t => t.value === type)?.label || ''
}

function costTagType(type) {
  const map = { 1: '', 2: 'danger', 3: 'warning', 4: 'info', 5: '', 6: 'info', 7: 'success' }
  return map[type] || ''
}

const filters = ref({
  categoryId: null,
  costType: null,
  dateRange: null
})

const defaultForm = {
  categoryId: null,
  batchId: null,
  costType: null,
  amount: null,
  costDate: '',
  remark: ''
}

const form = ref({ ...defaultForm })

const rules = {
  categoryId: [{ required: true, message: '请选择种类', trigger: 'change' }],
  costType: [{ required: true, message: '请选择成本类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  costDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

async function loadList() {
  loading.value = true
  try {
    const params = {}
    if (filters.value.categoryId) params.categoryId = filters.value.categoryId
    if (filters.value.costType) params.costType = filters.value.costType
    if (filters.value.dateRange) {
      params.startDate = filters.value.dateRange[0]
      params.endDate = filters.value.dateRange[1]
    }
    list.value = await getCostList(params)
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = { categoryId: null, costType: null, dateRange: null }
  loadList()
}

function resetForm() {
  form.value = { ...defaultForm, costDate: new Date().toISOString().slice(0, 10) }
  formRef.value?.resetFields()
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await createCost(form.value)
    ElMessage.success('保存成功')
    showAdd.value = false
    await loadList()
  } finally {
    saving.value = false
  }
}

function onSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

async function handleDelete(id) {
  await deleteCost(id)
  ElMessage.success('删除成功')
  await loadList()
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条记录吗？`, '提示', { type: 'warning' })
    .then(async () => {
      batchDeleting.value = true
      try {
        await batchDeleteCost(selectedIds.value)
        ElMessage.success('删除成功')
        selectedIds.value = []
        await loadList()
      } finally {
        batchDeleting.value = false
      }
    })
    .catch(() => {})
}

async function handleAutoFeed() {
  calculating.value = true
  calcText.value = '正在计算饲料费，请稍候...'
  try {
    const msg = await autoFeedCost()
    ElMessage.success(msg)
    await loadList()
  } finally {
    calculating.value = false
  }
}

async function handleAutoMgmt() {
  calculating.value = true
  calcText.value = '正在计算管理费，请稍候...'
  try {
    const msg = await autoMgmtCost()
    ElMessage.success(msg)
    await loadList()
  } finally {
    calculating.value = false
  }
}

onMounted(async () => {
  categoryTree.value = await getCategoryTree()
  batchList.value = await getBatchList({ status: 1 })
  resetForm()
  await loadList()
})
</script>

<style scoped>
.amount-value {
  font-weight: 600;
  color: var(--warning);
}
.text-muted { color: var(--text-placeholder); }
.batch-no {
  font-family: monospace;
  font-size: 12px;
  color: var(--text-primary);
}
</style>
