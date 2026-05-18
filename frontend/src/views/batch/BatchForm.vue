<template>
  <el-card class="page-card">
    <template #header>
      <div class="card-header">
        <span class="card-title">{{ isEdit ? '编辑批次' : '登记存栏' }}</span>
        <el-button @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>
    </template>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="batch-form">
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="批次编号" prop="batchNo">
            <el-input v-model="form.batchNo" :disabled="isEdit" placeholder="选种类后自动生成" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="家畜种类" prop="categoryId">
            <el-tree-select
              v-model="form.categoryId"
              :data="categoryTree"
              :props="{ label: 'categoryName', value: 'id' }"
              placeholder="请选择种类"
              check-strictly
              @change="onCategoryChange"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="入场数量" prop="initialQuantity">
            <el-input-number v-model="form.initialQuantity" :min="1" style="width: 100%" controls-position="right" />
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="isEdit">
          <el-form-item label="当前存栏" prop="currentQuantity">
            <el-input-number v-model="form.currentQuantity" :min="0" style="width: 100%" controls-position="right" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="入场日期" prop="entryDate">
            <el-date-picker v-model="form.entryDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" placeholder="选择日期" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预计出栏日" prop="expectedSlaughterDate">
            <el-date-picker v-model="form.expectedSlaughterDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" placeholder="自动计算" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="养殖场地" prop="siteId">
            <el-select v-model="form.siteId" placeholder="请选择" clearable>
              <el-option v-for="s in sites" :key="s.id" :label="s.siteName" :value="s.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="生长阶段" prop="currentStage">
            <el-select v-model="form.currentStage" placeholder="请选择">
              <el-option label="苗种" :value="1" />
              <el-option label="青年" :value="2" />
              <el-option label="成年" :value="3" />
              <el-option label="出栏前" :value="4" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="负责人" prop="responsiblePerson">
        <el-input v-model="form.responsiblePerson" maxlength="50" placeholder="可选" style="max-width: 360px" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" placeholder="可选填写备注信息" style="max-width: 560px" />
      </el-form-item>
      <el-divider />
      <el-form-item>
        <el-button type="primary" @click="handleSave" :loading="saving" size="large">
          <el-icon><Check /></el-icon>
          {{ isEdit ? '保存修改' : '确认登记' }}
        </el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getBatchById, createBatch, updateBatch, getNextBatchNo } from '../../api/batch'
import { getCategoryTree } from '../../api/category'
import { getSiteList } from '../../api/site'

const route = useRoute()
const router = useRouter()
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const categoryTree = ref([])
const sites = ref([])

const defaultForm = {
  batchNo: '',
  categoryId: null,
  currentStage: 1,
  initialQuantity: null,
  currentQuantity: null,
  entryDate: '',
  expectedSlaughterDate: '',
  siteId: null,
  responsiblePerson: '',
  status: 1,
  remark: ''
}

const form = ref({ ...defaultForm })

const rules = {
  categoryId: [{ required: true, message: '请选择家畜种类', trigger: 'change' }],
  initialQuantity: [{ required: true, message: '请输入入场数量', trigger: 'blur' }],
  entryDate: [{ required: true, message: '请选择入场日期', trigger: 'change' }],
  currentStage: [{ required: true, message: '请选择生长阶段', trigger: 'change' }]
}

async function onCategoryChange(categoryId) {
  if (!categoryId || isEdit.value) return
  try {
    form.value.batchNo = await getNextBatchNo(categoryId)
  } catch {
    // error handled by interceptor
  }
}

function cleanForm(data) {
  const cleaned = { ...data }
  for (const key of ['expectedSlaughterDate']) {
    if (cleaned[key] === '') cleaned[key] = null
  }
  if (!cleaned.id) delete cleaned.currentQuantity
  return cleaned
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const data = cleanForm(form.value)
    if (isEdit.value) {
      await updateBatch(route.query.id, data)
      ElMessage.success('修改成功')
    } else {
      await createBatch(data)
      ElMessage.success('登记成功')
    }
    router.push('/batch')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  categoryTree.value = await getCategoryTree()
  sites.value = await getSiteList()

  if (route.query.id) {
    isEdit.value = true
    const data = await getBatchById(route.query.id)
    form.value = { ...defaultForm, ...data }
  } else {
    form.value.entryDate = new Date().toISOString().slice(0, 10)
  }
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.batch-form {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;
}
</style>
