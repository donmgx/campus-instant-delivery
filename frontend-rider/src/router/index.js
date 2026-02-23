import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/register',
        component: () => import('../views/Register.vue')
    },
    {

        path: '/login',
        component: () => import('../views/Login.vue')
    },
    {
        path: '/',
        component: () => import('../views/Layout.vue'),
        redirect: '/hall',
        children: [
            {
                path: 'hall',
                component: () => import('../views/Hall.vue')
            },
            {
                path: 'running',
                component: () => import('../views/Running.vue')
            },
            {
                path: 'profile',
                component: () => import('../views/Profile.vue')
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 全局前置路由守卫：校验 Token
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  // 设置不需要登录就能访问的白名单页面
  const whiteList = ['/login', '/register']
  
  // 如果去的页面不在白名单里，且没有 token，才强制打回登录页
  if (!whiteList.includes(to.path) && !token) {
    next('/login')
  } else {
    // 正常放行
    next()
  }
})

export default router