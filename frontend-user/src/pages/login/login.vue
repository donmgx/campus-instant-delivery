<template>
  <view class="login-container">
    <view class="logo-box">
      <image class="logo" src="https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg" mode="aspectFill"></image>
      <text class="title">校园递送</text>
      <text class="subtitle">更懂你的校园外卖</text>
    </view>

    <view class="btn-group">
      <button class="login-btn" @click="handleWechatLogin">
        <text class="btn-text">微信一键登录</text>
      </button>
      <button class="cancel-btn" @click="handleCancel">暂不登录</button>
    </view>
  </view>
</template>

<script setup>
import { wxLoginAPI } from '../../api/user.js'

// 处理微信授权登录
const handleWechatLogin = () => {
  uni.showLoading({ title: '登录中...' })
  
  // 1. 调用微信原生 API 获取 code
  uni.login({
    provider: 'weixin',
    success: async (loginRes) => {
      if (loginRes.code) {
        try {
          // 2. 将 code 发给 Java 后端换取 Token
          const res = await wxLoginAPI({ code: loginRes.code })
          
          // 3. 将后端返回的 token 和 openid 存入本地
          uni.setStorageSync('token', res.token)
          uni.setStorageSync('userInfo', JSON.stringify(res))
          
          uni.hideLoading()
          uni.showToast({ title: '登录成功', icon: 'success' })
          
          // 4. 跳转回首页
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/index/index' })
          }, 1000)
          
        } catch (error) {
          uni.hideLoading()
          // 报错已经在 request.js 拦截器处理过了
        }
      }
    },
    fail: () => {
      uni.hideLoading()
      uni.showToast({ title: '获取微信授权失败', icon: 'none' })
    }
  })
}

const handleCancel = () => {
  uni.showToast({ title: '只有登录才能点外卖哦~', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #ff6034 0%, #ff9e80 40%, #f7f8fa 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
}

.logo-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 120rpx;

  .logo {
    width: 180rpx;
    height: 180rpx;
    border-radius: 50%;
    border: 6rpx solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
  }

  .title {
    font-size: 48rpx;
    font-weight: bold;
    color: #ffffff;
    margin-top: 30rpx;
  }

  .subtitle {
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.9);
    margin-top: 10rpx;
  }
}

.btn-group {
  width: 100%;
  padding: 0 60rpx;
  box-sizing: border-box;

  .login-btn {
    background-color: #07c160; /* 微信绿 */
    border-radius: 50rpx;
    height: 90rpx;
    display: flex;
    justify-content: center;
    align-items: center;
    box-shadow: 0 8rpx 16rpx rgba(7, 193, 96, 0.3);

    .btn-text {
      color: #ffffff;
      font-size: 32rpx;
      font-weight: bold;
    }
  }

  .cancel-btn {
    background-color: transparent;
    color: #666;
    font-size: 28rpx;
    margin-top: 30rpx;
  }
  .cancel-btn::after {
    border: none;
  }
}
</style>