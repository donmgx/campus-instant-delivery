<template>
  <div class="login-container">
    <van-nav-bar title="骑手登录" />
    
    <div class="logo-area">
      <img src="https://fastly.jsdelivr.net/npm/@vant/assets/logo.png" alt="logo" class="logo" />
      <h2>校园递送 - 骑手端</h2>
    </div>

    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="phone"
          name="phone"
          label="手机号"
          placeholder="请输入注册手机号"
          :rules="[{ required: true, message: '请填写手机号' }]"
        />
        <van-field
          v-model="password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请填写密码' }]"
        />
      </van-cell-group>
      
      <div style="margin: 30px 16px;">
        <van-button 
          round 
          block 
          type="primary" 
          native-type="submit" 
          :loading="loading" 
          color="linear-gradient(to right, #ff6034, #ee0a24)"
        >
          立即登录
        </van-button>
        
        <div class="register-link" @click="goToRegister">
          还没有账号？点击这里注册
        </div>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { loginAPI } from '../api/rider.js'
import { showSuccessToast, showToast } from 'vant'

const router = useRouter()
const phone = ref('')
const password = ref('')
const loading = ref(false)

const onSubmit = async (values) => {
  loading.value = true
  try {
    const submitData = {
      phone: values.phone,
      password: values.password
    }
    const res = await loginAPI(submitData)

    console.log('后端登录接口返回的数据：', res)
    
    // 防错校验：如果 res 里面没有 token 字段，说明后端的属性名可能不叫 token
    if (!res || !res.token) {
      showFailToast('登录失败：后端没有返回 Token，请按 F12 查看控制台日志！')
      return; // 终止执行，防止存入 undefined 导致闪退
    }

    localStorage.setItem('token', res.token)
    localStorage.setItem('riderInfo', JSON.stringify(res))
    
    showSuccessToast('登录成功')
    router.push('/hall')
  } catch (error) {
    // 错误处理
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  showToast(router.push('/register'))
}
</script>

<style scoped>
.login-container { 
  min-height: 100vh; 
  background-color: #f7f8fa; 
}
.logo-area { 
  text-align: center; 
  padding: 50px 0 30px 0; 
}
.logo { 
  width: 80px; 
  height: 80px; 
}
h2 { 
  margin-top: 15px; 
  font-size: 22px; 
  color: #323233; 
  font-weight: bold;
}
.register-link { 
  text-align: center; 
  margin-top: 20px; 
  color: #1989fa; 
  font-size: 14px; 
}
</style>