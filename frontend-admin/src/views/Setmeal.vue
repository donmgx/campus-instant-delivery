<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="queryParams.name" placeholder="请输入套餐名称" style="width: 200px; margin-right: 15px;" clearable />
      <el-select v-model="queryParams.categoryId" placeholder="请选择套餐分类" style="width: 150px; margin-right: 15px;" clearable>
        <el-option v-for="item in setmealCategoryList" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-select v-model="queryParams.status" placeholder="售卖状态" style="width: 150px; margin-right: 15px;" clearable>
        <el-option label="起售" :value="1" />
        <el-option label="停售" :value="0" />
      </el-select>
      <el-button type="primary" @click="fetchData">查询</el-button>
      <el-button type="success" @click="openDialog('add')">+ 新增套餐</el-button>
      <el-button type="danger" @click="handleBatchDelete" :disabled="selectedIds.length === 0">批量删除</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border @selection-change="handleSelectionChange" style="width: 100%; margin-top: 20px;">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="name" label="套餐名称" align="center" />
      <el-table-column label="图片" align="center" width="120">
        <template #default="{ row }">
          <el-image style="width: 50px; height: 50px; border-radius: 5px;" :src="row.image" fit="cover" :preview-src-list="[row.image]" preview-teleported />
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="套餐分类" align="center" />
      <el-table-column prop="price" label="套餐价" align="center">
        <template #default="{ row }"><span style="color: red; font-weight: bold;">￥{{ row.price }}</span></template>
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

    <el-dialog :title="dialogType === 'add' ? '新增套餐' : '修改套餐'" v-model="dialogVisible" width="700px" top="5vh">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="套餐名称" required>
          <el-input v-model="formData.name" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="套餐分类" required>
          <el-select v-model="formData.categoryId" placeholder="请选择">
            <el-option v-for="item in setmealCategoryList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="套餐价格" required>
          <el-input-number v-model="formData.price" :min="0.01" :precision="2" :step="1" />
        </el-form-item>
        
        <el-form-item label="套餐图片" required>
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
        </el-form-item>

        <el-form-item label="套餐描述">
          <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="套餐简述，如：包含一荤一素一汤" />
        </el-form-item>

        <el-form-item label="包含菜品" required>
          <el-button type="primary" plain @click="openAddDishDialog" style="margin-bottom: 10px;">+ 添加菜品</el-button>
          <el-table :data="formData.setmealDishes" border style="width: 100%">
            <el-table-column prop="name" label="名称" align="center" />
            <el-table-column prop="price" label="原价" align="center" width="100">
              <template #default="{ row }">￥{{ row.price }}</template>
            </el-table-column>
            <el-table-column label="份数" align="center" width="150">
              <template #default="{ row }">
                <el-input-number v-model="row.copies" :min="1" :max="99" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="100">
              <template #default="{ $index }">
                <el-button type="danger" link @click="removeSetmealDish($index)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="选择菜品" v-model="addDishDialogVisible" width="800px" append-to-body>
      <div style="display: flex; gap: 20px; height: 400px;">
        <div style="width: 200px; border-right: 1px solid #eee; overflow-y: auto;">
          <el-menu :default-active="activeDishCategoryId" @select="handleDishCategorySelect" style="border-right: none;">
            <el-menu-item v-for="item in dishCategoryList" :key="item.id" :index="item.id.toString()">
              {{ item.name }}
            </el-menu-item>
          </el-menu>
        </div>
        <div style="flex: 1; overflow-y: auto; padding-right: 10px;">
          <el-table :data="currentDishes" border @selection-change="handleDishSelectionChange" ref="dishTableRef">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column prop="name" label="菜品名称" align="center" />
            <el-table-column prop="price" label="售价" align="center" width="100">
              <template #default="{ row }">￥{{ row.price }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="addDishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddDishes">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { getSetmealPageAPI, addSetmealAPI, updateSetmealAPI, changeSetmealStatusAPI, deleteSetmealAPI, getSetmealByIdAPI } from '../api/setmeal'
import { getCategoryListAPI } from '../api/category'
import { getDishListByCategoryIdAPI } from '../api/dish'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// --- 基础状态 ---
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const selectedIds = ref([])

// 搜索参数
const queryParams = ref({ page: 1, pageSize: 10, name: '', categoryId: null, status: null })

// 表单数据
const formData = ref({ id: '', name: '', categoryId: '', price: 0, image: '', description: '', setmealDishes: [] })

// 分类数据字典
const setmealCategoryList = ref([]) // 套餐分类 (type=2)
const dishCategoryList = ref([])    // 菜品分类 (type=1)

// --- 图片上传相关 (与Dish一致) ---
const uploadUrl = '/api/admin/common/upload'
const uploadHeaders = computed(() => ({ token: localStorage.getItem('token') }))
const handleUploadSuccess = (res) => {
  const data = typeof res === 'string' ? JSON.parse(res) : res
  if (data.code === 1) {
    let url = data.data
    if (url && !url.startsWith('http')) url = 'https://' + url
    formData.value.image = url
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

// --- 初始化与数据加载 ---
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSetmealPageAPI(queryParams.value)
    tableData.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

const getCategories = async () => {
  setmealCategoryList.value = await getCategoryListAPI(2) // 2代表套餐分类
  dishCategoryList.value = await getCategoryListAPI(1)    // 1代表菜品分类
}

// --- 表格业务操作 ---
const handleSelectionChange = (val) => { selectedIds.value = val.map(item => item.id) }

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  await changeSetmealStatusAPI(newStatus, row.id)
  ElMessage.success('状态修改成功')
  fetchData()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该套餐吗？（起售中的套餐无法删除）', '提示', { type: 'warning' }).then(async () => {
    await deleteSetmealAPI(id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个套餐吗？`, '提示', { type: 'error' }).then(async () => {
    await deleteSetmealAPI(selectedIds.value.join(','))
    ElMessage.success('批量删除成功')
    fetchData()
  })
}

// --- 弹窗与表单提交 ---
const openDialog = async (type, id = null) => {
  dialogType.value = type
  if (type === 'edit') {
    const res = await getSetmealByIdAPI(id)
    formData.value = { ...res, setmealDishes: res.setmealDishes || [] }
  } else {
    formData.value = { id: '', name: '', categoryId: '', price: 0, image: '', description: '', setmealDishes: [] }
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formData.value.name || !formData.value.categoryId || !formData.value.image) {
    return ElMessage.warning('请填写完整的必填项信息并上传图片')
  }
  if (formData.value.setmealDishes.length === 0) {
    return ElMessage.warning('套餐至少需要包含一道菜品')
  }

  if (dialogType.value === 'add') {
    await addSetmealAPI(formData.value)
    ElMessage.success('套餐新增成功')
  } else {
    await updateSetmealAPI(formData.value)
    ElMessage.success('套餐修改成功')
  }
  dialogVisible.value = false
  fetchData()
}

// ==================== 选菜品逻辑 ====================
const addDishDialogVisible = ref(false)
const activeDishCategoryId = ref('')
const currentDishes = ref([])
const tempSelectedDishes = ref([])

const removeSetmealDish = (index) => {
  formData.value.setmealDishes.splice(index, 1)
}

const openAddDishDialog = async () => {
  addDishDialogVisible.value = true
  tempSelectedDishes.value = []
  // 默认选中第一个菜品分类
  if (dishCategoryList.value.length > 0) {
    activeDishCategoryId.value = dishCategoryList.value[0].id.toString()
    await fetchDishesByCategory(activeDishCategoryId.value)
  }
}

const handleDishCategorySelect = async (categoryId) => {
  activeDishCategoryId.value = categoryId
  await fetchDishesByCategory(categoryId)
}

const fetchDishesByCategory = async (categoryId) => {
  currentDishes.value = await getDishListByCategoryIdAPI(categoryId)
}

const handleDishSelectionChange = (val) => {
  tempSelectedDishes.value = val
}

const confirmAddDishes = () => {
  if (tempSelectedDishes.value.length === 0) {
    return ElMessage.warning('请至少选择一道菜品')
  }
  // 将选中的菜品组装成 后端 SetmealDish 需要的格式
  tempSelectedDishes.value.forEach(dish => {
    // 防止重复添加同一道菜
    const exists = formData.value.setmealDishes.find(item => item.dishId === dish.id)
    if (!exists) {
      formData.value.setmealDishes.push({
        dishId: dish.id,
        name: dish.name,
        price: dish.price,
        copies: 1 // 默认 1 份
      })
    }
  })
  addDishDialogVisible.value = false
  ElMessage.success('添加成功，请在表格中调整份数')
}

onMounted(() => {
  fetchData()
  getCategories()
})
</script>

<style scoped>
.app-container { padding: 20px; background: white; border-radius: 8px; }
.filter-container { display: flex; align-items: center; }

/* 图片上传组件样式复用 */
.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader :deep(.el-upload:hover) { border-color: var(--el-color-primary); }
.el-icon.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 120px; height: 120px; text-align: center; }
.avatar { width: 120px; height: 120px; display: block; object-fit: cover; }
</style>