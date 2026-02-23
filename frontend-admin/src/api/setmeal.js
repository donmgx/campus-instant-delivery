import request from '../utils/request'

export const getSetmealPageAPI = (params) => request.get('/admin/setmeal/page', { params })
export const addSetmealAPI = (data) => request.post('/admin/setmeal', data)
export const updateSetmealAPI = (data) => request.put('/admin/setmeal', data)
export const getSetmealByIdAPI = (id) => request.get(`/admin/setmeal/${id}`)
export const deleteSetmealAPI = (ids) => request.delete(`/admin/setmeal?ids=${ids}`)
export const changeSetmealStatusAPI = (status, id) => request.post(`/admin/setmeal/status/${status}?id=${id}`)