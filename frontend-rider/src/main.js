import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.js'
import Vant from 'vant'
import 'vant/lib/index.css' // 引入 Vant 移动端样式

const app = createApp(App)
app.use(router)
app.use(Vant)
app.mount('#app')