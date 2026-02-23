<template>
  <view class="my-container">
    <view class="user-header">
      <view class="user-info">
        <image class="avatar" src="https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg" mode="aspectFill"></image>
        <view class="text-info">
          <text class="name">校园递送用户</text>
          <text class="phone">138****0000</text>
        </view>
      </view>
      
      <view class="sign-btn" :class="{ 'signed': isSigned }" @click="handleSign">
        <text>{{ isSigned ? '今日已签到' : '签到领红包' }}</text>
      </view>
    </view>

    <view class="assets-card">
      <view class="asset-item">
        <text class="num">0</text>
        <text class="label">我的积分</text>
      </view>
      <view class="asset-item" @click="goToCoupon">
        <text class="num red">{{ couponNum }}</text>
        <text class="label">优惠券</text>
      </view>
      <view class="asset-item">
        <text class="num">0.00</text>
        <text class="label">钱包余额</text>
      </view>
    </view>

    <view class="menu-list">
      <view class="menu-item" @click="goToAddress">
        <view class="item-left">
          <text class="icon">📍</text>
          <text class="item-text">地址管理</text>
        </view>
        <text class="arrow">❯</text>
      </view>
      <view class="menu-item" @click="goToOrderList">
        <view class="item-left">
          <text class="icon">📜</text>
          <text class="item-text">历史订单</text>
        </view>
        <text class="arrow">❯</text>
      </view>
      <view class="menu-item" @click="showDevToast">
        <view class="item-left">
          <text class="icon">🎧</text>
          <text class="item-text">联系客服</text>
        </view>
        <text class="arrow">❯</text>
      </view>
      <view class="menu-item" @click="goToCoupon">
        <view class="item-left">
          <text class="icon">🎟️</text>
          <text class="item-text">我的优惠券</text>
        </view>
        <text class="arrow">❯</text>
      </view>
    </view>

    <button class="logout-btn" @click="handleLogout">退出登录</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const isSigned = ref(false)
const couponNum = ref(2) // 默认两张优惠券

// 签到逻辑
const handleSign = () => {
  if (isSigned.value) {
    uni.showToast({ title: '明天再来签到吧~', icon: 'none' })
    return
  }
  
  uni.showLoading({ title: '签到中...' })
  // 模拟调用后端签到接口
  setTimeout(() => {
    uni.hideLoading()
    isSigned.value = true
    couponNum.value += 1 // 签到成功，优惠券 + 1
    uni.showModal({
      title: '签到成功！🎉',
      content: '恭喜获得【2元无门槛配送券】一张！',
      showCancel: false,
      confirmText: '开心收下',
      confirmColor: '#ee0a24'
    })
  }, 600)
}

const goToCoupon = () => {
  uni.navigateTo({ url: '/pages/coupon/my' })
}

const showDevToast = () => {
  uni.showToast({ title: '页面开发中...', icon: 'none' })
}

const goToAddress = () => {
  uni.navigateTo({ url: '/pages/address/address' })
}

const goToOrderList = () => {
  uni.navigateTo({ url: '/pages/order/list' })
}


// 退出登录
const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')
        uni.reLaunch({ url: '/pages/login/login' })
      }
    }
  })

}
</script>

<style scoped>
.my-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 50rpx;
}

/* 头部样式 */
.user-header {
  background: linear-gradient(135deg, #ff6034 0%, #ee0a24 100%);
  padding: 60rpx 40rpx 80rpx 40rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom-left-radius: 40rpx;
  border-bottom-right-radius: 40rpx;
}
.user-info {
  display: flex;
  align-items: center;
}
.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255,255,255,0.8);
}
.text-info {
  margin-left: 20rpx;
  color: #fff;
  display: flex;
  flex-direction: column;
}
.name {
  font-size: 36rpx;
  font-weight: bold;
}
.phone {
  font-size: 24rpx;
  opacity: 0.8;
  margin-top: 10rpx;
}

/* 签到按钮 */
.sign-btn {
  background-color: rgba(255,255,255,0.2);
  color: #fff;
  padding: 12rpx 24rpx;
  border-radius: 30rpx;
  font-size: 24rpx;
  border: 1px solid rgba(255,255,255,0.5);
  transition: all 0.3s;
}
.sign-btn.signed {
  background-color: rgba(0,0,0,0.1);
  color: rgba(255,255,255,0.6);
  border-color: transparent;
}

/* 资产卡片 */
.assets-card {
  background-color: #fff;
  border-radius: 20rpx;
  margin: -40rpx 30rpx 30rpx 30rpx;
  padding: 30rpx 0;
  display: flex;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
  position: relative;
  z-index: 10;
}
.asset-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-right: 1px solid #eee;
}
.asset-item:last-child {
  border-right: none;
}
.num {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}
.num.red {
  color: #ee0a24;
}
.label {
  font-size: 24rpx;
  color: #666;
  margin-top: 10rpx;
}

/* 菜单列表 */
.menu-list {
  background-color: #fff;
  border-radius: 20rpx;
  margin: 0 30rpx;
  padding: 0 30rpx;
}
.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1px solid #f5f5f5;
}
.menu-item:last-child {
  border-bottom: none;
}
.item-left {
  display: flex;
  align-items: center;
}
.icon {
  font-size: 32rpx;
  margin-right: 20rpx;
}
.item-text {
  font-size: 28rpx;
  color: #333;
}
.arrow {
  color: #ccc;
  font-family: consolas;
}

/* 退出登录 */
.logout-btn {
  margin: 60rpx 30rpx;
  background-color: #fff;
  color: #666;
  border-radius: 50rpx;
  font-size: 30rpx;
}
.logout-btn::after {
  border: none;
}
</style>