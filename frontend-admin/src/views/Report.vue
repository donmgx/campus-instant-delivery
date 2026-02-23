<template>
  <div class="app-container">
    <div class="filter-container">
      <span style="font-size: 14px; color: #606266; margin-right: 10px;">统计区间:</span>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        :clearable="false"
        @change="handleDateChange"
        style="width: 300px; margin-right: 20px;"
      />
      <el-button type="primary" @click="handleDateChange">查询</el-button>
      <el-button type="success" @click="exportReport"><el-icon><Download /></el-icon> 导出 Excel 报表</el-button>
    </div>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="hover">
          <div ref="turnoverChartRef" style="height: 350px; width: 100%;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <div ref="userChartRef" style="height: 350px; width: 100%;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="hover">
          <div ref="orderChartRef" style="height: 350px; width: 100%;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <div ref="top10ChartRef" style="height: 350px; width: 100%;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getTurnoverStatsAPI, getUserStatsAPI, getOrderStatsAPI, getTop10StatsAPI } from '../api/report'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'

// --- 基础状态 ---
const dateRange = ref([])
// 图表 DOM 引用
const turnoverChartRef = ref(null)
const userChartRef = ref(null)
const orderChartRef = ref(null)
const top10ChartRef = ref(null)

// 图表实例引用
let turnoverChart = null
let userChart = null
let orderChart = null
let top10Chart = null

// --- 初始化日期 (默认最近30天) ---
const initDate = () => {
  const end = new Date()
  const start = new Date()
  start.setTime(start.getTime() - 3600 * 1000 * 24 * 30) // 往前推30天
  const formatDate = (date) => {
    const y = date.getFullYear()
    const m = (date.getMonth() + 1).toString().padStart(2, '0')
    const d = date.getDate().toString().padStart(2, '0')
    return `${y}-${m}-${d}`
  }
  dateRange.value = [formatDate(start), formatDate(end)]
}

// --- 数据获取与渲染逻辑 ---
const handleDateChange = () => {
  if (!dateRange.value || dateRange.value.length < 2) return
  const begin = dateRange.value[0]
  const end = dateRange.value[1]
  
  fetchTurnoverData(begin, end)
  fetchUserData(begin, end)
  fetchOrderData(begin, end)
  fetchTop10Data(begin, end)
}

// 1. 营业额图表
const fetchTurnoverData = async (begin, end) => {
  const res = await getTurnoverStatsAPI(begin, end)
  if (!turnoverChart) turnoverChart = echarts.init(turnoverChartRef.value)
  const dateList = res.dateList ? res.dateList.split(',') : []
  const turnoverList = res.turnoverList ? res.turnoverList.split(',') : []
  
  turnoverChart.setOption({
    title: { text: '营业额统计' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dateList },
    yAxis: { type: 'value', name: '金额 (元)' },
    series: [{
      data: turnoverList,
      type: 'line',
      smooth: true,
      areaStyle: {}, // 加上渐变阴影
      itemStyle: { color: '#409EFF' }
    }]
  })
}

// 2. 用户统计图表
const fetchUserData = async (begin, end) => {
  const res = await getUserStatsAPI(begin, end)
  if (!userChart) userChart = echarts.init(userChartRef.value)
  const dateList = res.dateList ? res.dateList.split(',') : []
  const newUserList = res.newUserList ? res.newUserList.split(',') : []
  const totalUserList = res.totalUserList ? res.totalUserList.split(',') : []
  
  userChart.setOption({
    title: { text: '用户统计' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户', '总用户'] },
    xAxis: { type: 'category', data: dateList },
    yAxis: { type: 'value', name: '人数' },
    series: [
      { name: '新增用户', type: 'line', data: newUserList, itemStyle: { color: '#67C23A' } },
      { name: '总用户', type: 'line', data: totalUserList, itemStyle: { color: '#E6A23C' } }
    ]
  })
}

// 3. 订单统计图表
const fetchOrderData = async (begin, end) => {
  const res = await getOrderStatsAPI(begin, end)
  if (!orderChart) orderChart = echarts.init(orderChartRef.value)
  const dateList = res.dateList ? res.dateList.split(',') : []
  const orderCountList = res.orderCountList ? res.orderCountList.split(',') : []
  const validOrderCountList = res.validOrderCountList ? res.validOrderCountList.split(',') : []
  
  orderChart.setOption({
    title: { text: '订单统计', subtext: `订单完成率: ${(res.orderCompletionRate * 100).toFixed(1)}%` },
    tooltip: { trigger: 'axis' },
    legend: { data: ['总订单数', '有效订单数'] },
    xAxis: { type: 'category', data: dateList },
    yAxis: { type: 'value', name: '单数' },
    series: [
      { name: '总订单数', type: 'line', data: orderCountList, itemStyle: { color: '#909399' } },
      { name: '有效订单数', type: 'line', data: validOrderCountList, itemStyle: { color: '#F56C6C' } }
    ]
  })
}

// 4. 销量 Top10 图表
const fetchTop10Data = async (begin, end) => {
  const res = await getTop10StatsAPI(begin, end)
  if (!top10Chart) top10Chart = echarts.init(top10ChartRef.value)
  const nameList = res.nameList ? res.nameList.split(',') : []
  const numberList = res.numberList ? res.numberList.split(',') : []
  
  top10Chart.setOption({
    title: { text: '销量排名 Top 10' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: { 
      type: 'category', 
      data: nameList,
      axisLabel: { interval: 0, rotate: 30 } // 名字太长时倾斜显示
    },
    yAxis: { type: 'value', name: '销量 (份)' },
    series: [{
      data: numberList,
      type: 'bar',
      barWidth: '40%',
      itemStyle: { color: '#73C0DE', borderRadius: [5, 5, 0, 0] }
    }]
  })
}

// --- 导出 Excel 报表逻辑 ---
const exportReport = () => {
  // 直接通过浏览器的 window.open 下载文件，别忘了带上 Token 绕过拦截器
  const token = localStorage.getItem('token')
  window.open(`/api/admin/report/export?token=${token}`, '_blank')
}

// --- 挂载时执行 ---
onMounted(() => {
  initDate()
  nextTick(() => {
    handleDateChange()
  })
  
  // 监听窗口大小变化，使 ECharts 图表自适应缩放
  window.addEventListener('resize', () => {
    if (turnoverChart) turnoverChart.resize()
    if (userChart) userChart.resize()
    if (orderChart) orderChart.resize()
    if (top10Chart) top10Chart.resize()
  })
})
</script>

<style scoped>
.app-container { padding: 20px; background: white; border-radius: 8px; }
.filter-container { display: flex; align-items: center; padding-bottom: 20px; border-bottom: 1px solid #ebeef5; }
</style>