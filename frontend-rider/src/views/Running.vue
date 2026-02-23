<template>
  <div class="running-container">
    <van-nav-bar title="配送中" fixed placeholder safe-area-inset-top />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-empty v-if="orderList.length === 0 && !loading" description="当前没有正在配送的订单哦~" />

      <div class="order-list" v-else>
        <div class="order-card" v-for="order in orderList" :key="order.id">
          <div class="card-header">
            <span class="order-num">#{{ order.number.substring(order.number.length - 4) }}</span>
            <span class="status-tag">派送中</span>
          </div>

          <div class="card-body">
            <div class="info-row">
              <van-icon name="user-o" class="icon" />
              <span class="text fw-bold">{{ order.consignee }} ({{ order.phone }})</span>
            </div>
            <div class="info-row">
              <van-icon name="location-o" class="icon text-primary" />
              <span class="text">{{ order.address }}</span>
            </div>
          </div>

          <div class="card-footer van-hairline--top">
            <van-button plain type="danger" size="small" round @click="handleCancel(order.id)">取消配送</van-button>
            <van-button type="primary" size="small" round color="#ee0a24" @click="openCompleteDialog(order.id)">送达核销</van-button>
          </div>
        </div>
      </div>
    </van-pull-refresh>

    <van-dialog v-model:show="showDialog" title="输入取餐码" show-cancel-button @confirm="confirmComplete">
      <van-field v-model="pickupCode" placeholder="请向用户索要取餐码并输入" input-align="center" size="large" />
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRunningListAPI, cancelOrderAPI, completeOrderAPI } from '../api/order.js'
import { showSuccessToast, showConfirmDialog, showFailToast } from 'vant'

const orderList = ref([])
const refreshing = ref(false)
const loading = ref(false)

// 弹窗相关
const showDialog = ref(false)
const pickupCode = ref('')
const currentOrderId = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    // 状态 4 代表派送中
    orderList.value = await getRunningListAPI(4)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onRefresh = () => loadData()

// 取消配送
const handleCancel = (orderId) => {
  showConfirmDialog({ title: '警告', message: '无故取消配送将扣除信誉分，确定要取消吗？' }).then(async () => {
    await cancelOrderAPI(orderId)
    showSuccessToast('已取消配送')
    loadData()
  }).catch(() => {})
}

// 打开核销弹窗
const openCompleteDialog = (orderId) => {
  currentOrderId.value = orderId
  pickupCode.value = ''
  showDialog.value = true
}

// 确认核销
const confirmComplete = async () => {
  if (!pickupCode.value) {
    showFailToast('取餐码不能为空')
    return
  }
  await completeOrderAPI(currentOrderId.value, pickupCode.value)
  showSuccessToast('订单已送达！')
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.running-container { min-height: 100vh; background-color: #f7f8fa; padding-bottom: 60px; }
.order-list { padding: 12px; }
.order-card { background: white; border-radius: 12px; padding: 16px; margin-bottom: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.card-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 10px; margin-bottom: 10px; }
.order-num { font-size: 20px; font-weight: bold; color: #323233; }
.status-tag { background: #e8f3fe; color: #1989fa; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.card-body .info-row { display: flex; align-items: center; margin-bottom: 8px; font-size: 14px; }
.card-body .icon { font-size: 18px; margin-right: 8px; }
.fw-bold { font-weight: bold; color: #323233; }
.text-primary { color: #1989fa; font-weight: bold; font-size: 15px; }
.card-footer { margin-top: 15px; padding-top: 15px; display: flex; justify-content: flex-end; gap: 10px; }
</style>