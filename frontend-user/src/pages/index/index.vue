<template>
  <view class="home-container">
    <view class="status-banner" :class="shopStatus === 1 ? 'open' : 'closed'">
      <text class="status-text">{{ shopStatus === 1 ? '🟢 校园递送营业中，尽情选购吧~' : '🔴 店铺休息中，暂不接单哦' }}</text>
    </view>
    
    <view class="fake-search-box" @click="goToSearch">
      <view class="search-inner">
        <text class="icon">🔍</text>
        <text class="placeholder">想吃点什么？搜搜看...</text>
      </view>
    </view>

    <swiper class="banner-swiper" circular autoplay interval="3500" indicator-dots indicator-active-color="#ff6034">
      <swiper-item><image src="https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg" class="banner-img" mode="aspectFill"></image></swiper-item>
      <swiper-item><image src="https://fastly.jsdelivr.net/npm/@vant/assets/ipad.jpeg" class="banner-img" mode="aspectFill"></image></swiper-item>
    </swiper>

    <view class="action-section">
      <view class="main-card" @click="goToMenu">
        <view class="card-left">
          <text class="card-title">开始点餐</text>
          <text class="card-desc">美味不用等，直接送到宿舍楼</text>
        </view>
        <view class="card-right"><text class="go-icon">🥡</text></view>
      </view>
    </view>

    <view class="info-section">
      <view class="info-header">商家信息</view>
      <view class="info-item"><text class="label">📍 商家地址：</text><text class="value">大学城中心食堂一楼</text></view>
      <view class="info-item"><text class="label">⏰ 营业时间：</text><text class="value">08:00 - 22:30</text></view>
      <view class="info-item"><text class="label">📞 联系电话：</text><text class="value">13800000000</text></view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getShopStatusAPI } from '../../api/user.js'

const shopStatus = ref(1) 

onShow(() => { checkLogin() })

const checkLogin = () => {
  const token = uni.getStorageSync('token')
  if (!token) uni.reLaunch({ url: '/pages/login/login' })
  else loadShopStatus()
}

const loadShopStatus = async () => {
  try { shopStatus.value = await getShopStatusAPI() } catch (error) {}
}

const goToMenu = () => {
  uni.switchTab({ url: '/pages/menu/menu' })
}

// 点击假搜索框的跳转逻辑
const goToSearch = () => {
  // 埋下一个标记，告诉点餐页“我要聚焦搜索框”
  uni.setStorageSync('autoSearchFocus', true)
  uni.switchTab({ url: '/pages/menu/menu' })
}
</script>

<style scoped>
.home-container { min-height: 100vh; background-color: #f7f8fa; padding-bottom: 40rpx; }
.status-banner { width: 100%; padding: 20rpx 0; text-align: center; font-size: 26rpx; font-weight: bold; }
.open { background-color: #e8f5e9; color: #4caf50; }
.closed { background-color: #ffebee; color: #f44336; }

/* 🌟 假搜索框样式 */
.fake-search-box { padding: 20rpx 30rpx; background-color: #fff; }
.search-inner { background-color: #f2f3f5; height: 72rpx; border-radius: 36rpx; display: flex; align-items: center; padding: 0 30rpx; }
.search-inner .icon { font-size: 32rpx; margin-right: 16rpx; color: #999; }
.search-inner .placeholder { font-size: 28rpx; color: #999; }

.banner-swiper { width: 100%; height: 400rpx; }
.banner-img { width: 100%; height: 100%; }
.action-section { padding: 40rpx 30rpx; margin-top: -40rpx; position: relative; z-index: 10; }
.main-card { background: linear-gradient(135deg, #ff9e80, #ff6034); border-radius: 30rpx; padding: 50rpx 40rpx; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 10rpx 30rpx rgba(255, 96, 52, 0.3); }
.card-left { display: flex; flex-direction: column; }
.card-title { font-size: 48rpx; font-weight: bold; color: #fff; margin-bottom: 10rpx; }
.card-desc { font-size: 26rpx; color: rgba(255,255,255,0.9); }
.card-right { width: 100rpx; height: 100rpx; background: rgba(255,255,255,0.2); border-radius: 50%; display: flex; justify-content: center; align-items: center; }
.go-icon { font-size: 50rpx; }
.info-section { background-color: #fff; margin: 0 30rpx; border-radius: 20rpx; padding: 40rpx 30rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.03); }
.info-header { font-size: 32rpx; font-weight: bold; color: #333; margin-bottom: 30rpx; border-left: 8rpx solid #ff6034; padding-left: 16rpx; }
/* 防挤压布局 */
.info-item { 
  display: flex; 
  align-items: flex-start; 
  margin-bottom: 24rpx; 
  font-size: 28rpx; 
  line-height: 1.5;
}
.info-item:last-child { margin-bottom: 0; }
.label { 
  color: #666; 
  width: 220rpx; 
  white-space: nowrap; 
  flex-shrink: 0; 
}
.value { 
  color: #333; 
  font-weight: bold; 
  flex: 1; 
  word-break: break-all; 
}
</style>