<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="queryParams.name" placeholder="请输入优惠券名称" style="width: 200px; margin-right: 15px;" clearable />
      <el-select v-model="queryParams.status" placeholder="活动状态" style="width: 150px; margin-right: 15px;" clearable>
        <el-option label="未开始" :value="0" />
        <el-option label="进行中" :value="1" />
        <el-option label="已结束" :value="2" />
      </el-select>
      <el-button type="primary" @click="fetchData">查询</el-button>
      <el-button type="success" @click="openDialog('add')">+ 新增优惠券</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="name" label="优惠券名称" align="center" width="180" />
      
      <el-table-column label="优惠金额" align="center" width="120">
        <template #default="{ row }">
          <span style="color: red; font-weight: bold;">￥{{ row.amount }}</span>
        </template>
      </el-table-column>
      
      <el-table-column label="使用门槛" align="center" width="150">
        <template #default="{ row }">
          <span v-if="row.conditionAmount > 0">满 {{ row.conditionAmount }} 元可用</span>
          <span v-else>无门槛</span>
        </template>
      </el-table-column>

      <el-table-column prop="stock" label="发行库存" align="center" width="100" />
      
      <el-table-column label="有效期" align="center" width="300">
        <template #default="{ row }">
          {{ formatDate(row.beginTime) }} 至 {{ formatDate(row.endTime) }}
        </template>
      </el-table-column>

      <el-table-column label="状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" fixed="right" width="220">
        <template #default="{ row }">
          <el-button type="primary" link @click="openDialog('edit', row.id)">编辑</el-button>
          <el-button type="danger" link @click="handleStartSeckill(row.id)">开启秒杀</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination 
      v-model:current-page="queryParams.page" 
      v-model:page-size="queryParams.pageSize" 
      :total="total" 
      layout="total, prev, pager, next" 
      @current-change="fetchData" 
      style="margin-top: 20px; justify-content: flex-end;"
    />

    <el-dialog :title="dialogType === 'add' ? '新增优惠券' : '编辑优惠券'" v-model="dialogVisible" width="550px">
      <el-form :model="formData" label-width="110px" :rules="rules" ref="formRef">
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="formData.name" placeholder="如：周末狂欢满减券" />
        </el-form-item>
        
        <el-form-item label="面额 (元)" prop="amount">
          <el-input-number v-model="formData.amount" :min="1" :precision="2" :step="1" />
        </el-form-item>
        
        <el-form-item label="使用门槛 (元)" prop="conditionAmount">
          <el-input-number v-model="formData.conditionAmount" :min="0" :precision="2" :step="1" placeholder="0代表无门槛" />
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">设为0即为无门槛券</span>
        </el-form-item>

        <el-form-item label="发行数量" prop="stock">
          <el-input-number v-model="formData.stock" :min="1" :step="10" />
        </el-form-item>

        <el-form-item label="活动有效期" prop="validTimeRange">
          <el-date-picker
            v-model="formData.validTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCouponPageAPI, addCouponAPI, updateCouponAPI, getCouponByIdAPI, startSeckillAPI } from '../api/coupon'
import { ElMessage, ElMessageBox } from 'element-plus'

// --- 状态变量 ---
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)

const queryParams = ref({ page: 1, pageSize: 10, name: '', status: null })

// 表单数据，注意这里单独定义了 validTimeRange 数组来承接 DatePicker 的双向绑定
const formData = ref({
  id: '',
  name: '',
  amount: 0,
  conditionAmount: 0,
  stock: 100,
  validTimeRange: [] // [开始时间, 结束时间]
})

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  validTimeRange: [{ required: true, message: '请选择活动有效期', trigger: 'change' }]
}

// --- 辅助方法 ---
const formatDate = (dateString) => {
  if (!dateString) return ''
  // 截取掉后端的毫秒数或者不需要的部分，保留到分钟即可
  return dateString.substring(0, 16)
}

const getStatusText = (status) => {
  const map = { 0: '未开始', 1: '进行中', 2: '已结束' }
  return map[status] || '未知'
}

const getStatusTagType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

// --- 业务方法 ---
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCouponPageAPI(queryParams.value)
    tableData.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

const openDialog = async (type, id = null) => {
  dialogType.value = type
  if (type === 'edit') {
    const res = await getCouponByIdAPI(id)
    // 组装回显数据，将后端的 beginTime 和 endTime 拼成数组给选择器用
    formData.value = {
      ...res,
      validTimeRange: [res.beginTime, res.endTime]
    }
  } else {
    // 重置表单
    formData.value = { id: '', name: '', amount: 10, conditionAmount: 0, stock: 100, validTimeRange: [] }
    if (formRef.value) formRef.value.resetFields()
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      // 核心逻辑：提交前，把前端的 validTimeRange 拆分给后端的 beginTime 和 endTime
      const submitData = {
        ...formData.value,
        beginTime: formData.value.validTimeRange[0],
        endTime: formData.value.validTimeRange[1]
      }
      // 删除不需要传给后端的辅助字段
      delete submitData.validTimeRange

      if (dialogType.value === 'add') {
        await addCouponAPI(submitData)
        ElMessage.success('新增优惠券成功')
      } else {
        await updateCouponAPI(submitData)
        ElMessage.success('修改优惠券成功')
      }
      dialogVisible.value = false
      fetchData()
    }
  })
}

const handleStartSeckill = (id) => {
  ElMessageBox.confirm('开启秒杀后该优惠券将被推送到前端大厅且开始扣减库存，确认开启吗？', '高危操作提示', {
    type: 'warning',
    confirmButtonText: '确认开启',
    cancelButtonText: '取消'
  }).then(async () => {
    await startSeckillAPI(id)
    ElMessage.success('秒杀活动已开启！')
    fetchData()
  })
}

onMounted(() => fetchData())
</script>

<style scoped>
.app-container { padding: 20px; background: white; border-radius: 8px; }
.filter-container { display: flex; align-items: center; }
</style>