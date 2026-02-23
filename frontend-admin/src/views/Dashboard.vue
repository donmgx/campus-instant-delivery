<template>
  <div class="dashboard-container" v-loading="loading">
    <h2 class="section-title">今日核心指标</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="data-card turnover-card">
          <div class="card-header">营业额 (元)</div>
          <div class="card-value">¥ {{ businessData.turnover || '0.00' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">有效订单数</div>
          <div class="card-value">{{ businessData.validOrderCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">订单完成率</div>
          <div class="card-value">{{ (businessData.orderCompletionRate * 100).toFixed(1) || 0 }}%</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">新增用户</div>
          <div class="card-value">{{ businessData.newUsers || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">平均客单价</div>
          <div class="card-value">¥ {{ businessData.unitPrice || '0.00' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card shadow="hover" class="overview-card">
          <template #header>
            <div class="card-title">订单概览</div>
          </template>
          <div class="overview-grid">
            <div class="overview-item warning">
              <div class="num">{{ orderData.waitingOrders || 0 }}</div>
              <div class="label">待接单</div>
            </div>
            <div class="overview-item primary">
              <div class="num">{{ orderData.deliveredOrders || 0 }}</div>
              <div class="label">派送中</div>
            </div>
            <div class="overview-item success">
              <div class="num">{{ orderData.completedOrders || 0 }}</div>
              <div class="label">已完成</div>
            </div>
            <div class="overview-item danger">
              <div class="num">{{ orderData.cancelledOrders || 0 }}</div>
              <div class="label">已取消</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="overview-card">
          <template #header>
            <div class="card-title">菜品总览</div>
          </template>
          <div class="overview-grid half">
            <div class="overview-item success">
              <div class="num">{{ dishData.sold || 0 }}</div>
              <div class="label">起售中</div>
            </div>
            <div class="overview-item danger">
              <div class="num">{{ dishData.discontinued || 0 }}</div>
              <div class="label">已停售</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="overview-card">
          <template #header>
            <div class="card-title">套餐总览</div>
          </template>
          <div class="overview-grid half">
            <div class="overview-item success">
              <div class="num">{{ setmealData.sold || 0 }}</div>
              <div class="label">起售中</div>
            </div>
            <div class="overview-item danger">
              <div class="num">{{ setmealData.discontinued || 0 }}</div>
              <div class="label">已停售</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getBusinessDataAPI, getOverviewOrdersAPI, getOverviewDishesAPI, getOverviewSetmealsAPI } from '../api/workspace'
import { ElMessage } from 'element-plus'

const loading = ref(true)

// 数据容器
const businessData = ref({})
const orderData = ref({})
const dishData = ref({})
const setmealData = ref({})

// 获取所有看板数据
const fetchAllData = async () => {
  loading.value = true
  try {
    // 使用 Promise.all 并发请求，极大地提升加载速度
    const [bizRes, orderRes, dishRes, setmealRes] = await Promise.all([
      getBusinessDataAPI(),
      getOverviewOrdersAPI(),
      getOverviewDishesAPI(),
      getOverviewSetmealsAPI()
    ])
    
    businessData.value = bizRes
    orderData.value = orderRes
    dishData.value = dishRes
    setmealData.value = setmealRes
  } catch (error) {
    console.error('获取看板数据失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAllData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 10px;
}
.section-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  font-weight: 600;
  border-left: 4px solid var(--el-color-primary);
  padding-left: 10px;
}
.mt-20 {
  margin-top: 20px;
}

/* 核心指标卡片样式 */
.data-card {
  height: 120px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  text-align: center;
  background: #fff;
  border: none;
}
.turnover-card {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%);
  color: #fff;
}
.turnover-card .card-header, .turnover-card .card-value {
  color: #fff !important;
}
.card-header {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}
.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

/* 概览卡片样式 */
.overview-card {
  height: 200px;
  border-radius: 8px;
  border: none;
}
.card-title {
  font-weight: bold;
  font-size: 16px;
}
.overview-grid {
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 100px;
}
.overview-grid.half {
  justify-content: space-evenly;
}
.overview-item {
  text-align: center;
}
.overview-item .num {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}
.overview-item .label {
  font-size: 13px;
  color: #606266;
}

/* 状态颜色区分 */
.warning .num { color: #e6a23c; }
.primary .num { color: #409eff; }
.success .num { color: #67c23a; }
.danger .num { color: #f56c6c; }
</style>