<template>
  <view class="address-container">
    <view class="empty-tip" v-if="addressList.length === 0">
      <text>还没有添加过校园地址哦~</text>
    </view>

    <view class="address-list" v-else>
      <view class="address-card" v-for="item in addressList" :key="item.id">
        <view class="info-area">
          <view class="location">
            <text class="tag" v-if="item.label">{{ item.label }}</text>
            <text class="detail">{{ item.detail }}</text>
          </view>
          <view class="user">
            <text class="name">{{ item.consignee }}</text>
            <text class="sex">{{ item.sex === '1' ? '先生' : '女士' }}</text>
            <text class="phone">{{ item.phone }}</text>
          </view>
        </view>
        
        <view class="action-area">
          <text class="edit-btn" @click="goToEdit(item)">修改</text>
        </view>
      </view>
    </view>

    <view class="bottom-btn-box">
      <button class="add-btn" @click="goToAdd">
        <text class="plus">+</text> 新增收货地址
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAddressListAPI } from '../../api/address.js'

const addressList = ref([])

onShow(() => {
  loadAddressList()
})

const loadAddressList = async () => {
  try {
    const res = await getAddressListAPI()
    addressList.value = res || []
  } catch (error) {
    console.error('获取地址失败')
  }
}

const goToAdd = () => {
  uni.navigateTo({ url: '/pages/address/edit' })
}

const goToEdit = (item) => {
  // 将整个地址对象序列化后传给编辑页
  uni.navigateTo({ url: `/pages/address/edit?item=${encodeURIComponent(JSON.stringify(item))}` })
}
</script>

<style scoped>
.address-container { min-height: 100vh; background-color: #f7f8fa; padding: 20rpx; padding-bottom: 140rpx; }
.empty-tip { text-align: center; color: #999; padding-top: 200rpx; font-size: 28rpx; }

.address-card { background-color: #fff; border-radius: 16rpx; padding: 30rpx; margin-bottom: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.info-area { flex: 1; padding-right: 20rpx; }
.location { display: flex; align-items: center; margin-bottom: 16rpx; }
.tag { background-color: #e8f3fe; color: #1989fa; font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 8rpx; margin-right: 12rpx; white-space: nowrap; }
.detail { font-size: 32rpx; font-weight: bold; color: #333; }
.user { font-size: 26rpx; color: #666; }
.user text { margin-right: 20rpx; }

.action-area { border-left: 1px solid #eee; padding-left: 30rpx; }
.edit-btn { color: #999; font-size: 26rpx; }

.bottom-btn-box { position: fixed; bottom: 0; left: 0; right: 0; padding: 20rpx 30rpx 60rpx; background-color: #fff; box-shadow: 0 -4rpx 10rpx rgba(0,0,0,0.05); }
.add-btn { background: linear-gradient(135deg, #ff9e80, #ff6034); color: #fff; border-radius: 50rpx; font-size: 32rpx; font-weight: bold; height: 90rpx; display: flex; justify-content: center; align-items: center; }
.add-btn::after { border: none; }
.plus { font-size: 40rpx; margin-right: 10rpx; margin-top: -6rpx; }
</style>