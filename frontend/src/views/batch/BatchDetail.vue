<template>
  <div>
    <el-card class="page-card detail-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Document /></el-icon>
            批次详情
          </span>
          <el-tooltip content="返回上一页" placement="bottom" :show-after="300">
            <el-button @click="$router.back()">
              <el-icon><ArrowLeft /></el-icon>
              返回列表
            </el-button>
          </el-tooltip>
        </div>
      </template>

      <el-descriptions :column="3" border v-loading="loading" class="enhanced-descriptions">
        <el-descriptions-item label="批次编号">
          <el-tag type="" size="small">{{ batch.batchNo }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="家畜种类">{{ batch.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="生长阶段">
          <el-tag :type="stageTagType(batch.currentStage)" effect="dark" size="small">{{ stageLabel(batch.currentStage) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入场日期">{{ batch.entryDate }}</el-descriptions-item>
        <el-descriptions-item label="入场数量">{{ batch.initialQuantity }}</el-descriptions-item>
        <el-descriptions-item label="当前存栏">
          <span :class="{ 'text-danger': batch.currentQuantity < batch.initialQuantity * 0.5, 'text-success': batch.currentQuantity >= batch.initialQuantity * 0.5 }">
            {{ batch.currentQuantity }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="预计出栏日">{{ batch.expectedSlaughterDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="养殖场地">{{ batch.siteName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ batch.responsiblePerson || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="batch.status === 1 ? 'success' : 'info'" effect="plain" size="small">
            {{ batch.status === 1 ? '饲养中' : '已结束' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ batch.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="page-card" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Histogram /></el-icon>
            存栏变动记录
          </span>
          <el-button type="primary" size="small" @click="showChangeDialog = true">
            <el-icon><Plus /></el-icon>
            登记变动
          </el-button>
        </div>
      </template>

      <el-table :data="changes" border stripe v-loading="changesLoading" class="enhanced-table">
        <el-table-column prop="changeDate" label="日期" width="120" align="center" />
        <el-table-column prop="changeType" label="变动类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="changeTagType(row.changeType)" size="small" effect="dark" class="change-tag">
              {{ changeTypeLabel(row.changeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeQuantity" label="变动数量" width="100" align="center" />
        <el-table-column prop="quantityAfterChange" label="变动后存栏" width="120" align="center" />
        <el-table-column prop="remark" label="备注" min-width="160">
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-popconfirm title="确定删除此变动记录吗？" @confirm="handleDeleteChange(row.id)">
              <template #reference>
                <el-tooltip content="删除此变动记录" placement="top" :show-after="300">
                  <el-button type="danger" link size="small">删除</el-button>
                </el-tooltip>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showChangeDialog" title="登记存栏变动" width="450px" :close-on-click-modal="false" class="enhanced-dialog">
      <el-form ref="changeFormRef" :model="changeForm" :rules="changeRules" label-width="100px">
        <el-form-item label="变动类型" prop="changeType">
          <el-select v-model="changeForm.changeType" placeholder="请选择" style="width: 100%">
            <el-option label="出栏" :value="1" />
            <el-option label="死亡" :value="2" />
            <el-option label="转群" :value="3" />
            <el-option label="补栏" :value="4" />
            <el-option label="转入" :value="5" />
            <el-option label="转出" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="变动数量" prop="changeQuantity">
          <el-input-number v-model="changeForm.changeQuantity" :min="1" style="width: 100%" controls-position="right" />
        </el-form-item>
        <el-form-item label="变动日期" prop="changeDate">
          <el-date-picker v-model="changeForm.changeDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="changeForm.remark" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangeDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveChange" :loading="savingChange">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { getBatchById, getBatchChanges, addBatchChange, deleteBatchChange } from '../../api/batch'
import { getCategoryTree } from '../../api/category'
import { getSiteList } from '../../api/site'
import { stageLabel, stageTagType } from '../../composables/useStage'

const route = useRoute()
const loading = ref(false)
const batch = ref({})
const changes = ref([])
const changesLoading = ref(false)
const showChangeDialog = ref(false)
const savingChange = ref(false)
const changeFormRef = ref(null)

function changeTypeLabel(type) {
  return ['', '出栏', '死亡', '转群', '补栏', '转入', '转出'][type] || ''
}

function changeTagType(type) {
  return ['', 'success', 'danger', 'warning', 'primary', 'primary', 'warning'][type] || ''
}

const changeForm = ref({
  changeType: null,
  changeQuantity: null,
  changeDate: '',
  remark: ''
})

const changeRules = {
  changeType: [{ required: true, message: '请选择变动类型', trigger: 'change' }],
  changeQuantity: [{ required: true, message: '请输入变动数量', trigger: 'blur' }],
  changeDate: [{ required: true, message: '请选择变动日期', trigger: 'change' }]
}

let catTreeCache = null
let siteListCache = null

async function getNameData() {
  if (!catTreeCache) catTreeCache = await getCategoryTree()
  if (!siteListCache) siteListCache = await getSiteList()
  const findName = (tree, id) => {
    for (const c of tree) {
      if (c.id === id) return c.categoryName
      if (c.children) {
        const r = findName(c.children, id)
        if (r) return r
      }
    }
    return null
  }
  return { findName, sites: siteListCache }
}

async function loadDetail() {
  loading.value = true
  try {
    const [data, { findName, sites }] = await Promise.all([getBatchById(route.params.id), getNameData()])
    data.categoryName = findName(catTreeCache, data.categoryId)
    data.siteName = sites.find(s => s.id === data.siteId)?.siteName || ''
    batch.value = data
  } finally {
    loading.value = false
  }
}

async function loadChanges() {
  changesLoading.value = true
  try {
    changes.value = await getBatchChanges(route.params.id)
  } finally {
    changesLoading.value = false
  }
}

async function handleSaveChange() {
  const valid = await changeFormRef.value.validate().catch(() => false)
  if (!valid) return

  savingChange.value = true
  try {
    await addBatchChange(route.params.id, changeForm.value)
    ElMessage.success('登记成功')
    showChangeDialog.value = false
    changeForm.value = { changeType: null, changeQuantity: null, changeDate: '', remark: '' }
    await loadChanges()
    await loadDetail()
  } finally {
    savingChange.value = false
  }
}

async function handleDeleteChange(changeId) {
  try {
    await deleteBatchChange(route.params.id, changeId)
    ElMessage.success('删除成功')
    await loadChanges()
    await loadDetail()
  } catch {
    // error handled by interceptor
  }
}

onMounted(() => {
  loadDetail()
  loadChanges()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.text-danger {
  color: #f89898;
  font-weight: 700;
}
.text-success {
  color: #85ce61;
  font-weight: 600;
}
.change-tag {
  min-width: 48px;
  text-align: center;
}
</style>
