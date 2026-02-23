<template>
  <view class="coupon-container">
    <view class="tab-bar">
      <view class="tab-item" :class="{ active: currentStatus === 0 }" @click="switchTab(0)">未使用</view>
      <view class="tab-item" :class="{ active: currentStatus === 1 }" @click="switchTab(1)">已使用</view>
      <view class="tab-item" :class="{ active: currentStatus === 2 }" @click="switchTab(2)">已过期</view>
      <view class="slider" :style="{ transform: `translateX(${currentStatus * 100}%)` }"></view>
    </view>

    <scroll-view class="coupon-scroll" scroll-y>
      <view class="empty-tip" v-if="couponList.length === 0">
        <text>暂无相关优惠券哦~</text>
      </view>

      <view class="coupon-card" :class="{ 'disabled': currentStatus !== 0 }" v-for="item in couponList" :key="item.id">
        <view class="card-left">
          <view class="amount-box">
            <text class="symbol">￥</text>
            <text class="amount">{{ item.amount }}</text>
          </view>
          <text class="condition">满{{ item.conditionAmount }}可用</text>
        </view>

        <view class="divider"></view>

        <view class="card-right">
          <view class="title">{{ item.name }}</view>
          <view class="time">有效期至：{{ item.expirationTime }}</view>
          
          <view class="use-btn" v-if="currentStatus === 0" @click="goToMenu">去使用</view>
          <image class="status-stamp" v-if="currentStatus === 1" src="https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg" mode="aspectFit"></image> 
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyCouponListAPI } from '../../api/coupon.js'

// 🌟 修改 5：0: 未使用, 1: 已使用, 2: 已过期。默认值设为 0
const currentStatus = ref(0)
const couponList = ref([])

onShow(() => {
  fetchCoupons()
})

const switchTab = (status) => {
  if (currentStatus.value === status) return
  currentStatus.value = status
  fetchCoupons()
}

const fetchCoupons = async () => {
  uni.showLoading({ title: '加载中...' })
  try {
    const res = await getMyCouponListAPI(currentStatus.value)
    
    // 强制转换为数组，防止报错
    couponList.value = Array.isArray(res) ? res : (res?.records || [])
    
    // 🌟 修改 6：假数据的判断条件也改为 0
    if (couponList.value.length === 0 && currentStatus.value === 0) {
      couponList.value = [
        { id: 1, name: '新人专享神券', amount: 5, conditionAmount: 20, expirationTime: '2026-12-31 23:59:59' },
        { id: 2, name: '免配送费券', amount: 3, conditionAmount: 0, expirationTime: '2026-05-01 12:00:00' }
      ]
    }
  } catch (error) {
    couponList.value = []
  } finally {
    uni.hideLoading()
  }
}

const goToMenu = () => {
  uni.switchTab({ url: '/pages/menu/menu' })
}
</script>

<style scoped>
/* 所有 CSS 样式保持完全不变，非常完美 */
.coupon-container { height: 100vh; display: flex; flex-direction: column; background-color: #f7f8fa; }
.tab-bar { display: flex; background: #fff; height: 100rpx; position: relative; }
.tab-item { flex: 1; text-align: center; line-height: 100rpx; font-size: 28rpx; color: #666; font-weight: bold; transition: color 0.3s; z-index: 2; }
.tab-item.active { color: #ff6034; }
.slider { position: absolute; bottom: 0; left: 0; width: 33.33%; height: 6rpx; display: flex; justify-content: center; transition: transform 0.3s cubic-bezier(0.645, 0.045, 0.355, 1); z-index: 1; }
.slider::after { content: ''; width: 40rpx; height: 100%; background: #ff6034; border-radius: 6rpx; }
.coupon-scroll { flex: 1; padding: 30rpx; box-sizing: border-box; overflow: hidden; }
.empty-tip { text-align: center; color: #999; margin-top: 200rpx; font-size: 28rpx; }
.coupon-card { display: flex; height: 200rpx; margin-bottom: 30rpx; background: #fff; border-radius: 16rpx; position: relative; overflow: hidden; box-shadow: 0 4rpx 16rpx rgba(255, 96, 52, 0.1); }
.coupon-card.disabled { filter: grayscale(100%); opacity: 0.7; box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05); }
.card-left { width: 240rpx; height: 100%; background: linear-gradient(135deg, #ff9e80, #ff6034); display: flex; flex-direction: column; justify-content: center; align-items: center; color: #fff; position: relative; }
.amount-box { display: flex; align-items: baseline; }
.symbol { font-size: 32rpx; font-weight: bold; }
.amount { font-size: 72rpx; font-weight: bold; line-height: 1; }
.condition { font-size: 24rpx; margin-top: 10rpx; opacity: 0.9; }
.divider { width: 0; border-left: 2px dashed #eee; position: relative; margin: 20rpx 0; }
.divider::before, .divider::after { content: ''; position: absolute; left: -14rpx; width: 28rpx; height: 28rpx; background-color: #f7f8fa; border-radius: 50%; z-index: 10; }
.divider::before { top: -34rpx; }
.divider::after { bottom: -34rpx; }
.card-right { flex: 1; padding: 30rpx 20rpx 30rpx 30rpx; display: flex; flex-direction: column; justify-content: space-between; position: relative; }
.title { font-size: 32rpx; font-weight: bold; color: #333; }
.time { font-size: 22rpx; color: #999; }
.use-btn { position: absolute; right: 20rpx; bottom: 30rpx; width: 120rpx; height: 50rpx; line-height: 50rpx; text-align: center; background: #fff; color: #ff6034; border: 1px solid #ff6034; border-radius: 25rpx; font-size: 24rpx; font-weight: bold; }
.status-stamp { position: absolute; right: 20rpx; top: 20rpx; width: 100rpx; height: 100rpx; opacity: 0.2; }
</style>