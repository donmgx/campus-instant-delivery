<template>
  <view class="edit-container">
    <view class="form-group">
      <view class="form-item">
        <text class="label">联系人</text>
        <input class="input" v-model="form.consignee" placeholder="请填写收货人姓名" placeholder-class="ph-color"/>
      </view>
      
      <view class="form-item">
        <text class="label">性别</text>
        <view class="radio-group">
          <view class="radio-item" :class="{ active: form.sex === '1' }" @click="form.sex = '1'">先生</view>
          <view class="radio-item" :class="{ active: form.sex === '0' }" @click="form.sex = '0'">女士</view>
        </view>
      </view>

      <view class="form-item">
        <text class="label">手机号</text>
        <input class="input" type="number" maxlength="11" v-model="form.phone" placeholder="请填写收货手机号" placeholder-class="ph-color"/>
      </view>

      <view class="form-item" style="align-items: flex-start; padding-top: 30rpx;">
        <text class="label">详细地址</text>
        <textarea class="textarea" v-model="form.detail" placeholder="例：北校区 3栋 402室" placeholder-class="ph-color" auto-height></textarea>
      </view>

      <view class="form-item">
        <text class="label">标签</text>
        <view class="tag-group">
          <text class="tag" :class="{ active: form.label === '宿舍' }" @click="form.label = '宿舍'">宿舍</text>
          <text class="tag" :class="{ active: form.label === '教室' }" @click="form.label = '教室'">教室</text>
          <text class="tag" :class="{ active: form.label === '实验室' }" @click="form.label = '实验室'">实验室</text>
        </view>
      </view>
    </view>

    <view class="default-group">
      <text class="label">设为默认地址</text>
      <switch :checked="form.isDefault === 1" color="#ff6034" @change="handleDefaultChange" />
    </view>

    <view class="btn-box">
      <button class="save-btn" @click="handleSave">保存地址</button>
      <button class="del-btn" v-if="form.id" @click="handleDelete">删除该地址</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { addAddressAPI, updateAddressAPI, deleteAddressAPI } from '../../api/address.js'

const form = ref({
  id: null,
  consignee: '',
  sex: '1',
  phone: '',
  detail: '',
  label: '宿舍',
  isDefault: 0
})

onLoad((options) => {
  if (options.item) {
    // 如果是带参数进来的，说明是修改操作
    const item = JSON.parse(decodeURIComponent(options.item))
    // 动态设置标题
    uni.setNavigationBarTitle({ title: '修改收货地址' })
    form.value = { ...item }
  }
})

const handleDefaultChange = (e) => {
  form.value.isDefault = e.detail.value ? 1 : 0
}

const handleSave = async () => {
  if (!form.value.consignee || !form.value.phone || !form.value.detail) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  
  uni.showLoading({ title: '保存中...' })
  try {
    if (form.value.id) {
      await updateAddressAPI(form.value)
    } else {
      await addAddressAPI(form.value)
    }
    uni.hideLoading()
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => { uni.navigateBack() }, 1000)
  } catch (error) {
    uni.hideLoading()
  }
}

const handleDelete = () => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这个地址吗？',
    success: async (res) => {
      if (res.confirm) {
        await deleteAddressAPI(form.value.id)
        uni.showToast({ title: '删除成功', icon: 'success' })
        setTimeout(() => { uni.navigateBack() }, 1000)
      }
    }
  })
}
</script>

<style scoped>
.edit-container { min-height: 100vh; background-color: #f7f8fa; padding-top: 20rpx; }
.form-group { background-color: #fff; padding: 0 30rpx; }
.form-item { display: flex; align-items: center; min-height: 100rpx; border-bottom: 1px solid #f5f5f5; }
.form-item:last-child { border-bottom: none; }
.label { width: 160rpx; font-size: 28rpx; color: #333; }
.input { flex: 1; font-size: 28rpx; color: #333; }
.ph-color { color: #ccc; }
.textarea { flex: 1; min-height: 80rpx; font-size: 28rpx; line-height: 1.5; padding: 10rpx 0; }

.radio-group { display: flex; }
.radio-item { padding: 6rpx 24rpx; border: 1px solid #ddd; border-radius: 8rpx; font-size: 24rpx; color: #666; margin-right: 20rpx; }
.radio-item.active { border-color: #ff6034; color: #ff6034; background-color: #fff8f5; }

.tag-group { display: flex; }
.tag { padding: 6rpx 24rpx; background-color: #f5f5f5; border-radius: 30rpx; font-size: 24rpx; color: #666; margin-right: 20rpx; }
.tag.active { background-color: #ff6034; color: #fff; }

.default-group { background-color: #fff; margin-top: 20rpx; padding: 0 30rpx; height: 100rpx; display: flex; justify-content: space-between; align-items: center; }

.btn-box { padding: 60rpx 30rpx; }
.save-btn { background: linear-gradient(135deg, #ff9e80, #ff6034); color: #fff; border-radius: 50rpx; font-size: 32rpx; height: 90rpx; line-height: 90rpx; margin-bottom: 30rpx; }
.save-btn::after { border: none; }
.del-btn { background-color: #fff; color: #666; border-radius: 50rpx; font-size: 30rpx; height: 90rpx; line-height: 90rpx; border: 1px solid #ebedf0; }
.del-btn::after { border: none; }
</style>