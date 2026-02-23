import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard', // 默认重定向到工作台
    children: [
      { path: 'dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'employee', component: () => import('../views/Employee.vue') },
      { path: 'category', component: () => import('../views/Category.vue') },
      { path: 'order', component: () => import('../views/Order.vue') },
      { path: 'dish', component: () => import('../views/Dish.vue') },
      { path: 'coupon', component: () => import('../views/Coupon.vue') },
      { path: 'setmeal', component: () => import('../views/Setmeal.vue') },
      { path: 'report', component: () => import('../views/Report.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：没有 Token 强制跳转登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router