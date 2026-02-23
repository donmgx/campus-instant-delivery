<template>
  <view class="menu-container">
    <view class="search-header">
      <view class="search-input-box">
        <text class="icon">🔍</text>
        <input class="input" v-model="searchKeyword" placeholder="搜搜想吃什么..." @input="handleSearchInput" :focus="isSearchFocus" />
        <text class="clear" v-if="searchKeyword" @click="clearSearch">✖</text>
      </view>
    </view>

    <view class="content-body">
      <scroll-view class="category-scroll" scroll-y v-show="!isSearching">
        <view class="category-item" :class="{ active: currentCategory.id === item.id }"
              v-for="item in categoryList" :key="item.id" @click="handleSelectCategory(item)">
          {{ item.name }}
        </view>
      </scroll-view>

      <scroll-view class="goods-scroll" :class="{ 'full-width': isSearching }" scroll-y>
        <view class="goods-header">
          <text class="title">{{ isSearching ? `搜索结果 (${searchResults.length})` : currentCategory.name }}</text>
        </view>
        
        <view v-if="(isSearching ? searchResults : goodsList).length === 0" class="empty-tip">
          {{ isSearching ? '没有搜到相关的菜品哦~' : '暂无商品上架~' }}
        </view>
        
        <view class="goods-list" v-else>
          <view class="goods-card" v-for="goods in (isSearching ? searchResults : goodsList)" :key="goods.id" @click="openDetail(goods)">
            <image class="goods-img" :src="goods.image || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'" mode="aspectFill"></image>
            <view class="goods-info">
              <view class="goods-name">{{ goods.name }}</view>
              <view class="goods-desc">{{ goods.description || '美味尽在校园递送' }}</view>
              <view class="goods-bottom">
                <text class="price">￥{{ goods.price }}</text>
                <view class="add-btn" @click.stop="handleAddToCart(goods)"><text class="plus">+</text></view>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="detail-mask" v-if="showDetailPopup" @click="showDetailPopup = false"></view>
    <view class="detail-popup" :class="{ 'show': showDetailPopup }">
      <view class="close-btn" @click="showDetailPopup = false">×</view>
      <image class="detail-img" :src="currentGoods.image || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'" mode="aspectFill"></image>
      <view class="detail-content">
        <view class="detail-name">{{ currentGoods.name }}</view>
        <view class="detail-sales">近期好评如潮，校园人气单品 🔥</view>
        <view class="detail-desc">{{ currentGoods.description || '这道菜很神秘，老板什么都没写~ 建议直接点一份尝尝！' }}</view>
        <view class="detail-bottom">
          <text class="detail-price">￥{{ currentGoods.price }}</text>
          <button class="detail-add-btn" @click="handleAddToCart(currentGoods)">加入购物车</button>
        </view>
      </view>
    </view>

    <view class="cart-mask" v-if="showCartPopup" @click="toggleCartPopup"></view>
    <view class="cart-popup" :class="{ 'show': showCartPopup }">
      <view class="popup-header">
        <text class="popup-title">已选商品</text>
        <view class="clear-btn" @click="handleClearCart"><text class="icon">🗑️</text> 清空</view>
      </view>
      <scroll-view class="popup-scroll" scroll-y>
        <view class="cart-item" v-for="item in cartList" :key="item.id">
          <image class="item-img" :src="item.image || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'" mode="aspectFill"></image>
          <view class="item-info">
            <view class="item-name">{{ item.name }}</view>
            <view class="item-price">￥{{ item.amount }}</view>
          </view>
          <view class="item-ctrl">
            <view class="ctrl-btn minus" @click="handleSubCart(item)">-</view>
            <text class="item-num">{{ item.number > 0 ? item.number : 0 }}</text>
            <view class="ctrl-btn plus" @click="handleAddCartItem(item)">+</view>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="cart-bar">
      <view class="cart-icon-box" @click="toggleCartPopup">
        <text class="cart-icon">🛍️</text>
        <view class="badge" v-if="totalNum > 0">{{ totalNum }}</view>
      </view>
      <view class="price-box" @click="toggleCartPopup">
        <text class="rmb">￥</text>
        <text class="total-price">{{ totalPrice }}</text>
      </view>
      <view class="submit-btn" :class="{ active: totalNum > 0 }" @click="goToCheckout">去结算</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getCategoryListAPI, getDishListAPI, getSetmealListAPI, searchGoodsAPI } from '../../api/goods.js'
import { getCartListAPI, addCartAPI, subCartAPI, clearCartAPI } from '../../api/cart.js'

// 基础数据
const categoryList = ref([])
const goodsList = ref([])
const currentCategory = ref({})
const cartList = ref([]) 
const showCartPopup = ref(false) 
const showDetailPopup = ref(false)
const currentGoods = ref({})

//  ES 搜索模块核心变量
const searchKeyword = ref('')
const searchResults = ref([])
const isSearchFocus = ref(false)
// 只要输入框里有字，就自动进入“搜索模式”
const isSearching = computed(() => searchKeyword.value.trim().length > 0)

const totalNum = computed(() => cartList.value.reduce((sum, item) => sum + Math.max(0, item.number), 0))
const totalPrice = computed(() => cartList.value.reduce((sum, item) => sum + (item.amount * Math.max(0, item.number)), 0).toFixed(2))

onShow(async () => {
  // 检查首页是否传来了自动聚焦的指令
  if (uni.getStorageSync('autoSearchFocus')) {
    isSearchFocus.value = true
    uni.removeStorageSync('autoSearchFocus')
  } else {
    isSearchFocus.value = false
  }

  await loadCategory()
  await loadCart() 
})

// ES 防抖搜索核心逻辑
let searchTimer = null
const handleSearchInput = () => {
  const kw = searchKeyword.value.trim()
  if (!kw) {
    searchResults.value = []
    return
  }
  
  // 清除上一次的计时器，重新倒数 500 毫秒（防抖）
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    try {
      // 触发后端的 ES 检索接口
      const res = await searchGoodsAPI(kw)
      searchResults.value = res || []
    } catch (error) {
      searchResults.value = []
    }
  }, 500)
}

// 清空搜索框，退出搜索模式
const clearSearch = () => {
  searchKeyword.value = ''
  searchResults.value = []
}

const loadCategory = async () => {
  try {
    const res = await getCategoryListAPI()
    if (res && res.length > 0) {
      categoryList.value = res
      handleSelectCategory(res[0])
    }
  } catch (error) {}
}

const handleSelectCategory = async (category) => {
  currentCategory.value = category
  uni.showLoading({ title: '加载中...' })
  try {
    if (category.type === 1) {
      goodsList.value = await getDishListAPI(category.id)
    } else {
      goodsList.value = await getSetmealListAPI(category.id)
    }
  } catch (error) { goodsList.value = [] } finally { uni.hideLoading() }
}

const loadCart = async () => {
  try {
    const res = await getCartListAPI()
    cartList.value = Array.isArray(res) ? res.filter(item => item.number > 0) : []
    if (cartList.value.length === 0) showCartPopup.value = false
  } catch (error) { cartList.value = [] }
}

const openDetail = (goods) => {
  currentGoods.value = goods
  showDetailPopup.value = true
}

const toggleCartPopup = async () => {
  if (!showCartPopup.value) {
    uni.showLoading({ title: '核对数据中...', mask: true })
    await loadCart()
    uni.hideLoading()
    if (totalNum.value > 0) showCartPopup.value = true
    else uni.showToast({ title: '购物车空空如也，先点些吃的吧~', icon: 'none' })
  } else {
    showCartPopup.value = false
  }
}

// 复用现有的加入购物车逻辑
// 精准判断加入购物车的是菜品还是套餐
const handleAddToCart = async (goods) => {
  uni.showLoading({ title: '加入中...' })
  try {
    const postData = {}
    
    // 逻辑分流：判断用户当前是在搜索还是在看分类
    if (isSearching.value) {
      // 1. 搜索模式：如果后端明确返回了 type=2 则是套餐
      if (goods.type === 2) {
        postData.setmealId = goods.id
      } else {
        postData.dishId = goods.id
      }
    } else {
      // 2. 分类模式：严格根据左侧选中分类的 type 来决定(1: 菜品, 2: 套餐)
      if (currentCategory.value.type === 2) {
        postData.setmealId = goods.id
      } else {
        postData.dishId = goods.id
      }
    }

    await addCartAPI(postData)
    await loadCart() 
    
    // 如果是从详情弹窗里点加号进来的，加完自动关掉弹窗
    if (showDetailPopup.value) showDetailPopup.value = false
    
    uni.hideLoading()
    uni.showToast({ title: '已加入', icon: 'success' })
  } catch (error) { 
    uni.hideLoading() 
  }
}

const handleAddCartItem = async (item) => {
  uni.showLoading({ title: '处理中...' })
  try {
    const postData = {}
    if (item.dishId) postData.dishId = item.dishId
    if (item.setmealId) postData.setmealId = item.setmealId
    await addCartAPI(postData)
    await loadCart()
    uni.hideLoading()
  } catch (error) { uni.hideLoading() }
}

const handleSubCart = async (item) => {
  uni.showLoading({ title: '处理中...' })
  try {
    const postData = {}
    if (item.dishId) postData.dishId = item.dishId
    if (item.setmealId) postData.setmealId = item.setmealId
    await subCartAPI(postData)
    await loadCart() 
    uni.hideLoading()
  } catch (error) { uni.hideLoading() }
}

const handleClearCart = () => {
  uni.showModal({
    title: '温馨提示',
    content: '确定要清空购物车吗？',
    confirmColor: '#ee0a24',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '清空中...' })
        try {
          await clearCartAPI()
          await loadCart()
          showCartPopup.value = false
          uni.hideLoading()
        } catch (error) { uni.hideLoading() }
      }
    }
  })
}

const goToCheckout = () => {
  if (totalNum.value <= 0) {
    uni.showToast({ title: '购物车还是空的哦', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/order/submit' })
}
</script>

<style scoped>
/* 整体布局变为纵向 Flex，保证搜索框固定在顶部 */
.menu-container { display: flex; flex-direction: column; height: 100vh; padding-bottom: 150rpx; box-sizing: border-box; background-color: #fff; position: relative; }

/* 搜索栏样式 */
.search-header { padding: 20rpx 30rpx; background-color: #fff; border-bottom: 1px solid #f5f5f5; z-index: 100; }
.search-input-box { background-color: #f2f3f5; height: 72rpx; border-radius: 36rpx; display: flex; align-items: center; padding: 0 30rpx; }
.search-input-box .icon { font-size: 32rpx; margin-right: 16rpx; color: #999; }
.search-input-box .input { flex: 1; font-size: 28rpx; color: #333; }
.search-input-box .clear { width: 40rpx; height: 40rpx; line-height: 40rpx; text-align: center; color: #fff; background: #ccc; border-radius: 50%; font-size: 24rpx; margin-left: 20rpx; }

/* 主体内容区，填满剩余空间 */
.content-body { display: flex; flex: 1; overflow: hidden; }

/* 侧边栏及右侧商品列表样式 */
.category-scroll { width: 200rpx; background-color: #f7f8fa; height: 100%; flex-shrink: 0; }
.category-item { height: 100rpx; line-height: 100rpx; text-align: center; font-size: 26rpx; color: #666; position: relative; }
.category-item.active { background-color: #fff; color: #323233; font-weight: bold; }
.category-item.active::before { content: ''; position: absolute; left: 0; top: 50%; transform: translateY(-50%); width: 8rpx; height: 36rpx; background-color: #ff6034; border-radius: 0 4rpx 4rpx 0; }

.goods-scroll { flex: 1; height: 100%; padding: 0 20rpx; }
/* 处于搜索状态时，右侧列表霸占全屏 */
.goods-scroll.full-width { padding: 0 30rpx; }

.goods-header { padding: 20rpx 0; } .goods-header .title { font-size: 28rpx; font-weight: bold; color: #323233; }
.empty-tip { text-align: center; color: #999; margin-top: 100rpx; font-size: 26rpx; }
.goods-card { display: flex; margin-bottom: 30rpx; background: #fff; }
.goods-img { width: 180rpx; height: 180rpx; border-radius: 12rpx; margin-right: 20rpx; background-color: #f2f2f2; }
.goods-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; padding: 6rpx 0; }
.goods-name { font-size: 30rpx; font-weight: bold; color: #323233; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.goods-desc { font-size: 22rpx; color: #999; margin-top: 8rpx; }
.goods-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 20rpx; }
.price { font-size: 34rpx; font-weight: bold; color: #ee0a24; }
.add-btn { width: 48rpx; height: 48rpx; background: linear-gradient(135deg, #ff9e80, #ff6034); border-radius: 50%; display: flex; justify-content: center; align-items: center; box-shadow: 0 4rpx 8rpx rgba(255, 96, 52, 0.3); } .plus { color: #fff; font-size: 36rpx; font-weight: bold; margin-top: -4rpx; }

/* 商品详情弹窗 */
.detail-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); z-index: 1000; }
.detail-popup { position: fixed; top: 50%; left: 10vw; right: 10vw; transform: translateY(-50%) scale(0.9); background: #fff; border-radius: 24rpx; z-index: 1010; overflow: hidden; opacity: 0; pointer-events: none; transition: all 0.3s cubic-bezier(0.18, 0.89, 0.32, 1.28); }
.detail-popup.show { opacity: 1; pointer-events: auto; transform: translateY(-50%) scale(1); }
.close-btn { position: absolute; top: 20rpx; right: 20rpx; width: 60rpx; height: 60rpx; background: rgba(0,0,0,0.4); color: #fff; border-radius: 50%; text-align: center; line-height: 54rpx; font-size: 40rpx; z-index: 10; }
.detail-img { width: 100%; height: 400rpx; background: #f5f5f5; }
.detail-content { padding: 40rpx 30rpx; }
.detail-name { font-size: 38rpx; font-weight: bold; color: #333; margin-bottom: 10rpx; }
.detail-sales { font-size: 24rpx; color: #ff6034; margin-bottom: 20rpx; }
.detail-desc { font-size: 26rpx; color: #666; margin-bottom: 40rpx; line-height: 1.6; }
.detail-bottom { display: flex; justify-content: space-between; align-items: center; }
.detail-price { font-size: 44rpx; font-weight: bold; color: #ee0a24; }
.detail-add-btn { background: linear-gradient(135deg, #ff9e80, #ff6034); color: #fff; font-size: 28rpx; height: 70rpx; line-height: 70rpx; border-radius: 40rpx; padding: 0 40rpx; margin: 0; font-weight: bold; }
.detail-add-btn::after { border: none; }

/* 购物车遮罩层与弹窗 */
.cart-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background-color: rgba(0, 0, 0, 0.5); z-index: 900; transition: opacity 0.3s; }
.cart-popup { position: fixed; left: 0; right: 0; bottom: -100%; background-color: #fff; border-radius: 40rpx 40rpx 0 0; padding-bottom: 140rpx; z-index: 950; transition: bottom 0.3s ease-out; }
.cart-popup.show { bottom: 0; }
.popup-header { display: flex; justify-content: space-between; align-items: center; padding: 30rpx 40rpx; border-bottom: 1px solid #f5f5f5; width: 100%; box-sizing: border-box; }
.popup-title { font-size: 32rpx; font-weight: bold; color: #333; }
.clear-btn { font-size: 26rpx; color: #999; display: flex; align-items: center; }
.popup-scroll { max-height: 55vh; width: 100%; padding: 0 40rpx; box-sizing: border-box; }
.cart-item { display: flex; align-items: center; padding: 30rpx 0; border-bottom: 1px solid #f9f9f9; width: 100%; box-sizing: border-box; }
.item-img { width: 100rpx; height: 100rpx; border-radius: 12rpx; margin-right: 20rpx; background-color: #f5f5f5; flex-shrink: 0; }
.item-info { flex: 1; overflow: hidden; padding-right: 20rpx; }
.item-name { font-size: 28rpx; font-weight: bold; color: #333; margin-bottom: 10rpx; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-price { font-size: 32rpx; font-weight: bold; color: #ee0a24; }
.item-ctrl { display: flex; align-items: center; flex-shrink: 0; }
.ctrl-btn { width: 44rpx; height: 44rpx; border-radius: 50%; display: flex; justify-content: center; align-items: center; font-size: 30rpx; font-weight: bold; }
.minus { border: 2rpx solid #ccc; color: #666; background-color: #fff; }
.plus { background: #ff6034; color: #fff; }
.item-num { font-size: 28rpx; color: #333; width: 60rpx; text-align: center; }

/* 底部悬浮黑条 */
.cart-bar { position: fixed; left: 30rpx; right: 30rpx; bottom: 20rpx; height: 100rpx; background-color: #333333; border-radius: 50rpx; display: flex; align-items: center; z-index: 999; box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.2); }
.cart-icon-box { position: relative; width: 100rpx; height: 100rpx; background-color: #ff6034; border-radius: 50%; display: flex; justify-content: center; align-items: center; margin-left: 20rpx; margin-top: -30rpx; border: 8rpx solid #333; }
.cart-icon { font-size: 40rpx; }
.badge { position: absolute; top: -10rpx; right: -10rpx; background-color: #ee0a24; color: #fff; font-size: 20rpx; padding: 2rpx 10rpx; border-radius: 20rpx; font-weight: bold; }
.price-box { flex: 1; padding-left: 20rpx; color: #fff; }
.rmb { font-size: 24rpx; }
.total-price { font-size: 36rpx; font-weight: bold; }
.submit-btn { width: 180rpx; height: 100%; background-color: #555; color: #999; border-radius: 0 50rpx 50rpx 0; display: flex; justify-content: center; align-items: center; font-size: 30rpx; font-weight: bold; }
.submit-btn.active { background: linear-gradient(135deg, #ff9e80, #ff6034); color: #fff; }
</style>