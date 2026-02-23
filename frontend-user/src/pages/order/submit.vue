<template>
  <view class="submit-container">
    <view class="address-box" @click="goToAddress">
      <view class="address-info" v-if="currentAddress">
        <view class="tag-row">
          <text class="tag" v-if="currentAddress.label">{{ currentAddress.label }}</text>
          <text class="detail">{{ currentAddress.detail }}</text>
        </view>
        <view class="user-row">
          <text class="name">{{ currentAddress.consignee }}</text>
          <text class="phone">{{ currentAddress.phone }}</text>
        </view>
      </view>
      <view class="no-address" v-else>
        <text class="plus">+</text> 请选择收货地址
      </view>
      <text class="arrow">></text>
    </view>

    <view class="order-detail-card">
      <view class="shop-name">校园递送自营店</view>
      <view class="goods-list">
        <view class="goods-item" v-for="item in cartList" :key="item.id">
          <image class="goods-img" :src="item.image || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'" mode="aspectFill"></image>
          <view class="goods-info">
            <view class="goods-name">{{ item.name }}</view>
            <view class="goods-bottom">
              <text class="price">￥{{ item.amount }}</text>
              <text class="num">x{{ item.number }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <view class="fee-row">
        <text>包装费</text>
        <text>￥{{ packFee }}</text>
      </view>
      <view class="fee-row">
        <text>配送费</text>
        <text>￥{{ deliveryFee }}</text>
      </view>

      <view class="fee-row coupon-row" @click="openCouponPopup">
        <text class="coupon-label">优惠券</text>
        <view class="coupon-value">
          <text v-if="selectedCoupon" class="red font-bold">-￥{{ selectedCoupon.amount }}</text>
          <text v-else-if="usableCoupons.length > 0" class="red">有 {{ usableCoupons.length }} 张可用</text>
          <text v-else class="gray">暂无可用</text>
          <text class="arrow">❯</text>
        </view>
      </view>
    </view>

    <view class="coupon-mask" v-if="showCouponPopup" @click="showCouponPopup = false"></view>
    <view class="coupon-popup" :class="{ 'show': showCouponPopup }">
      <view class="popup-header">
        <text class="popup-title">选择优惠券</text>
        <text class="close-btn" @click="showCouponPopup = false">×</text>
      </view>
      <scroll-view class="popup-scroll" scroll-y>
        <view class="empty-tip" v-if="usableCoupons.length === 0">这里空空如也，暂无满足条件的优惠券~</view>
        
        <view class="c-card" :class="{ 'active': selectedCoupon?.id === item.id }" 
              v-for="item in usableCoupons" :key="item.id" @click="handleSelectCoupon(item)">
          <view class="c-left">
            <text class="c-symbol">￥</text><text class="c-amount">{{ item.amount }}</text>
          </view>
          <view class="c-right">
            <view class="c-title">{{ item.name }}</view>
            <view class="c-desc">满{{ item.conditionAmount }}可用</view>
          </view>
          <view class="c-check" v-if="selectedCoupon?.id === item.id">✔</view>
        </view>
      </scroll-view>
      <view class="not-use-btn" @click="clearCoupon">不使用优惠券</view>
    </view>

    <view class="bottom-bar">
      <view class="price-box">
        <text class="label">合计：</text>
        <text class="rmb">￥</text>
        <text class="total">{{ finalPrice }}</text>
      </view>
      <button class="submit-btn" :loading="isSubmitting" :disabled="isSubmitting" @click="handleSubmit">
        去支付
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAddressListAPI } from '../../api/address.js'
import { getCartListAPI } from '../../api/cart.js'
import { getOrderTokenAPI, submitOrderAPI, payOrderAPI } from '../../api/order.js'
import { getMyCouponListAPI } from '../../api/coupon.js' // 引入优惠券接口

const currentAddress = ref(null)
const cartList = ref([])
const isSubmitting = ref(false)
const orderToken = ref('') 

const packFee = ref(2.00)
const deliveryFee = ref(3.00)

// 优惠券核心状态
const availableCoupons = ref([]) 
const selectedCoupon = ref(null)
const showCouponPopup = ref(false)

// 动态计算出“当前金额满足条件”的可用券
const usableCoupons = computed(() => {
  const goodsTotal = cartList.value.reduce((sum, item) => sum + (parseFloat(item.amount) || 0) * (parseInt(item.number) || 0), 0)
  return availableCoupons.value.filter(c => goodsTotal >= c.conditionAmount)
})

// 终极防线计算总价：商品 + 包装 + 配送 - 优惠券 (最低支付 0.01 元)
const finalPrice = computed(() => {
  const list = Array.isArray(cartList.value) ? cartList.value : []
  const goodsTotal = list.reduce((sum, item) => {
    const amount = parseFloat(item.amount) || 0
    const num = parseInt(item.number) || 0
    return sum + (amount * num)
  }, 0)
  
  let total = goodsTotal + packFee.value + deliveryFee.value
  
  // 扣减优惠券
  if (selectedCoupon.value) {
    total -= selectedCoupon.value.amount
  }
  
  // 兜底：防止贴钱（最低支付 1 分钱）
  return Math.max(0.01, total).toFixed(2)
})

onShow(async () => {
  await loadDefaultAddress()
  await loadCartData()
  await fetchOrderToken() 
  await loadCoupons() // 拉取优惠券
})

const loadCoupons = async () => {
  try {
    const res = await getMyCouponListAPI(0) // 1 表示拉取未使用的
    availableCoupons.value = Array.isArray(res) ? res : (res?.records || [])
    
    // 测试用假数据
    if (availableCoupons.value.length === 0) {
      availableCoupons.value = [
        { id: 1, name: '新人无门槛神券', amount: 5, conditionAmount: 0 },
        { id: 2, name: '满减大额满减券', amount: 12, conditionAmount: 50 }
      ]
    }
    
    // 默认帮用户选一张抵扣最多的
    if (usableCoupons.value.length > 0 && !selectedCoupon.value) {
      selectedCoupon.value = usableCoupons.value.reduce((max, cur) => cur.amount > max.amount ? cur : max, usableCoupons.value[0])
    }
  } catch (e) {}
}

const openCouponPopup = () => {
  if (usableCoupons.value.length > 0) {
    showCouponPopup.value = true
  } else {
    uni.showToast({ title: '暂无可用优惠券', icon: 'none' })
  }
}

const handleSelectCoupon = (coupon) => {
  selectedCoupon.value = coupon
  showCouponPopup.value = false
}

const clearCoupon = () => {
  selectedCoupon.value = null
  showCouponPopup.value = false
}

const loadDefaultAddress = async () => {
  try {
    const res = await getAddressListAPI()
    if (Array.isArray(res) && res.length > 0) {
      currentAddress.value = res.find(item => item.isDefault == 1) || res[0]
    }
  } catch (error) {}
}

const loadCartData = async () => {
  try {
    const res = await getCartListAPI()
    cartList.value = Array.isArray(res) ? res : []
  } catch (error) { cartList.value = [] }
}

const fetchOrderToken = async () => {
  try { orderToken.value = await getOrderTokenAPI() } catch (error) {}
}

const goToAddress = () => { uni.navigateTo({ url: '/pages/address/address' }) }

const handleSubmit = async () => {
  if (!currentAddress.value) return uni.showToast({ title: '请先选择收货地址', icon: 'none' })
  if (cartList.value.length === 0) return uni.showToast({ title: '购物车是空的', icon: 'none' })

  isSubmitting.value = true 
  uni.showLoading({ title: '创建订单中...', mask: true })

  try {
    const now = new Date(new Date().getTime() + 30 * 60000)
    const year = now.getFullYear(); const month = String(now.getMonth() + 1).padStart(2, '0'); const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0'); const minutes = String(now.getMinutes()).padStart(2, '0'); const seconds = String(now.getSeconds()).padStart(2, '0');
    const formattedTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;

    const submitData = {
      addressBookId: currentAddress.value.id,
      amount: finalPrice.value,
      payMethod: 1, deliveryStatus: 1, estimatedDeliveryTime: formattedTime, 
      packAmount: packFee.value, tablewareNumber: 1, tablewareStatus: 1, 
      remark: '尽快送达！', deliveryMode: 1,
      // 将优惠券 ID 交给后端进行核销
      couponId: selectedCoupon.value ? selectedCoupon.value.id : null
    }

    const orderRes = await submitOrderAPI(submitData, orderToken.value)
    
    uni.hideLoading()
    uni.showModal({
      title: '订单创建成功',
      content: `需支付 ￥${finalPrice.value}\n(模拟微信支付)`,
      confirmText: '确认支付', confirmColor: '#07c160',
      success: async (res) => {
        if (res.confirm) {
          uni.showLoading({ title: '支付中...' })
          try {
            await payOrderAPI({ orderNumber: orderRes.orderNumber, payMethod: 1 })
            uni.hideLoading()
            uni.showToast({ title: '支付成功！', icon: 'success' })
            setTimeout(() => { uni.reLaunch({ url: '/pages/index/index' }) }, 1500)
          } catch (payErr) { uni.hideLoading() }
        } else {
          uni.showToast({ title: '已取消支付', icon: 'none' })
        }
      }
    })
  } catch (error) {
    uni.hideLoading()
    await fetchOrderToken() 
  } finally {
    isSubmitting.value = false 
  }
}
</script>

<style scoped>
.submit-container { min-height: 100vh; background-color: #f7f8fa; padding: 20rpx; padding-bottom: 140rpx; }
.address-box { background-color: #fff; border-radius: 20rpx; padding: 30rpx; display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; position: relative; }
.address-box::after { content: ''; position: absolute; left: 0; right: 0; bottom: 0; height: 6rpx; background: repeating-linear-gradient(-45deg, #ff6c6c 0, #ff6c6c 20%, transparent 0, transparent 25%, #1989fa 0, #1989fa 45%, transparent 0, transparent 50%); background-size: 160rpx; border-bottom-left-radius: 20rpx; border-bottom-right-radius: 20rpx; }
.address-info { flex: 1; }
.tag-row { display: flex; align-items: center; margin-bottom: 10rpx; }
.tag { background-color: #ff6034; color: #fff; font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 8rpx; margin-right: 12rpx; }
.detail { font-size: 34rpx; font-weight: bold; color: #333; }
.user-row { font-size: 26rpx; color: #666; }
.name { margin-right: 20rpx; }
.no-address { flex: 1; font-size: 32rpx; font-weight: bold; color: #ff6034; display: flex; align-items: center; }
.plus { font-size: 40rpx; margin-right: 10rpx; margin-top: -6rpx; }
.arrow { color: #ccc; font-family: consolas; }
.order-detail-card { background-color: #fff; border-radius: 20rpx; padding: 30rpx; }
.shop-name { font-size: 28rpx; font-weight: bold; margin-bottom: 30rpx; border-bottom: 1px solid #f5f5f5; padding-bottom: 20rpx; }
.goods-item { display: flex; margin-bottom: 30rpx; }
.goods-img { width: 120rpx; height: 120rpx; border-radius: 12rpx; margin-right: 20rpx; background-color: #f5f5f5; }
.goods-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.goods-name { font-size: 28rpx; font-weight: bold; color: #333; }
.goods-bottom { display: flex; justify-content: space-between; align-items: center; }
.price { font-size: 32rpx; font-weight: bold; color: #333; }
.num { font-size: 26rpx; color: #999; }
.fee-row { display: flex; justify-content: space-between; font-size: 26rpx; color: #666; margin-top: 20rpx; }

/* 优惠券行样式 */
.coupon-row { border-top: 1px dashed #eee; padding-top: 20rpx; margin-top: 30rpx; align-items: center; }
.coupon-label { font-size: 28rpx; color: #333; font-weight: bold; }
.coupon-value { display: flex; align-items: center; }
.red { color: #ee0a24; }
.gray { color: #999; }
.font-bold { font-weight: bold; font-size: 32rpx; }
.coupon-value .arrow { margin-left: 10rpx; font-size: 24rpx; }

/* 优惠券底部弹窗 */
.coupon-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 1000; }
.coupon-popup { position: fixed; bottom: -100%; left: 0; right: 0; background: #f7f8fa; border-radius: 30rpx 30rpx 0 0; z-index: 1010; transition: all 0.3s; padding-bottom: constant(safe-area-inset-bottom); padding-bottom: env(safe-area-inset-bottom); }
.coupon-popup.show { bottom: 0; }
.popup-header { display: flex; justify-content: space-between; align-items: center; padding: 30rpx 40rpx; background: #fff; border-radius: 30rpx 30rpx 0 0; }
.popup-title { font-size: 32rpx; font-weight: bold; color: #333; }
.close-btn { font-size: 44rpx; color: #999; line-height: 1; }
.popup-scroll { max-height: 60vh; padding: 30rpx; box-sizing: border-box; }
.empty-tip { text-align: center; color: #999; padding: 60rpx 0; font-size: 28rpx; }

.c-card { display: flex; background: #fff; border-radius: 16rpx; margin-bottom: 20rpx; padding: 30rpx; border: 2rpx solid transparent; transition: all 0.2s; position: relative; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.02); }
.c-card.active { border-color: #ff6034; background-color: #fff8f5; }
.c-left { width: 140rpx; display: flex; align-items: baseline; color: #ff6034; }
.c-symbol { font-size: 32rpx; font-weight: bold; }
.c-amount { font-size: 60rpx; font-weight: bold; }
.c-right { flex: 1; padding-left: 20rpx; display: flex; flex-direction: column; justify-content: center; }
.c-title { font-size: 30rpx; font-weight: bold; color: #333; margin-bottom: 8rpx; }
.c-desc { font-size: 24rpx; color: #999; }
.c-check { position: absolute; right: 30rpx; top: 50%; transform: translateY(-50%); width: 40rpx; height: 40rpx; background: #ff6034; border-radius: 50%; color: #fff; text-align: center; line-height: 40rpx; font-size: 24rpx; font-weight: bold; }

.not-use-btn { margin: 20rpx 30rpx 40rpx; background: #fff; color: #666; text-align: center; height: 80rpx; line-height: 80rpx; border-radius: 40rpx; font-size: 28rpx; box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.03); }

.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; height: 110rpx; background-color: #fff; display: flex; justify-content: space-between; align-items: center; padding: 0 30rpx 40rpx; box-shadow: 0 -4rpx 10rpx rgba(0,0,0,0.05); z-index: 999; }
.price-box { display: flex; align-items: baseline; }
.label { font-size: 26rpx; color: #333; }
.rmb { font-size: 28rpx; color: #ee0a24; font-weight: bold; }
.total { font-size: 44rpx; color: #ee0a24; font-weight: bold; }
.submit-btn { width: 220rpx; height: 80rpx; border-radius: 40rpx; background: linear-gradient(135deg, #ff9e80, #ff6034); color: #fff; font-size: 30rpx; font-weight: bold; line-height: 80rpx; margin: 0; }
.submit-btn::after { border: none; }
</style>