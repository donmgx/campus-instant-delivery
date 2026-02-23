<script>
export default {
  onLaunch: function () {
    console.log('App Launch - 校园递送用户端启动！')
  }
}
</script>

<style lang="scss">
/* 引入全局 SCSS 变量 */
@import '@/uni.scss';

/* 全局基础样式 */
page {
  background-color: $campus-bg-color;
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica, Segoe UI, Arial, Roboto, 'PingFang SC', sans-serif;
  font-size: 28rpx;
  color: $campus-text-main;
}

/* 隐藏小程序默认的滚动条，提升颜值 */
::-webkit-scrollbar {
  display: none;
  width: 0 !important;
  height: 0 !important;
  -webkit-appearance: none;
  background: transparent;
}
</style>