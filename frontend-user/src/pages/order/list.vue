<template>
  <view class="list-container">
    <view class="tab-bar">
      <view class="tab-item" :class="{ active: currentStatus === item.value }" 
            v-for="item in tabList" :key="item.value" @click="handleTabChange(item.value)">
        <text>{{ item.label }}</text>
        <view class="line" v-if="currentStatus === item.value"></view>
      </view>
    </view>

    <scroll-view class="order-scroll" scroll-y @scrolltolower="loadMore">
      <view class="empty-tip" v-if="orderList.length === 0">
        <text>暂无相关订单数据哦~</text>
      </view>

      <view class="order-card" v-for="order in orderList" :key="order.id" @click="goToDetail(order.id)">
        <view class="card-header">
          <text class="time">{{ order.orderTime }}</text>
          <text class="status" :class="`status-${order.status}`">{{ getStatusText(order.status) }}</text>
        </view>

        <view class="goods-box">
          <scroll-view class="img-scroll" scroll-x v-if="order.orderDetailList && order.orderDetailList.length > 0">
            <image class="goods-img" v-for="goods in order.orderDetailList" :key="goods.id" 
                   :src="goods.image || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'" mode="aspectFill"></image>
          </scroll-view>
          <view class="price-info">
            <view class="total-price">￥{{ order.amount }}</view>
            <view class="total-num">共{{ getOrderTotalNum(order) }}件</view>
          </view>
        </view>

        <view class="card-footer" @click.stop>
          <button class="btn cancel-btn" v-if="order.status === 1" @click="handleCancel(order.id)">取消订单</button>
          <button class="btn pay-btn" v-if="order.status === 1" @click="handlePay(order)">去支付</button>
          <button class="btn normal-btn" v-if="order.status === 5 || order.status === 6" @click="handleRepetition(order.id)">再来一单</button>
        </view>
      </view>
      
      <view class="loading-text" v-if="orderList.length > 0">{{ hasMore ? '加载中...' : '没有更多了' }}</view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getOrderListAPI, cancelOrderAPI, repetitionOrderAPI, payOrderAPI } from '../../api/order.js'

// Tab 状态: ''全部, 1待付款, 2待接单, 3已接单, 4派送中, 5已完成, 6已取消
const tabList = [
  { label: '全部', value: '' },
  { label: '待付款', value: 1 },
  { label: '进行中', value: 2 }, // 把待接单、派送中统称为进行中，简化展示
  { label: '已完成', value: 5 },
  { label: '已取消', value: 6 }
]

const currentStatus = ref('')
const orderList = ref([])
const page = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)

onShow(() => {
  refreshList()
})

const handleTabChange = (status) => {
  currentStatus.value = status
  refreshList()
}

const refreshList = async () => {
  page.value = 1
  hasMore.value = true
  orderList.value = []
  await fetchOrderData()
}

const loadMore = async () => {
  if (!hasMore.value) return
  page.value++
  await fetchOrderData()
}

// 🌟 修复：精准控制参数，查“全部”时坚决不传 status 字段
const fetchOrderData = async () => {
  uni.showLoading({ title: '加载中...' })
  try {
    // 1. 先准备必传的分页参数
    const params = {
      page: page.value,
      pageSize: pageSize.value
    }
    
    // 2. 只有当状态不为空时，才把 status 塞进参数对象里
    if (currentStatus.value !== '') {
      params.status = currentStatus.value
    }

    // 3. 发起请求
    const res = await getOrderListAPI(params)
    
    // 防御性校验后端数据
    const newRecords = (res && Array.isArray(res.records)) ? res.records : []
    orderList.value = [...orderList.value, ...newRecords]
    
    if (newRecords.length < pageSize.value) {
      hasMore.value = false
    }
  } catch (error) {
    hasMore.value = false
  } finally {
    uni.hideLoading()
  }
}

// 辅助函数
const getStatusText = (status) => {
  const map = { 1: '待付款', 2: '待接单', 3: '已接单', 4: '派送中', 5: '已完成', 6: '已取消' }
  return map[status] || '未知状态'
}
const getOrderTotalNum = (order) => {
  if (!order.orderDetailList) return 0
  return order.orderDetailList.reduce((sum, item) => sum + item.number, 0)
}

// 跳转详情
const goToDetail = (id) => {
  uni.navigateTo({ url: `/pages/order/detail?id=${id}` })
}

// --- 订单操作逻辑 ---
const handleCancel = (id) => {
  uni.showModal({
    title: '提示', content: '确定要取消这个订单吗？', confirmColor: '#ff6034',
    success: async (res) => {
      if (res.confirm) {
        await cancelOrderAPI(id)
        uni.showToast({ title: '已取消' })
        refreshList()
      }
    }
  })
}

const handlePay = async (order) => {
  uni.showLoading({ title: '拉起支付...' })
  try {
    await payOrderAPI({ orderNumber: order.number, payMethod: 1 })
    uni.hideLoading()
    uni.showToast({ title: '支付成功', icon: 'success' })
    refreshList() // 刷新列表变成待接单
  } catch (err) { uni.hideLoading() }
}

const handleRepetition = async (id) => {
  uni.showLoading({ title: '处理中...' })
  try {
    await repetitionOrderAPI(id)
    uni.hideLoading()
    uni.showToast({ title: '商品已加入购物车' })
    // 跳转到点餐页
    setTimeout(() => { uni.switchTab({ url: '/pages/menu/menu' }) }, 1000)
  } catch (err) { uni.hideLoading() }
}
</script>

<style scoped>
.list-container { height: 100vh; display: flex; flex-direction: column; background-color: #f7f8fa; }
.tab-bar { display: flex; background: #fff; height: 100rpx; align-items: center; border-bottom: 1px solid #eee; }
.tab-item { flex: 1; text-align: center; font-size: 28rpx; color: #666; position: relative; height: 100%; display: flex; justify-content: center; align-items: center; }
.tab-item.active { color: #333; font-weight: bold; }
.tab-item .line { position: absolute; bottom: 0; width: 40rpx; height: 6rpx; background: #ff6034; border-radius: 6rpx; }

.order-scroll { flex: 1; overflow: hidden; padding: 20rpx; box-sizing: border-box; }
.empty-tip { text-align: center; color: #999; margin-top: 200rpx; font-size: 28rpx; }

.order-card { background: #fff; border-radius: 20rpx; padding: 30rpx; margin-bottom: 20rpx; box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.02); }
.card-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f9f9f9; padding-bottom: 20rpx; margin-bottom: 20rpx; }
.time { font-size: 26rpx; color: #999; }
.status { font-size: 28rpx; font-weight: bold; }
.status-1 { color: #ff6034; } /* 待付款橙色 */
.status-5 { color: #4caf50; } /* 完成绿色 */
.status-6 { color: #999; } /* 取消灰色 */

.goods-box { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30rpx; }
.img-scroll { flex: 1; white-space: nowrap; overflow: hidden; }
.goods-img { width: 120rpx; height: 120rpx; border-radius: 12rpx; margin-right: 20rpx; background: #f5f5f5; display: inline-block; }
.price-info { width: 150rpx; text-align: right; }
.total-price { font-size: 32rpx; font-weight: bold; color: #333; }
.total-num { font-size: 24rpx; color: #999; margin-top: 10rpx; }

.card-footer { display: flex; justify-content: flex-end; }
.btn { margin: 0 0 0 20rpx; padding: 0 30rpx; height: 60rpx; line-height: 60rpx; font-size: 26rpx; border-radius: 30rpx; background: #fff; }
.btn::after { border: none; }
.normal-btn { border: 1px solid #ddd; color: #333; }
.cancel-btn { border: 1px solid #ddd; color: #666; }
.pay-btn { border: 1px solid #ff6034; color: #ff6034; background: #fff8f5; }

.loading-text { text-align: center; font-size: 24rpx; color: #999; padding: 20rpx 0; }
</style>