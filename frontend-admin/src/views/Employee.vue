<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="queryParams.name" placeholder="请输入员工姓名" style="width: 200px; margin-right: 15px;" clearable />
      <el-button type="primary" @click="fetchData">查询</el-button>
      <el-button type="success" @click="openDialog('add')">+ 新增员工</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="name" label="员工姓名" align="center" />
      <el-table-column prop="username" label="账号" align="center" />
      <el-table-column prop="phone" label="手机号" align="center" />
      <el-table-column label="状态" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="250">
        <template #default="{ row }">
          <el-button type="primary" link @click="openDialog('edit', row)">编辑</el-button>
          <el-button :type="row.status === 1 ? 'danger' : 'success'" link @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
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

    <el-dialog :title="dialogType === 'add' ? '新增员工' : '编辑员工'" v-model="dialogVisible" width="500px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="账号"><el-input v-model="formData.username" :disabled="dialogType === 'edit'"/></el-form-item>
        <el-form-item label="姓名"><el-input v-model="formData.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="formData.phone" /></el-form-item>
        <el-form-item label="身份证"><el-input v-model="formData.idNumber" /></el-form-item>
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
import { getEmployeePageAPI, addEmployeeAPI, updateEmployeeAPI, changeEmployeeStatusAPI } from '../api/employee'
import { ElMessage, ElMessageBox } from 'element-plus'

// 状态变量
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const queryParams = ref({ page: 1, pageSize: 10, name: '' })
const formData = ref({ id: '', username: '', name: '', phone: '', idNumber: '' })

// 加载数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getEmployeePageAPI(queryParams.value)
    tableData.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

// 启用/禁用
const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  await changeEmployeeStatusAPI(newStatus, row.id)
  ElMessage.success('状态修改成功')
  fetchData()
}

// 打开弹窗
const openDialog = (type, row = null) => {
  dialogType.value = type
  if (type === 'edit') {
    formData.value = { ...row } // 回显数据
  } else {
    formData.value = { id: '', username: '', name: '', phone: '', idNumber: '' }
  }
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (dialogType.value === 'add') {
    await addEmployeeAPI(formData.value)
    ElMessage.success('新增成功')
  } else {
    await updateEmployeeAPI(formData.value)
    ElMessage.success('修改成功')
  }
  dialogVisible.value = false
  fetchData()
}

onMounted(() => fetchData())
</script>

<style scoped>
.app-container { padding: 20px; background: white; border-radius: 8px; }
.filter-container { display: flex; align-items: center; }
</style>