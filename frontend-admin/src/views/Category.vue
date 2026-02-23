<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="queryParams.name" placeholder="请输入分类名称" style="width: 200px; margin-right: 15px;" clearable />
      <el-select v-model="queryParams.type" placeholder="请选择分类类型" style="width: 150px; margin-right: 15px;" clearable>
        <el-option label="菜品分类" :value="1" />
        <el-option label="套餐分类" :value="2" />
      </el-select>
      <el-button type="primary" @click="fetchData">查询</el-button>
      <el-button type="success" @click="openDialog('add')">+ 新增分类</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="name" label="分类名称" align="center" />
      <el-table-column label="分类类型" align="center">
        <template #default="{ row }">
          <el-tag :type="row.type === 1 ? 'info' : 'warning'">
            {{ row.type === 1 ? '菜品分类' : '套餐分类' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序权重" align="center" width="120" />
      <el-table-column label="状态" align="center" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="最后修改时间" align="center" width="180" />
      
      <el-table-column label="操作" align="center" width="220">
        <template #default="{ row }">
          <el-button type="primary" link @click="openDialog('edit', row)">编辑</el-button>
          <el-button :type="row.status === 1 ? 'danger' : 'success'" link @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
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

    <el-dialog :title="dialogType === 'add' ? '新增分类' : '修改分类'" v-model="dialogVisible" width="450px" destroy-on-close>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类类型" prop="type">
          <el-radio-group v-model="formData.type" :disabled="dialogType === 'edit'">
            <el-radio :label="1">菜品分类</el-radio>
            <el-radio :label="2">套餐分类</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="1" :max="99" />
          <div style="margin-left: 10px; font-size: 12px; color: #999;">数字越小，越靠前</div>
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
import { ref, reactive, onMounted } from 'vue'
import { getCategoryPageAPI, addCategoryAPI, updateCategoryAPI, changeCategoryStatusAPI, deleteCategoryAPI } from '../api/category'
import { ElMessage, ElMessageBox } from 'element-plus'

// --- 状态变量 ---
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)

// 搜索参数
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  name: '',
  type: null
})

// 表单数据
const formData = reactive({
  id: '',
  name: '',
  type: 1, // 默认菜品分类
  sort: 1
})

// 表单校验规则
const rules = {
  name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '请选择分类类型', trigger: 'change' }]
}

// --- 业务方法 ---

// 1. 获取分页数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCategoryPageAPI(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

// 2. 启用/禁用状态切换
const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await changeCategoryStatusAPI(newStatus, row.id)
    ElMessage.success('状态更新成功')
    fetchData()
  } catch (error) {
    // 错误在 request.js 已拦截
  }
}

// 3. 删除分类
const handleDelete = (id) => {
  ElMessageBox.confirm(
    '确定要删除该分类吗？<br/><span style="color:red;font-size:12px;">注：如果该分类下关联了菜品或套餐，将无法删除。</span>', 
    '删除警告', 
    { type: 'warning', dangerouslyUseHTMLString: true }
  ).then(async () => {
    await deleteCategoryAPI(id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

// 4. 打开弹窗 (新增/编辑共用)
const openDialog = (type, row = null) => {
  dialogType.value = type
  if (type === 'edit') {
    // 数据回显
    Object.assign(formData, {
      id: row.id,
      name: row.name,
      type: row.type,
      sort: row.sort
    })
  } else {
    // 重置数据
    Object.assign(formData, { id: '', name: '', type: 1, sort: 1 })
    // 如果之前有校验错误，清空掉
    if (formRef.value) formRef.value.clearValidate()
  }
  dialogVisible.value = true
}

// 5. 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (dialogType.value === 'add') {
        await addCategoryAPI(formData)
        ElMessage.success('新增分类成功')
      } else {
        await updateCategoryAPI(formData)
        ElMessage.success('修改分类成功')
      }
      dialogVisible.value = false
      fetchData()
    }
  })
}

// 页面加载时请求数据
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.app-container { 
  padding: 20px; 
  background: white; 
  border-radius: 8px; 
}
.filter-container { 
  display: flex; 
  align-items: center; 
}
</style>
<!-- <template>

  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="queryParams.name" placeholder="请输入分类名称" style="width: 200px; margin-right: 15px;" clearable />
      <el-select v-model="queryParams.type" placeholder="分类类型" style="width: 150px; margin-right: 15px;" clearable>
        <el-option label="菜品分类" :value="1" />
        <el-option label="套餐分类" :value="2" />
      </el-select>
      <el-button type="primary" @click="fetchData">查询</el-button>
      <el-button type="success" @click="openDialog('add')">+ 新增分类</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="name" label="分类名称" align="center" />
      <el-table-column label="分类类型" align="center">
        <template #default="{ row }">
          <el-tag :type="row.type === 1 ? 'info' : 'warning'">{{ row.type === 1 ? '菜品分类' : '套餐分类' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" align="center" width="100" />
      <el-table-column label="状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="250">
        <template #default="{ row }">
          <el-button type="primary" link @click="openDialog('edit', row)">编辑</el-button>
          <el-button :type="row.status === 1 ? 'danger' : 'success'" link @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="400px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="formData.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类类型">
          <el-radio-group v-model="formData.type" :disabled="dialogType === 'edit'">
            <el-radio :label="1">菜品分类</el-radio>
            <el-radio :label="2">套餐分类</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="1" :max="99" />
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
import { getCategoryPageAPI, addCategoryAPI, updateCategoryAPI, changeCategoryStatusAPI, deleteCategoryAPI } from '../api/category'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const dialogTitle = computed(() => dialogType.value === 'add' ? '新增分类' : '编辑分类')

const queryParams = ref({ page: 1, pageSize: 10, name: '', type: null })
const formData = ref({ id: '', name: '', type: 1, sort: 1 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCategoryPageAPI(queryParams.value)
    tableData.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  await changeCategoryStatusAPI(newStatus, row.id)
  ElMessage.success('状态更新成功')
  fetchData()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该分类吗？如果分类下有菜品则无法删除。', '警告', { type: 'warning' }).then(async () => {
    await deleteCategoryAPI(id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const openDialog = (type, row = null) => {
  dialogType.value = type
  if (type === 'edit') {
    formData.value = { ...row }
  } else {
    formData.value = { id: '', name: '', type: 1, sort: 1 }
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (dialogType.value === 'add') {
    await addCategoryAPI(formData.value)
    ElMessage.success('新增成功')
  } else {
    await updateCategoryAPI(formData.value)
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
</style> -->