<template>
  <div class="profile-container">
    <div class="header-bg">
      <div class="user-info">
        <van-image round width="4rem" height="4rem" src="https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg" />
        <div class="info-text">
          <div class="name">{{ riderInfo.name || '校园骑手' }}</div>
          <div class="phone">{{ riderInfo.phone || '138****0000' }}</div>
        </div>
      </div>
    </div>

    <div class="stats-panel">
      <div class="stats-title">今日跑单收入 (元)</div>
      <div class="stats-money">{{ todayIncome }}</div>
    </div>

    <van-cell-group inset style="margin-top: 20px;">
      <van-cell title="历史账单" is-link icon="balance-list-o" />
      <van-cell title="联系客服" is-link icon="service-o" />
      <van-cell title="修改密码" is-link icon="setting-o" @click="showPwdDialog = true" />
    </van-cell-group>

    <div style="margin: 30px 16px;">
      <van-button block round type="default" @click="handleLogout">退出登录</van-button>
    </div>

    <van-dialog 
      v-model:show="showPwdDialog" 
      title="修改密码" 
      show-cancel-button 
      @confirm="confirmUpdatePwd"
      :before-close="onBeforeClose"
    >
      <div style="padding: 16px 0;">
        <van-field 
          v-model="newPassword" 
          type="password" 
          label="新密码" 
          placeholder="请输入新密码" 
          input-align="right"
          :rules="[{ required: true, message: '密码不能为空' }]"
        />
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast, showFailToast } from 'vant'
import { getStatsAPI, updateRiderAPI } from '../api/rider.js'

const router = useRouter()

// 安全解析骑手信息，防止 JSON.parse 崩溃
let initRiderInfo = {}
try {
  const localStr = localStorage.getItem('riderInfo')
  if (localStr && localStr !== 'undefined' && localStr !== 'null') {
    initRiderInfo = JSON.parse(localStr)
  }
} catch (error) {
  console.error('骑手信息解析失败，本地缓存异常：', error)
}
const riderInfo = ref(initRiderInfo)

const todayIncome = ref('0.00')

// 修改密码相关状态
const showPwdDialog = ref(false)
const newPassword = ref('')

// 获取当天日期格式化 (yyyy-MM-dd)
const getTodayStr = () => {
  const date = new Date()
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

// 获取今日战绩
const loadStats = async () => {
  try {
    const today = getTodayStr()
    const res = await getStatsAPI(today, today)
    todayIncome.value = res ? Number(res).toFixed(2) : '0.00'
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 退出登录
const handleLogout = () => {
  showConfirmDialog({ title: '提示', message: '确定要退出登录吗？' }).then(() => {
    localStorage.clear()
    router.push('/login')
  }).catch(() => {})
}

// 拦截弹窗关闭，防止密码为空时点确认也关掉
const onBeforeClose = (action) => {
  if (action === 'confirm' && !newPassword.value) {
    showFailToast('密码不能为空')
    return false
  }
  return true
}

// 确认修改密码
const confirmUpdatePwd = async () => {
  try {
    const submitData = {
      id: riderInfo.value.id, // 必须传骑手ID
      password: newPassword.value
    }
    await updateRiderAPI(submitData)
    showSuccessToast('密码修改成功，请重新登录！')
    
    // 清除旧的登录状态，强制跳回登录页
    localStorage.clear()
    router.push('/login')
  } catch (error) {
    // 报错拦截器会处理
  } finally {
    newPassword.value = '' 
  }
}

onMounted(() => loadStats())
</script>

<style scoped>
.profile-container { min-height: 100vh; background-color: #f7f8fa; padding-bottom: 60px; }
.header-bg { background: linear-gradient(to right, #ff6034, #ee0a24); padding: 50px 20px 70px 20px; color: white; border-bottom-left-radius: 20px; border-bottom-right-radius: 20px; }
.user-info { display: flex; align-items: center; }
.info-text { margin-left: 15px; }
.name { font-size: 20px; font-weight: bold; margin-bottom: 5px; }
.phone { font-size: 14px; opacity: 0.9; }

.stats-panel { background: white; border-radius: 12px; padding: 20px; margin: -40px 16px 0 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); text-align: center; position: relative; z-index: 10; }
.stats-title { font-size: 14px; color: #969799; margin-bottom: 10px; }
.stats-money { font-size: 36px; font-weight: bold; color: #ee0a24; }
</style>