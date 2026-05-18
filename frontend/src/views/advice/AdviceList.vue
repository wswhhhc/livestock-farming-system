<template>
  <div class="page-container">
    <PageHeader icon="ChatDotSquare" title="养殖建议" subtitle="AI 智能分析养殖数据，为您提供专业的养殖管理建议">
      <el-button type="primary" @click="handleGenerate" :loading="generating">
        <el-icon><Refresh /></el-icon>
        生成最新建议
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
              <el-tag
                :type="triggerTagType(item.triggerType)"
                size="small"
                effect="dark"
                :class="['trigger-tag', triggerClass(item.triggerType)]"
              >
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search, Delete, ChatDotSquare } from '@element-plus/icons-vue'
import PageHeader from '../../components/PageHeader.vue'
import { getAdviceList, generateAdvice, markAdviceRead, deleteAdvice } from '../../api/advice'
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
  const map = { 1: '出栏提醒', 2: '存栏预警', 3: '成本异常', 4: '价格利好', 5: 'AI建议' }
  return map[type] || '系统建议'
}

function triggerTagType(type) {
  const map = { 1: 'warning', 2: 'danger', 3: 'danger', 4: 'success', 5: 'primary' }
  return map[type] || 'info'
}

function triggerClass(type) {
  const map = {
    1: 'trigger-tag-warning',
    2: 'trigger-tag-danger',
    3: 'trigger-tag-danger',
    4: 'trigger-tag-success',
    5: 'trigger-tag-primary'
  }
  return map[type] || 'trigger-tag-info'
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
.trigger-tag {
  border: none !important;
  color: #fff !important;
  font-weight: 600;
  letter-spacing: 0.04em;
  box-shadow: 0 8px 18px rgba(61, 90, 61, 0.12);
}
.trigger-tag.trigger-tag-primary {
  background: linear-gradient(135deg, var(--primary-dark) 0%, var(--primary) 55%, var(--primary-accent) 100%) !important;
  box-shadow: 0 10px 22px rgba(93, 140, 93, 0.24);
}
.trigger-tag.trigger-tag-success {
  background: linear-gradient(135deg, #5f8f5f 0%, var(--success) 100%) !important;
}
.trigger-tag.trigger-tag-warning {
  background: linear-gradient(135deg, #b8895d 0%, var(--warning) 100%) !important;
}
.trigger-tag.trigger-tag-danger {
  background: linear-gradient(135deg, #b86c6c 0%, var(--danger) 100%) !important;
}
.trigger-tag.trigger-tag-info {
  background: linear-gradient(135deg, #688795 0%, var(--info) 100%) !important;
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
</style>
