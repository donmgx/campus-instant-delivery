<template>
  <div class="register-container">
    <van-nav-bar title="骑手注册" left-arrow @click-left="onClickLeft" />
    
    <div class="header-text">
      <h3>加入校园递送</h3>
      <p>成为骑手，轻松赚取零花钱</p>
    </div>

    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="formData.name"
          name="name"
          label="姓名"
          placeholder="请输入真实姓名"
          :rules="[{ required: true, message: '请填写姓名' }]"
        />
        <van-field
          v-model="formData.phone"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
          :rules="[{ required: true, message: '请填写手机号' }]"
        />
        <van-field
          v-model="formData.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请设置登录密码"
          :rules="[{ required: true, message: '请设置密码' }]"
        />
      </van-cell-group>
      
      <div style="margin: 40px 16px;">
        <van-button round block type="primary" native-type="submit" :loading="loading" color="linear-gradient(to right, #ff6034, #ee0a24)">
          立即注册
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { registerAPI } from '../api/rider.js'
import { showSuccessToast } from 'vant'

const router = useRouter()
const loading = ref(false)
const formData = ref({ name: '', phone: '', password: '' })

const onClickLeft = () => {
  router.back()
}

const onSubmit = async () => {
  loading.value = true
  try {
    // 调用 RiderController 中的 /register 接口
    await registerAPI(formData.value)
    showSuccessToast('注册成功，请登录！')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container { min-height: 100vh; background-color: #f7f8fa; }
.header-text { padding: 30px 20px; }
.header-text h3 { margin: 0; font-size: 24px; color: #323233; }
.header-text p { margin-top: 8px; font-size: 14px; color: #969799; }
</style>