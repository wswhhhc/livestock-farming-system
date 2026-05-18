<template>
  <div class="page-container">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon :size="28"><Setting /></el-icon>
        </div>
        <div class="header-text">
          <h1 class="page-title">系统设置</h1>
          <p class="page-subtitle">管理系统运行参数和全局配置</p>
        </div>
      </div>
    </div>

    <!-- 配置列表 -->
    <el-card class="data-card glass-card" shadow="never">
      <template #header>
        <div class="card-title-row">
          <el-icon :size="20"><Operation /></el-icon>
          <span>配置参数</span>
        </div>
      </template>
      <el-table :data="configs" v-loading="loading" stripe>
        <el-table-column prop="configName" label="配置名称" width="180" />
        <el-table-column label="配置值">
          <template #default="{ row }">
            <template v-if="row.configKey === 'notification_enabled'">
              <el-switch
                :model-value="row.configValue === 'true'"
                @change="val => handleToggle(row, val)"
              />
            </template>
            <template v-else>
              {{ row.configValue || '-' }}
            </template>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="200" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" title="编辑配置" width="420px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="配置名称">
          <el-input :model-value="editForm.configName" disabled />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input v-model="editForm.configValue" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input :model-value="editForm.description" disabled type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfigs, updateConfig } from '../../api/system'

const configs = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)

const editForm = reactive({
  id: null,
  configName: '',
  configValue: '',
  description: ''
})

onMounted(async () => {
  await fetchConfigs()
})

async function fetchConfigs() {
  loading.value = true
  try {
    configs.value = await getConfigs()
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleEdit(row) {
  editForm.id = row.id
  editForm.configName = row.configName
  editForm.configValue = row.configValue
  editForm.description = row.description
  dialogVisible.value = true
}

async function handleSaveConfig() {
  saving.value = true
  try {
    await updateConfig({
      id: editForm.id,
      configValue: editForm.configValue
    })
    ElMessage.success('配置已更新')
    dialogVisible.value = false
    await fetchConfigs()
  } catch {
    // handled by interceptor
  } finally {
    saving.value = false
  }
}

async function handleToggle(row, val) {
  try {
    await updateConfig({
      id: row.id,
      configValue: val ? 'true' : 'false'
    })
    row.configValue = val ? 'true' : 'false'
    ElMessage.success('配置已更新')
  } catch {
    // handled by interceptor
  }
}
</script>

<style scoped>
.page-container {
  padding: 24px;
  max-width: 860px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 52px;
  height: 52px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  backdrop-filter: blur(12px);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.3px;
}

.page-subtitle {
  font-size: 13px;
  color: var(--text-tertiary);
  margin: 0;
}

.data-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--glass-border-strong);
  background: var(--glass-bg-strong);
  backdrop-filter: blur(20px);
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

</style>
