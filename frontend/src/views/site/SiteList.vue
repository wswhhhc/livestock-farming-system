<template>
  <div class="page-container">
    <PageHeader icon="OfficeBuilding" title="养殖场地管理" subtitle="管理您的养殖场地与容量配置">
      <el-button type="primary" size="large" class="add-btn" @click="handleAdd">
        <el-icon :size="18"><Plus /></el-icon>
        <span>新增场地</span>
      </el-button>
    </PageHeader>

    <el-card class="data-card" shadow="never">
      <el-table :data="list" v-loading="loading" class="data-table">
        <el-table-column prop="siteCode" label="场地编号" width="150">
          <template #default="{ row }">
            <el-tag type="" size="small" effect="plain">{{ row.siteCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="siteName" label="场地名称" min-width="160" />
        <el-table-column prop="location" label="位置" min-width="200">
          <template #default="{ row }">
            <span v-if="row.location"><el-icon><Location /></el-icon> {{ row.location }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容量(头/只)" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.capacity != null" class="capacity-value">{{ row.capacity }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200">
          <template #default="{ row }">
            {{ row.remark || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-tooltip content="编辑场地信息" placement="top" :show-after="300">
              <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            </el-tooltip>
            <el-popconfirm title="确定删除该场地吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-tooltip content="删除该场地" placement="top" :show-after="300">
                  <el-button type="danger" link size="small">删除</el-button>
                </el-tooltip>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="list.length === 0 && !loading" description="暂无养殖场地" />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑场地' : '新增场地'"
      width="520px"
      :close-on-click-modal="false"
      class="enhanced-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="场地编号" prop="siteCode">
          <el-input v-model="form.siteCode" placeholder="如：S001" maxlength="50" />
        </el-form-item>
        <el-form-item label="场地名称" prop="siteName">
          <el-input v-model="form.siteName" placeholder="请输入场地名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="如：3号养殖区" maxlength="200" />
        </el-form-item>
        <el-form-item label="容量(头/只)" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" style="width: 100%" controls-position="right" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" placeholder="可选填写备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Location, Plus, OfficeBuilding } from '@element-plus/icons-vue'
import PageHeader from '../../components/PageHeader.vue'
import { getSiteList, createSite, updateSite, deleteSite } from '../../api/site'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)

const defaultForm = {
  siteCode: '',
  siteName: '',
  location: '',
  capacity: null,
  remark: ''
}

const form = ref({ ...defaultForm })

const rules = {
  siteCode: [{ required: true, message: '请输入场地编号', trigger: 'blur' }],
  siteName: [{ required: true, message: '请输入场地名称', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    list.value = await getSiteList()
  } finally {
    loading.value = false
  }
}

function resetDialog() {
  formRef.value?.clearValidate()
}

function handleAdd() {
  isEdit.value = false
  form.value = { ...defaultForm }
  dialogVisible.value = true
  nextTick(resetDialog)
}

function handleEdit(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
  nextTick(resetDialog)
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEdit.value) {
      await updateSite(form.value.id, form.value)
      ElMessage.success('修改成功')
    } else {
      await createSite(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteSite(id)
    ElMessage.success('删除成功')
    await loadList()
  } catch {
    // error handled by interceptor
  }
}

onMounted(loadList)
</script>

<style scoped>
.capacity-value {
  font-weight: 600;
  color: var(--primary);
}
.text-muted { color: var(--text-placeholder); }
</style>
