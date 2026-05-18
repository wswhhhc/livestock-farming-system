<template>
  <div class="page-container">
    <PageHeader icon="SetUp" title="价格规则管理" subtitle="配置价格调整规则，支持多条件定价策略">
      <el-button type="primary" size="large" class="add-btn" @click="handleAdd">
        <el-icon :size="18"><Plus /></el-icon>
        <span>新增规则</span>
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
          <el-form-item label="状态">
            <el-select v-model="filters.enabled" placeholder="全部" clearable style="width: 120px">
              <el-option label="已启用" :value="1" />
              <el-option label="已禁用" :value="0" />
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

      <el-table :data="list" v-loading="loading" class="data-table">
        <el-table-column prop="ruleName" label="规则名称" min-width="140" />
        <el-table-column prop="categoryName" label="适用种类" width="120" />
        <el-table-column prop="direction" label="方向" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.direction === 1 ? 'danger' : 'success'" size="small" effect="dark">
              {{ row.direction === 1 ? '涨价' : '降价' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ratio" label="比例" width="100" align="right">
          <template #default="{ row }">
            <span :class="row.direction === 1 ? 'up' : 'down'">
              {{ row.direction === 1 ? '+' : '-' }}{{ row.ratio }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" align="center" />
        <el-table-column prop="applyCondition" label="适用条件" min-width="200">
          <template #default="{ row }">
            <span v-if="row.applyCondition">{{ row.applyCondition }}</span>
            <span v-else class="text-muted">无条件</span>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.enabled === 1"
              @change="val => handleToggle(row, val)"
              active-color="#67c23a"
              inactive-color="#c0c4cc"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-tooltip content="编辑价格规则" placement="top" :show-after="300">
              <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            </el-tooltip>
            <el-popconfirm title="确定删除该规则吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-tooltip content="删除此规则" placement="top" :show-after="300">
                  <el-button type="danger" link size="small">删除</el-button>
                </el-tooltip>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showForm" :title="isEdit ? '编辑规则' : '新增规则'" width="550px" :close-on-click-modal="false" @closed="resetForm" class="enhanced-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" maxlength="50" />
        </el-form-item>
        <el-form-item label="适用种类" prop="categoryId">
          <el-tree-select
            v-model="form.categoryId"
            :data="categoryTree"
            :props="{ label: 'categoryName', value: 'id' }"
            placeholder="请选择种类"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="方向" prop="direction">
              <el-select v-model="form.direction" placeholder="请选择" style="width: 100%">
                <el-option label="涨价" :value="1" />
                <el-option label="降价" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="比例(%)" prop="ratio">
              <el-input-number v-model="form.ratio" :min="0" :max="100" :precision="1" :step="1" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-input-number v-model="form.priority" :min="1" :max="999" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用">
              <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="适用条件" prop="applyCondition">
          <el-input v-model="form.applyCondition" type="textarea" :rows="2" maxlength="200" placeholder="例如：month IN (1,2,3)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForm = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, SetUp } from '@element-plus/icons-vue'
import PageHeader from '../../components/PageHeader.vue'
import { getPriceRuleList, createPriceRule, updatePriceRule, deletePriceRule } from '../../api/priceRule'
import { getCategoryTree } from '../../api/category'

const loading = ref(false)
const list = ref([])
const categoryTree = ref([])
const showForm = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)

const filters = ref({
  categoryId: null,
  enabled: null
})

const defaultForm = {
  ruleName: '',
  categoryId: null,
  direction: 1,
  ratio: null,
  priority: 1,
  enabled: 1,
  applyCondition: ''
}

const form = ref({ ...defaultForm })

const rules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择适用种类', trigger: 'change' }],
  direction: [{ required: true, message: '请选择方向', trigger: 'change' }],
  ratio: [{ required: true, message: '请输入比例', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const params = {}
    if (filters.value.categoryId) params.categoryId = filters.value.categoryId
    if (filters.value.enabled != null) params.enabled = filters.value.enabled
    list.value = await getPriceRuleList(params)
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = { categoryId: null, enabled: null }
  loadList()
}

function handleAdd() {
  isEdit.value = false
  form.value = { ...defaultForm }
  showForm.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.value = { ...row }
  showForm.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

async function handleToggle(row, val) {
  await updatePriceRule(row.id, { enabled: val ? 1 : 0 })
  row.enabled = val ? 1 : 0
  ElMessage.success(val ? '已启用' : '已禁用')
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      await updatePriceRule(form.value.id, form.value)
      ElMessage.success('修改成功')
    } else {
      await createPriceRule(form.value)
      ElMessage.success('新增成功')
    }
    showForm.value = false
    await loadList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  await deletePriceRule(id)
  ElMessage.success('删除成功')
  await loadList()
}

onMounted(async () => {
  categoryTree.value = await getCategoryTree()
  await loadList()
})
</script>

<style scoped>
.up { color: var(--danger); font-weight: 700; }
.down { color: var(--success); font-weight: 700; }
.text-muted { color: var(--text-placeholder); }
</style>
