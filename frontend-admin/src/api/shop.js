import request from '../utils/request'

// 获取店铺营业状态
export const getShopStatusAPI = () => request.get('/admin/shop/status')

// 设置店铺营业状态 (1:营业中, 0:打烊中)
export const setShopStatusAPI = (status) => request.put(`/admin/shop/${status}`)