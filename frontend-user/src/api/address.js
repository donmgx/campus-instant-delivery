import request from '../utils/request.js'

// 1. 获取当前用户的所有地址
export const getAddressListAPI = () => request.get('/addressBook/list')

// 2. 新增地址
export const addAddressAPI = (data) => request.post('/addressBook', data)

// 3. 修改地址
export const updateAddressAPI = (data) => request.put('/addressBook', data)

// 4. 删除地址
export const deleteAddressAPI = (id) => request.delete(`/addressBook?id=${id}`)

// 5. 设置默认地址
export const setDefaultAddressAPI = (data) => request.put('/addressBook/default', data)