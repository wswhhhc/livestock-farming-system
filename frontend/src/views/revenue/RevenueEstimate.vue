<template>
  <div class="page-container">
    <PageHeader icon="TrendCharts" title="收益预估" subtitle="计算与分析养殖收益，辅助经营决策">
      <el-button type="primary" size="large" class="add-btn" @click="loadData" :loading="loading">
        <el-icon :size="18"><DataAnalysis /></el-icon>
        <span>计算预估</span>
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
        <el-form-item>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card v-if="summary" class="data-card summary-card" style="margin-top: 16px">
      <template #header>
        <div class="card-header-inner">
          <el-icon :size="20"><DataBoard /></el-icon>
          <span>收益汇总</span>
        </div>
      </template>
      <div class="stat-grid">
        <div class="stat-item">
          <div class="stat-label">批次数量</div>
          <div class="stat-value">{{ summary.batchCount }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">总存栏</div>
          <div class="stat-value">{{ summary.totalQuantity }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">预计产肉量</div>
          <div class="stat-value highlight-weight">{{ summary.totalEstimatedWeight }} <small>kg</small></div>
        </div>
        <div class="stat-item">
          <div class="stat-label">预计总收入</div>
          <div class="stat-value highlight-revenue">¥{{ formatMoney(summary.totalEstimatedRevenue) }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">预估总成本</div>
          <div class="stat-value highlight-cost">¥{{ formatMoney(summary.totalCost) }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">今日成本</div>
          <div class="stat-value" style="color:#e6a23c">¥{{ formatMoney(summary.todayCost) }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">历史总成本</div>
          <div class="stat-value" style="color:#909399">¥{{ formatMoney(summary.historicalTotalCost) }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">预计总利润</div>
          <div class="stat-value" :class="summary.totalEstimatedProfit >= 0 ? 'highlight-profit' : 'highlight-loss'">
            ¥{{ formatMoney(summary.totalEstimatedProfit) }}
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-label">利润率</div>
          <div class="stat-value" :class="summary.overallProfitMargin !== '-' && parseFloat(summary.overallProfitMargin) >= 0 ? 'highlight-profit' : 'highlight-loss'">
            {{ summary.overallProfitMargin }}
          </div>
        </div>
      </div>
    </el-card>

    <el-card class="data-card" style="margin-top: 16px">
      <template #header>
        <div class="card-header-inner">
          <el-icon :size="20"><List /></el-icon>
          <span>批次收益明细</span>
          <el-tag v-if="list.length > 0" type="info" effect="plain" size="small" style="margin-left: 8px">{{ list.length }} 个批次</el-tag>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" class="data-table">
        <el-table-column prop="batchNo" label="批次编号" width="160" />
        <el-table-column prop="categoryName" label="种类" width="80" />
        <el-table-column prop="currentStage" label="阶段" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="stageTagType(row.currentStage)" size="small" effect="dark">
              {{ stageLabel(row.currentStage) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentQuantity" label="存栏" width="60" align="center" />
        <el-table-column label="基准价" width="80" align="right">
          <template #default="{ row }">¥{{ row.basePrice }}</template>
        </el-table-column>
        <el-table-column label="调整价" width="80" align="right">
          <template #default="{ row }">¥{{ row.adjustedPrice }}</template>
        </el-table-column>
        <el-table-column label="价格调整" min-width="180">
          <template #default="{ row }">
            <span v-if="row.applicableRuleCount === 0" class="text-muted">基准价</span>
            <el-tooltip v-else :content="row.priceAdjustDesc">
              <el-tag size="small" :type="row.adjustedPrice >= row.basePrice ? 'danger' : 'success'" effect="dark">
                {{ row.adjustedPrice >= row.basePrice ? '+' : '' }}{{ ((row.adjustedPrice - row.basePrice) / row.basePrice * 100).toFixed(1) }}%
                <el-icon style="margin-left:2px"><QuestionFilled /></el-icon>
              </el-tag>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="estimatedWeight" label="预计产肉量" width="120" align="right">
          <template #default="{ row }">{{ row.estimatedWeight }} kg</template>
        </el-table-column>
        <el-table-column prop="estimatedRevenue" label="预计收入" width="120" align="right">
          <template #default="{ row }"><span class="revenue-value">¥{{ formatMoney(row.estimatedRevenue) }}</span></template>
        </el-table-column>
        <el-table-column prop="totalCost" label="预估成本" width="110" align="right">
          <template #default="{ row }">
            <el-tooltip placement="left" effect="dark">
              <template #content>
                <div style="font-size:12px; line-height:1.8">
                  <div>饲料费: ¥{{ formatMoney(row.projectedFeedCost) }}</div>
                  <div>人工/水电/设备: ¥{{ formatMoney(row.projectedLaborWaterEquip) }}</div>
                  <div>医疗/其他: ¥{{ formatMoney(row.medicalOtherCost) }}</div>
                </div>
              </template>
              <span class="cost-value">¥{{ formatMoney(row.totalCost) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="estimatedProfit" label="预计利润" width="120" align="right">
          <template #default="{ row }">
            <span :class="row.estimatedProfit >= 0 ? 'text-profit' : 'text-loss'">
              ¥{{ formatMoney(row.estimatedProfit) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="profitMargin" label="利润率" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.profitMargin !== '-' && parseFloat(row.profitMargin) >= 0 ? 'success' : 'danger'" size="small" effect="plain">
              {{ row.profitMargin }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="list.length === 0 && !loading" description="暂无饲养中的批次" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { DataAnalysis, DataBoard, List, TrendCharts } from '@element-plus/icons-vue'
import PageHeader from '../../components/PageHeader.vue'
import { getRevenueEstimate } from '../../api/revenue'
import { getCategoryTree } from '../../api/category'
import { stageLabel, stageTagType } from '../../composables/useStage'

const loading = ref(false)
const list = ref([])
const summary = ref(null)
const categoryTree = ref([])

const filters = ref({
  categoryId: null
})

function formatMoney(val) {
  if (val == null) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function loadData() {
  loading.value = true
  try {
    const params = {}
    if (filters.value.categoryId) params.categoryId = filters.value.categoryId
    const result = await getRevenueEstimate(params)
    list.value = result.list || []
    summary.value = result.summary || null
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = { categoryId: null }
  loadData()
}

onMounted(async () => {
  categoryTree.value = await getCategoryTree()
  await loadData()
})
</script>

<style scoped>
.card-header-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.card-header-inner .el-icon {
  color: var(--primary);
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 20px;
}
.stat-item { text-align: center; }
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}
.stat-value small { font-size: 13px; font-weight: 400; color: var(--text-secondary); }
.highlight-weight { color: var(--primary); }
.highlight-revenue { color: var(--primary); }
.highlight-cost { color: var(--warning); }
.highlight-profit { color: var(--success); }
.highlight-loss { color: var(--danger); }

.revenue-value { font-weight: 600; color: #66b1ff; }
.cost-value { font-weight: 600; color: var(--warning); }
.text-profit { color: #85ce61; font-weight: 700; }
.text-loss { color: #f89898; font-weight: 700; }
.text-muted { color: var(--text-placeholder); }
</style>
