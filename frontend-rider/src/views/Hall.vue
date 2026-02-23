<template>
  <div class="hall-container">
    <van-nav-bar title="抢单大厅" fixed placeholder safe-area-inset-top />
    
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh" success-text="刷新成功">
      
      <van-empty v-if="orderList.length === 0 && !loading" description="当前暂无新订单，稍后再来看看吧" />

      <div class="order-list" v-else>
        <div class="order-card" v-for="order in orderList" :key="order.id">
          
          <div class="card-header">
            <span class="order-num">#{{ order.number.substring(order.number.length - 4) }}</span>
            <span class="price">￥{{ order.amount }}</span>
          </div>

          <div class="card-body">
            <div class="info-row">
              <van-icon name="shop-o" class="icon" />
              <span class="text fw-bold">校园餐厅 (商家)</span>
            </div>
            <div class="info-row">
              <van-icon name="location-o" class="icon text-primary" />
              <span class="text">{{ order.address }}</span>
            </div>
            <div class="info-row">
              <van-icon name="clock-o" class="icon" />
              <span class="text text-gray">下单时间: {{ order.orderTime }}</span>
            </div>
          </div>

          <div class="card-footer">
            <van-button 
              type="primary" 
              size="large" 
              round 
              color="linear-gradient(to right, #ff6034, #ee0a24)"
              @click="handleTakeOrder(order.id)"
            >
              立即抢单
            </van-button>
          </div>
        </div>
      </div>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getWaitListAPI, takeOrderAPI } from '../api/order'
import { showToast, showSuccessToast, showFailToast, showConfirmDialog } from 'vant'

const orderList = ref([])
const refreshing = ref(false)
const loading = ref(false)

// 加载抢单大厅数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getWaitListAPI()
    orderList.value = res // 后端直接返回 List<OrderVO>
  } catch (error) {
    showFailToast('获取订单失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 下拉刷新动作
const onRefresh = () => {
  loadData()
}

// 执行抢单逻辑
const handleTakeOrder = (orderId) => {
  showConfirmDialog({
    title: '确认抢单',
    message: '抢单后请尽快前往商家取餐，超时将扣除信誉分！',
  }).then(async () => {
    try {
      // 这里的接口做了 Sentinel 限流
      await takeOrderAPI(orderId) 
      showSuccessToast('抢单成功！请尽快配送')
      // 抢单成功后，立刻刷新大厅列表，把被抢走的单子移除
      loadData() 
    } catch (error) {
      // 如果触发限流或者被别人抢了，request 拦截器会抛出异常，这里不需要额外处理
      // 拦截器会展示后端返回的 "抢单人数过多，请手动刷新"
      loadData() 
    }
  }).catch(() => {
    // 点击取消
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.hall-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 60px; /* 给底部导航栏留出空间 */
}
.order-list {
  padding: 12px;
}
.order-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px dashed #ebedf0;
  padding-bottom: 10px;
  margin-bottom: 10px;
}
.order-num { font-size: 20px; font-weight: bold; color: #323233; }
.price { font-size: 22px; font-weight: bold; color: #ee0a24; }

.card-body .info-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}
.card-body .icon { font-size: 18px; margin-right: 8px; }
.fw-bold { font-weight: bold; color: #323233; }
.text-primary { color: #1989fa; font-weight: bold; font-size: 15px; }
.text-gray { color: #969799; font-size: 13px; }

.card-footer { margin-top: 15px; }
</style>