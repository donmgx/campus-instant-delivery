<template>
  <div class="layout">
    <router-view />

    <van-tabbar route safe-area-inset-bottom active-color="#ee0a24" inactive-color="#7d7e80">
      <van-tabbar-item replace to="/hall" icon="shop-collect-o">抢单大厅</van-tabbar-item>
      <van-tabbar-item replace to="/running" icon="logistics">配送中</van-tabbar-item>
      <van-tabbar-item replace to="/profile" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
// 这里什么都不需要了，交给路由自动匹配高亮
</script>

<style scoped>
.layout {
  padding-bottom: 50px; /* 防止底部导航栏遮挡内容 */
}
</style>