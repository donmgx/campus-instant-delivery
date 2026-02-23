<div align="center">
    <h1>校园即时配送全栈系统 (Monorepo)</h1>
    <p>
        <b>Campus Delivery Full-Stack System</b>
    </p>
    <p>
        <img src="https://img.shields.io/badge/Java-17-blue.svg" alt="Java 17">
        <img src="https://img.shields.io/badge/Spring%20Boot-2.7.3-green.svg" alt="Spring Boot">
        <img src="https://img.shields.io/badge/Vue.js-3.x-4FC08D.svg" alt="Vue3">
        <img src="https://img.shields.io/badge/uni--app-Vite-2B9939.svg" alt="uni-app">
        <img src="https://img.shields.io/badge/Elasticsearch-7.17-yellow.svg" alt="Elasticsearch">
        <img src="https://img.shields.io/badge/RabbitMQ-3.9-orange.svg" alt="RabbitMQ">
        <img src="https://img.shields.io/badge/Redis-6.2-red.svg" alt="Redis">
    </p>
</div>

---

## 项目简介 | Introduction

**校园即时配送系统** 是一款专为高校封闭/半封闭场景打造的高并发餐饮外卖平台。

本项目采用大厂流行的 **Monorepo (单体仓库)** 架构，包含高性能 Java 后端及三端前端（用户端、管理端、骑手端），提供从点餐、结算、派送到售后的**完整商业级闭环**。通过引入多级缓存与中间件，解决校园高峰期高并发、高频搜索等性能瓶颈。

---

## 仓库结构 | Monorepo Structure

```plaintext
Campus-Delivery-System
├── backend/            # Java Spring Boot 核心后端 (基于 DDD 思想模块化设计)
├── frontend-user/      # 用户端小程序 (uni-app + Vue3 + Vite 构建)
├── frontend-admin/     # 商家管理端 Web (Vue3 + Element Plus)
├── frontend-rider/     # 骑手派送端 H5 网页 (响应式移动端 UI)
└── docs/               # 数据库 SQL 脚本、API 接口文档与 E-R 图
```

------

## 核心技术亮点 | Technical Highlights

- **高性能检索 (Elasticsearch)**：基于 ES 倒排索引重构商品检索，结合 RabbitMQ 实现近实时数据同步，复杂条件检索响应耗时降至 20ms 以内。
- **高并发秒杀防超卖 (Redisson + Lua)**：优惠券抢购场景下，采用 Redis + Lua 脚本实现原子性库存扣减，结合 Redisson 分布式锁彻底杜绝超卖。
- **接口幂等性保障 (防重发)**：设计 `@AutoIdempotent` 自定义注解，基于 Token 机制与 Redis 查删原子操作，完美拦截订单与支付的重复提交。
- **缓存穿透防御 (Bloom Filter)**：集成 Guava/Redisson 布隆过滤器，系统启动预热，高效拦截 99% 的非法恶意 ID 请求，保护数据库底座。
- **业务解耦与超时管控 (RabbitMQ)**：剥离非核心链路，利用延迟队列 (TTL + DLX) 实现“超时未支付自动取消订单”业务闭环。
- **丝滑前端体验**：全面接入 Vue3 组合式 API (Composition API)，实现购物车 Redis 持久化、优惠券动态抵扣算法、防挤压 Flex 布局及原生微信授权登录。

------

## 极速启动 | Quick Start

### 1. 后端服务 (`backend/`)

- **环境要求**：JDK 17, MySQL 8.x, Redis 5.0+, Elasticsearch 7.17.7, RabbitMQ 3.9+
- **初始化**：执行 `docs/` 目录下的 SQL 脚本初始化数据库。
- **配置**：修改 `src/main/resources/application-dev.yml` 中的中间件连接参数（Redis、MQ、ES 等）。
- **运行**：启动 `CampusApplication.java`。API 文档访问 `http://localhost:8080/doc.html`。

### 2. 前端服务 (`frontend-*/`)

进入对应的子目录，安装依赖并启动运行。例如启动用户端与骑手端：


```
# 启动用户端小程序
cd frontend-user
npm install
npm run dev:mp-weixin

# 启动骑手端 H5
cd ../frontend-rider
npm install
npm run dev
```

------

## 贡献与支持 | Contribution & Support

欢迎提交 Issue 和 Pull Request！

- **Author:** Yuan Yufan (donmgx)
- **Email:** yufanyuan70@gmail.com
- **License:** MIT License