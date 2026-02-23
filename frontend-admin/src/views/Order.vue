<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="queryParams.number" placeholder="请输入订单号" style="width: 200px; margin-right: 15px;" clearable />
      <el-input v-model="queryParams.phone" placeholder="用户手机号" style="width: 150px; margin-right: 15px;" clearable />
      <el-select v-model="queryParams.status" placeholder="订单状态" style="width: 150px; margin-right: 15px;" clearable>
        <el-option label="待接单" :value="2" />
        <el-option label="已接单" :value="3" />
        <el-option label="派送中" :value="4" />
        <el-option label="已完成" :value="5" />
        <el-option label="已取消" :value="6" />
      </el-select>
      <el-button type="primary" @click="fetchData">查询</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="number" label="订单号" align="center" width="200" />
      <el-table-column prop="consignee" label="收货人" align="center" />
      <el-table-column prop="phone" label="手机号" align="center" width="120" />
      <el-table-column prop="address" label="地址" align="center" show-overflow-tooltip />
      <el-table-column prop="amount" label="实收金额" align="center">
        <template #default="{ row }"><span style="color: #f56c6c; font-weight: bold;">￥{{ row.amount }}</span></template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="orderTime" label="下单时间" align="center" width="180" />
      
      <el-table-column label="操作" align="center" width="280">
        <template #default="{ row }">
          <template v-if="row.status === 2">
            <el-button type="primary" size="small" @click="handleAction('accept', row)">接单</el-button>
            <el-button type="danger" size="small" @click="handleAction('reject', row)">拒单</el-button>
          </template>
          <template v-if="row.status === 3">
            <el-button type="warning" size="small" @click="handleAction('delivery', row)">派送</el-button>
          </template>
          <template v-if="row.status === 4">
            <el-button type="success" size="small" @click="handleAction('complete', row)">完成</el-button>
          </template>
          <el-button type="info" link @click="viewDetails(row.id)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination 
      v-model:current-page="queryParams.page" 
      v-model:page-size="queryParams.pageSize" 
      :total="total" 
      layout="total, prev, pager, next" 
      @current-change="fetchData" 
      style="margin-top: 20px; justify-content: flex-end;"
    />
    <el-dialog title="订单详情" v-model="detailVisible" width="700px">
      <el-descriptions border :column="2" v-if="orderDetail" size="large">
        <el-descriptions-item label="订单号">{{ orderDetail.number }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusTagType(orderDetail.status)">{{ getStatusText(orderDetail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收货人">{{ orderDetail.consignee }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ orderDetail.phone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ orderDetail.address }}</el-descriptions-item>
        <el-descriptions-item label="订单金额"><span style="color: red; font-weight: bold;">￥{{ orderDetail.amount }}</span></el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ orderDetail.orderTime }}</el-descriptions-item>
        <el-descriptions-item label="备注留言" :span="2">{{ orderDetail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <h3 style="margin-top: 20px;">菜品明细</h3>
      <el-table :data="orderDetail?.orderDetailList" border style="width: 100%">
        <el-table-column prop="name" label="商品名称" align="center" />
        <el-table-column prop="dishFlavor" label="口味" align="center" />
        <el-table-column prop="number" label="数量" align="center" width="100" />
        <el-table-column prop="amount" label="单价" align="center" width="100">
          <template #default="{ row }">￥{{ row.amount }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrderPageAPI, confirmOrderAPI, rejectOrderAPI, deliveryOrderAPI, completeOrderAPI, getOrderDetailAPI} from '../api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const queryParams = ref({ page: 1, pageSize: 10, number: '', phone: '', status: null })
const detailVisible = ref(false)
const orderDetail = ref(null)


// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getOrderPageAPI(queryParams.value)
    tableData.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

// 状态字典转换
const getStatusText = (status) => {
  const map = { 1: '待付款', 2: '待接单', 3: '已接单', 4: '派送中', 5: '已完成', 6: '已取消' }
  return map[status] || '未知'
}
const getStatusTagType = (status) => {
  const map = { 1: 'info', 2: 'warning', 3: '', 4: 'primary', 5: 'success', 6: 'danger' }
  return map[status] || 'info'
}

// 处理各种业务动作
const handleAction = (action, row) => {
  const actionMap = {
    accept: { title: '接单', api: confirmOrderAPI, data: { id: row.id } },
    reject: { title: '拒单', api: rejectOrderAPI, data: { id: row.id, rejectionReason: '商家忙碌' } },
    delivery: { title: '派送', api: deliveryOrderAPI, isPath: true },
    complete: { title: '完成', api: completeOrderAPI, isPath: true }
  }
  const target = actionMap[action]

  ElMessageBox.confirm(`确认要执行【${target.title}】操作吗？`, '提示', { type: 'warning' }).then(async () => {
    // 根据接口设计，有的传对象，有的传路径参数
    if (target.isPath) {
      await target.api(row.id)
    } else {
      await target.api(target.data)
    }
    ElMessage.success(`${target.title}成功`)
    fetchData()
  })
}

const viewDetails = async (id) => {
  try {
    const res = await getOrderDetailAPI(id)
    orderDetail.value = res // 把后端返回的详细数据赋给变量
    detailVisible.value = true // 打开弹窗
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.app-container { padding: 20px; background: white; border-radius: 8px; }
.filter-container { display: flex; align-items: center; }
</style>