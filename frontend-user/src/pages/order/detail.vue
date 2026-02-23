<template>
  <view class="detail-container" v-if="orderData.id">
    <view class="status-header">
      <text class="status-text">{{ getStatusText(orderData.status) }}</text>
      <text class="status-desc">{{ getStatusDesc(orderData.status) }}</text>
    </view>

    <view class="address-card">
      <view class="location">
        <text class="icon">📍</text>
        <text class="address-text">{{ orderData.address }}</text>
      </view>
      <view class="user-info">
        <text class="name">{{ orderData.consignee }}</text>
        <text class="phone">{{ orderData.phone }}</text>
      </view>
    </view>

    <view class="goods-card">
      <view class="shop-name">校园递送自营店</view>
      <view class="goods-item" v-for="item in orderData.orderDetailList" :key="item.id">
        <image class="goods-img" :src="item.image || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'" mode="aspectFill"></image>
        <view class="goods-info">
          <view class="goods-name">{{ item.name }}</view>
          <view class="goods-price-box">
            <text class="num">x{{ item.number }}</text>
            <text class="price">￥{{ item.amount }}</text>
          </view>
        </view>
      </view>
      <view class="fee-row"><text>包装费</text><text>￥{{ orderData.packAmount }}</text></view>
      <view class="fee-row"><text>配送费</text><text>￥3.00</text></view>
      <view class="total-row">
        <text>实付款：</text><text class="total-price">￥{{ orderData.amount }}</text>
      </view>
    </view>

    <view class="info-card">
      <view class="info-item"><text class="label">订单号码</text><text class="value">{{ orderData.number }}</text></view>
      <view class="info-item"><text class="label">下单时间</text><text class="value">{{ orderData.orderTime }}</text></view>
      <view class="info-item"><text class="label">支付方式</text><text class="value">{{ orderData.payMethod === 1 ? '微信支付' : '其他' }}</text></view>
      <view class="info-item" v-if="orderData.remark"><text class="label">订单备注</text><text class="value">{{ orderData.remark }}</text></view>
    </view>

    <view style="height: 40rpx;"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getOrderDetailAPI } from '../../api/order.js'

const orderData = ref({})

onLoad((options) => {
  if (options.id) {
    fetchDetail(options.id)
  }
})

const fetchDetail = async (id) => {
  uni.showLoading({ title: '加载中...' })
  try {
    const res = await getOrderDetailAPI(id)
    orderData.value = res || {}
  } catch (error) {} finally {
    uni.hideLoading()
  }
}

const getStatusText = (status) => {
  const map = { 1: '等待支付', 2: '等待商家接单', 3: '商家已接单', 4: '骑手派送中', 5: '订单已完成', 6: '订单已取消' }
  return map[status] || '未知状态'
}
const getStatusDesc = (status) => {
  const map = { 1: '请尽快完成支付，超时将自动取消', 2: '商家正在确认订单，请稍候', 3: '商家正在努力制作中', 4: '外卖小哥正在火速赶来', 5: '感谢您的订餐，欢迎下次光临', 6: '订单已取消，期待您再次下单' }
  return map[status] || ''
}
</script>

<style scoped>
.detail-container { min-height: 100vh; background-color: #f7f8fa; padding-bottom: 40rpx; }

/* 头部状态 */
.status-header { background: linear-gradient(135deg, #ff9e80, #ff6034); padding: 60rpx 40rpx 80rpx; display: flex; flex-direction: column; }
.status-text { font-size: 44rpx; font-weight: bold; color: #fff; margin-bottom: 10rpx; }
.status-desc { font-size: 26rpx; color: rgba(255,255,255,0.9); }

/* 地址卡片 */
.address-card { background: #fff; border-radius: 20rpx; margin: -40rpx 30rpx 20rpx; padding: 30rpx; box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.05); position: relative; z-index: 10; }
.location { display: flex; align-items: center; margin-bottom: 16rpx; }
.icon { font-size: 36rpx; margin-right: 10rpx; color: #ff6034; }
.address-text { font-size: 32rpx; font-weight: bold; color: #333; }
.user-info { margin-left: 46rpx; font-size: 26rpx; color: #666; }
.user-info .name { margin-right: 20rpx; }

/* 商品清单 */
.goods-card { background: #fff; border-radius: 20rpx; margin: 0 30rpx 20rpx; padding: 30rpx; }
.shop-name { font-size: 28rpx; font-weight: bold; margin-bottom: 30rpx; padding-bottom: 20rpx; border-bottom: 1px solid #f9f9f9; }
.goods-item { display: flex; margin-bottom: 30rpx; }
.goods-img { width: 120rpx; height: 120rpx; border-radius: 12rpx; margin-right: 20rpx; background: #f5f5f5; }
.goods-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.goods-name { font-size: 28rpx; font-weight: bold; color: #333; }
.goods-price-box { display: flex; justify-content: space-between; align-items: center; }
.num { font-size: 26rpx; color: #999; }
.price { font-size: 32rpx; font-weight: bold; color: #333; }
.fee-row { display: flex; justify-content: space-between; font-size: 26rpx; color: #666; margin-top: 20rpx; }
.total-row { display: flex; justify-content: flex-end; align-items: baseline; margin-top: 30rpx; padding-top: 20rpx; border-top: 1px dashed #eee; font-size: 28rpx; color: #333; }
.total-price { font-size: 40rpx; font-weight: bold; color: #ee0a24; }

/* 订单信息 */
.info-card { background: #fff; border-radius: 20rpx; margin: 0 30rpx; padding: 30rpx; }
.info-item { display: flex; justify-content: space-between; margin-bottom: 20rpx; font-size: 26rpx; }
.info-item:last-child { margin-bottom: 0; }
.label { color: #999; }
.value { color: #333; max-width: 70%; text-align: right; word-break: break-all; }
</style>