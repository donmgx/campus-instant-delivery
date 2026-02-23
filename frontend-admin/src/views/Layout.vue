<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo-title">校园递送 Admin</div>
      <el-menu active-text-color="#ffd04b" background-color="#304156" text-color="#bfcbd9" router :default-active="$route.path">
        <el-menu-item index="/dashboard"><el-icon><DataBoard /></el-icon><span>工作台</span></el-menu-item>
        <el-menu-item index="/employee"><el-icon><User /></el-icon><span>员工管理</span></el-menu-item>
        <el-menu-item index="/order"><el-icon><List /></el-icon><span>订单管理</span></el-menu-item>
        <el-menu-item index="/dish"><el-icon><Food /></el-icon><span>菜品管理</span></el-menu-item>
        <el-menu-item index="/setmeal"><el-icon><Box /></el-icon><span>套餐管理</span></el-menu-item>
        <el-menu-item index="/category"><el-icon><Box /></el-icon><span>分类管理</span></el-menu-item>
        <el-menu-item index="/coupon"><el-icon><Ticket /></el-icon><span>优惠券管理</span></el-menu-item>
        <el-menu-item index="/report"><el-icon><PieChart /></el-icon><span>数据统计</span></el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span style="font-weight: bold; font-size: 16px;">欢迎回来，{{ userInfo.name || userInfo.username || '管理员' }}！</span>
        </div>
        
        <div class="header-right">
          <div class="shop-status">
            <span style="margin-right: 10px; font-size: 14px; color: #606266;">营业状态:</span>
            <el-switch
              v-model="shopStatus"
              :active-value="1"
              :inactive-value="0"
              active-text="营业中"
              inactive-text="打烊中"
              inline-prompt
              style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
              @change="handleShopStatusChange"
            />
          </div>

          <el-dropdown @command="handleCommand" trigger="click">
            <span class="el-dropdown-link user-dropdown">
              <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
              <span style="margin-left: 8px;">{{ userInfo.name || userInfo.username || '管理员' }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="editPassword">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view /> 
      </el-main>
    </el-container>

    <el-dialog title="修改密码" v-model="pwdDialogVisible" width="400px" destroy-on-close>
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPwd">确认修改</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { logoutAPI, editPasswordAPI } from '../api/employee'
import { getShopStatusAPI, setShopStatusAPI } from '../api/shop'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Lock, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()

// --- 1. 用户信息与营业状态 ---
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
const shopStatus = ref(1) // 默认 1:营业中, 0:打烊中

// 获取店铺当前状态
const fetchShopStatus = async () => {
  try {
    const res = await getShopStatusAPI()
    shopStatus.value = res // 后端返回 1 或 0
  } catch (error) {
    console.error('获取店铺状态失败')
  }
}

// 切换店铺状态
const handleShopStatusChange = async (val) => {
  try {
    await setShopStatusAPI(val)
    ElMessage.success(`店铺已${val === 1 ? '开启营业' : '打烊'}`)
  } catch (error) {
    // 如果接口报错，把开关状态回滚回去
    shopStatus.value = val === 1 ? 0 : 1
  }
}

// --- 2. 下拉菜单指令分发 ---
const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'editPassword') {
    pwdDialogVisible.value = true
    // 清空上次填写的表单
    if (pwdFormRef.value) pwdFormRef.value.resetFields()
  }
}

// 退出登录逻辑
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' }).then(async () => {
    await logoutAPI()
    localStorage.clear()
    ElMessage.success('已退出')
    router.push('/login')
  })
}

// --- 3. 修改密码逻辑 ---
const pwdDialogVisible = ref(false)
const pwdFormRef = ref(null)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

// 自定义校验：确认密码必须与新密码一致
const validateConfirmPwd = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, validator: validateConfirmPwd, trigger: 'blur' }]
}

const submitPwd = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 构造传给后端的 DTO
        const submitData = {
          empId: userInfo.value.id,
          oldPassword: pwdForm.oldPassword,
          newPassword: pwdForm.newPassword
        }
        await editPasswordAPI(submitData)
        
        ElMessage.success('密码修改成功，请重新登录')
        pwdDialogVisible.value = false
        
        // 密码修改成功后，强制退出重新登录
        localStorage.clear()
        router.push('/login')
      } catch (error) {
        // 错误提示已在 request.js 拦截器中处理（如原密码错误）
      }
    }
  })
}

// --- 挂载时执行 ---
onMounted(() => {
  fetchShopStatus()
})
</script>

<style scoped>
.layout-container { height: 100vh; }
.aside { background-color: #304156; }
.logo-title { height: 60px; line-height: 60px; text-align: center; color: white; font-size: 20px; font-weight: bold; background: #2b3643;}

/* 头部样式调整 */
.header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  background: white; 
  box-shadow: 0 2px 4px rgba(0,0,0,0.08); 
  z-index: 10; 
  padding: 0 20px;
  height: 60px; /* 固定头部高度 */
}

.header-right {
  display: flex;
  align-items: center;
  height: 100%;
}

.shop-status {
  display: flex;
  align-items: center;
  margin-right: 20px; 
  padding-right: 20px;
  position: relative;
}

/* 用伪元素画一个精美的短分割线，避免 border-right 太高 */
.shop-status::after {
  content: '';
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  height: 20px;
  width: 1px;
  background-color: #dcdfe6;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  outline: none;
  height: 100%;
}

.user-dropdown:hover {
  color: var(--el-color-primary);
}
.main-content { padding: 20px; background: #f3f4f7; }
</style>