<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="queryParams.name" placeholder="请输入菜品名称" style="width: 200px; margin-right: 15px;" clearable />
      <el-select v-model="queryParams.categoryId" placeholder="请选择菜品分类" style="width: 150px; margin-right: 15px;" clearable>
        <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-select v-model="queryParams.status" placeholder="售卖状态" style="width: 150px; margin-right: 15px;" clearable>
        <el-option label="起售" :value="1" />
        <el-option label="停售" :value="0" />
      </el-select>
      <el-button type="primary" @click="fetchData">查询</el-button>
      <el-button type="success" @click="openDialog('add')">+ 新增菜品</el-button>
      <el-button type="danger" @click="handleBatchDelete" :disabled="selectedIds.length === 0">批量删除</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border @selection-change="handleSelectionChange" style="width: 100%; margin-top: 20px;">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="name" label="菜品名称" align="center" />
      <el-table-column label="图片" align="center" width="120">
        <template #default="{ row }">
          <el-image style="width: 50px; height: 50px; border-radius: 5px;" :src="row.image" fit="cover" :preview-src-list="[row.image]" preview-teleported />
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="所属分类" align="center" />
      <el-table-column prop="price" label="售价" align="center">
        <template #default="{ row }"><span style="color: red;">￥{{ row.price }}</span></template>
      </el-table-column>
      <el-table-column label="状态" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '起售' : '停售' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="最后操作时间" align="center" width="180" />
      <el-table-column label="操作" align="center" width="220">
        <template #default="{ row }">
          <el-button type="primary" link @click="openDialog('edit', row.id)">修改</el-button>
          <el-button :type="row.status === 1 ? 'danger' : 'success'" link @click="toggleStatus(row)">
            {{ row.status === 1 ? '停售' : '起售' }}
          </el-button>
          <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
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

    <el-dialog :title="dialogType === 'add' ? '新增菜品' : '修改菜品'" v-model="dialogVisible" width="650px" top="5vh">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="菜品名称" required>
          <el-input v-model="formData.name" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="菜品分类" required>
          <el-select v-model="formData.categoryId" placeholder="请选择">
            <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜品价格" required>
          <el-input-number v-model="formData.price" :min="0.01" :precision="2" :step="1" placeholder="价格" />
        </el-form-item>
        
        <el-form-item label="菜品图片" required>
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <img v-if="formData.image" :src="formData.image" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="el-upload__tip">只能上传 jpg/png 文件，且不超过 2MB</div>
        </el-form-item>

        <el-form-item label="菜品描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="菜品简述" />
        </el-form-item>

        <el-form-item label="口味配置">
          <div v-for="(flavor, index) in formData.flavors" :key="index" class="flavor-item">
            <el-input v-model="flavor.name" placeholder="口味名称 (如: 甜度)" style="width: 150px;" />
            <span style="margin: 0 10px;">-</span>
            <el-input v-model="flavor.value" placeholder="可选标签 (逗号分隔, 如: 无糖,少糖,半糖)" style="flex: 1;" />
            <el-button type="danger" link style="margin-left: 10px;" @click="removeFlavor(index)">删除</el-button>
          </div>
          <el-button type="primary" plain @click="addFlavor" style="margin-top: 10px;">+ 添加口味</el-button>
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
import { ref, onMounted, computed } from 'vue'
import { getDishPageAPI, addDishAPI, updateDishAPI, changeDishStatusAPI, deleteDishAPI, getDishByIdAPI } from '../api/dish'
import { getCategoryListAPI } from '../api/category'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// --- 基础状态 ---
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const categoryList = ref([]) // 用于下拉框的数据
const selectedIds = ref([]) // 批量选中的ID

// 查询参数
const queryParams = ref({ page: 1, pageSize: 10, name: '', categoryId: null, status: null })

// 表单数据 (包含口味数组)
const formData = ref({ id: '', name: '', categoryId: '', price: 0, image: '', description: '', flavors: [] })

// --- 图片上传相关 ---
const uploadUrl = '/api/admin/common/upload' // 配合 vite.config.js 的代理
const uploadHeaders = computed(() => ({ token: localStorage.getItem('token') })) // 必须携带 Token
const handleUploadSuccess = (res) => {
  // 1. 容错处理：确保 res 是一个 JSON 对象
  const data = typeof res === 'string' ? JSON.parse(res) : res
  
  if (data.code === 1) {
    let url = data.data
    
    // 2. 容错处理：如果 OSS 返回的链接没有带协议头，手动帮它补上
    if (url && !url.startsWith('http')) {
      url = 'https://' + url
    }
    
    formData.value.image = url // 赋值给表单
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(data.msg || '上传失败')
  }
}
const beforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) ElMessage.error('上传图片只能是 JPG/PNG 格式!')
  if (!isLt2M) ElMessage.error('上传图片大小不能超过 2MB!')
  return isImage && isLt2M
}

// --- 初始化数据 ---
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDishPageAPI(queryParams.value)
    tableData.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

const getCategories = async () => {
  const res = await getCategoryListAPI(1) // 1表示查询菜品分类
  categoryList.value = res
}

// --- 口味动态操作 ---
const addFlavor = () => formData.value.flavors.push({ name: '', value: '' })
const removeFlavor = (index) => formData.value.flavors.splice(index, 1)

// --- 业务操作 ---
const handleSelectionChange = (val) => { selectedIds.value = val.map(item => item.id) }

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  await changeDishStatusAPI(newStatus, row.id)
  ElMessage.success('状态修改成功')
  fetchData()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该菜品吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteDishAPI(id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个菜品吗？`, '提示', { type: 'error' }).then(async () => {
    await deleteDishAPI(selectedIds.value.join(',')) // 将数组转为逗号分隔的字符串
    ElMessage.success('批量删除成功')
    fetchData()
  })
}

const openDialog = async (type, id = null) => {
  dialogType.value = type
  if (type === 'edit') {
    // 编辑时，必须通过 ID 去后端查询包含口味的完整明细
    const res = await getDishByIdAPI(id)
    formData.value = { ...res, flavors: res.flavors || [] }
  } else {
    formData.value = { id: '', name: '', categoryId: '', price: 0, image: '', description: '', flavors: [] }
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formData.value.name || !formData.value.categoryId || !formData.value.image) {
    return ElMessage.warning('请填写完整的必填项信息并上传图片')
  }
  if (dialogType.value === 'add') {
    await addDishAPI(formData.value)
    ElMessage.success('菜品新增成功')
  } else {
    await updateDishAPI(formData.value)
    ElMessage.success('菜品修改成功')
  }
  dialogVisible.value = false
  fetchData()
}

onMounted(() => {
  fetchData()
  getCategories()
})
</script>

<style scoped>
.app-container { padding: 20px; background: white; border-radius: 8px; }
.filter-container { display: flex; align-items: center; }

/* 图片上传组件样式 */
.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}
.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}
.flavor-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  background: #f8f9fa;
  padding: 10px;
  border-radius: 5px;
}
</style>