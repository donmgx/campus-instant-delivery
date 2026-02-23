import request from '../utils/request'

export const loginAPI = (data) => request.post('/admin/employee/login', data)
export const logoutAPI = () => request.post('/admin/employee/logout')
export const getEmployeePageAPI = (params) => request.get('/admin/employee/page', { params })
export const addEmployeeAPI = (data) => request.post('/admin/employee', data)
export const updateEmployeeAPI = (data) => request.put('/admin/employee', data)
export const changeEmployeeStatusAPI = (status, id) => request.post(`/admin/employee/status/${status}?id=${id}`)
export const editPasswordAPI = (data) => request.put('/admin/employee/editPassword', data)