Markdown<div align="center">
    <h1>校园即时配送高性能后端系统</h1>
    <p>
        <b>Campus Delivery High-Performance Backend System</b>
    </p>
    <p>
        <img src="https://img.shields.io/badge/Java-17-blue.svg" alt="Java 17">
        <img src="https://img.shields.io/badge/Spring%20Boot-2.7.3-green.svg" alt="Spring Boot">
        <img src="https://img.shields.io/badge/Elasticsearch-7.17.7-yellow.svg" alt="Elasticsearch">
        <img src="https://img.shields.io/badge/RabbitMQ-3.9-orange.svg" alt="RabbitMQ">
        <img src="https://img.shields.io/badge/Redis-6.2-red.svg" alt="Redis">
        <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License">
    </p>
</div>

---

## 项目背景 | Background

**校园配送系统** 是一款专为高校封闭式/半封闭式场景打造的**高并发餐饮配送平台**。

针对校园外卖业务中 **“午晚高峰瞬时流量大”**、**“地址路径复杂”**、**“商品搜索频次高”** 的特点，本项目在经典单体架构的基础上，引入了 **DDD（领域驱动设计）** 思想进行模块划分，并集成了 **Elasticsearch**、**RabbitMQ**、**Redisson** 等中间件，成功解决了传统外卖系统在高峰期的性能瓶颈。

> **核心价值**：在保持单体应用部署便捷性的同时，通过中间件赋能，实现了媲美微服务架构的高吞吐量与高可用性。

---

## 系统架构 | Architecture

本项目采用**模块化单体架构 (Modular Monolith)**，业务逻辑清晰，各层级职责明确。

---

## 核心技术亮点 | Technical Highlights

### Elasticsearch 高性能商品搜索
- 痛点：传统 SQL LIKE 查询在百万级数据量下性能急剧下降，且无法处理复杂的“分词搜索”和“按权重排序”。
- 解决方案：
利用 Spring Data Elasticsearch 构建倒排索引。
通过 DishEsSyncListener 监听数据库变更，利用 RabbitMQ 实现 MySQL 到 ES 的近实时数据同步。

- 效果：复杂条件下的商品搜索响应时间从 500ms+ 降低至 20ms 以内。

###  Redisson + Lua 分布式秒杀锁
- 痛点：优惠券抢购场景下，高并发会导致数据库 CPU 飙升，甚至出现“超卖”重大事故。
- 解决方案：
采用 Redis + Lua 脚本 (seckill.lua) 在内存中原子执行库存扣减与资格校验。
引入 Redisson 分布式锁，防止集群部署下的并发冲突。

- 核心代码逻辑：
```lua
-- seckill.lua 核心逻辑片段
if (redis.call('get', stockKey) < amount) then
    return -1 -- 库存不足
end
if (redis.call('sismember', userKey, userId) == 1) then
    return -2 -- 重复领取
end
redis.call('decrby', stockKey, amount) -- 扣减库存
redis.call('sadd', userKey, userId)    -- 记录用户
return 1

```

###  RabbitMQ 异步订单解耦
- 痛点：用户支付成功后，系统需要执行“修改订单状态”、“通知商家”、“打印小票”等操作，同步执行导致接口耗时过长。
- 解决方案：
引入 RabbitMQ 声明 order.exchange，将非核心业务逻辑剥离。
使用 延迟队列 (TTL + DLX) 处理“超时未支付自动取消订单”的业务闭环。

###  布隆过滤器 (Bloom Filter) 防缓存穿透
- 痛点：恶意用户可能请求数据库中不存在的 ID，导致请求绕过 Redis 直接击穿数据库，造成宕机风险。
- 解决方案：
引入 Guava / Redisson 布隆过滤器，在 BloomFilterInitRunner 启动时预热数据。
请求到达时，先在布隆过滤器中判断“是否存在”，如果不存在则直接拦截，不再查询数据库。

- 核心优势：拦截了 99% 的非法请求，构建了数据库的第一道防火墙。

### 自定义注解 + Token 实现接口幂等性
- 痛点：由于网络波动或用户连点，导致“提交订单”或“支付”请求被重复发送，产生脏数据。
- 解决方案：
设计 @AutoIdempotent 注解和 IdempotentInterceptor 拦截器。
- 机制：客户端请求前先获取 Token，请求时服务端执行 Lua 脚本 进行“查删原子性操作”（Get and Delete），验证成功才放行。

###  AOP + ThreadLocal 优雅的字段填充
- 痛点：每个 insert/update 操作都需要手动设置 createTime、createUser 等字段，代码冗余。
- 解决方案：
定义 @AutoFill 注解标识数据库操作方法。
利用 Spring AOP 切面拦截，结合 ThreadLocal (BaseContext) 获取当前登录用户 ID，反射自动填充公共字段。

---

##  项目结构 | Project Structure
```plaintext
campus-delivery
├── campus-common          #  基础设施层 (常量, 工具类, 异常处理)
├── campus-pojo            #  领域模型层 (DTO, Entity, VO, ES文档)
└── campus-server          #  核心业务层
    ├── config             # 配置类 (Redis, Redisson, RabbitMQ, WebMVC)
    ├── controller         # 控制器 (Admin/User 接口)
    ├── service            # 业务逻辑接口与实现
    ├── annotatiohn        # 自动注入和幂等性注解
    ├── event              # 事件类
    ├── mapper             # MyBatis DAO 层
    ├── filter             # Spring Security 的 JWT 认证过滤器链
    ├── aspect             # AOP 切面 (自动填充)
    ├── repository         # ES 所需的 repository
    ├── handler            # 全局异常拦截器和认证与权限的处理
    ├── task               # 定时任务 (Spring Task)
    ├── security           # Spring Security 用来封装登录用户信息
    ├── listener           # 消息监听器 (ES同步, 订单处理)
    └── websocket          # 消息推送 (WebSocketServer)
```

---

## 核心功能模块 | Features
###  管理端 (Admin Portal)
- 工作台: 实时展示今日数据（营业额、订单数、客单价）。
- 商品管理: 支持图片上传 (阿里云 OSS)、多口味配置、ES 数据同步。
- 订单处理:
  - Websocket 实时接单/拒单。
  - 智能派送路径规划（基础版）。
- 数据报表: 基于 Apache POI 导出 Excel 运营报表（最近 30 天趋势）。

### 用户端 (User App)
- 安全登录: 微信小程序一键登录 (OAuth2) + JWT 令牌校验。
- 极速点餐:
  - 支持 Elasticsearch 关键词搜索。
  - Redis 缓存店铺营业状态与热点菜品。
- 营销活动: 优惠券领取与核销（高并发 Lua 脚本支撑）。
- 购物车: 基于 Redis Hash 结构存储，持久化不丢失。

---

##  环境部署 | Setup & Deployment
### 基础环境
- JDK: 17
- Maven: 3.6+
- MySQL: 8.0.33
- Redis: 5.0+
- Erlang/RabbitMQ: 3.9+
- Elasticsearch: 7.17.7 (注意：本项目配置端口为 9201)

### 配置文件说明
修改 campus-server/src/main/resources/application-dev.yml：
```yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/campus_delivery_db...
      password: YOUR_PASSWORD
  redis:
    host: localhost
    port: 6379
  elasticsearch:
    uris: http://localhost:9201  # 注意端口
  rabbitmq:
    host: localhost
    port: 5672

campus:
  alioss:
    bucket-name: YOUR_BUCKET  # 图片上传需配置 OSS
```
### 启动步骤
1. 数据库初始化：执行 SQL 脚本创建数据库表结构。
2. 中间件启动：确保 Redis, RabbitMQ, Elasticsearch 已正常运行。
3. 后端启动：运行 CampusApplication.java。
4. 接口文档：访问 http://localhost:8080/doc.html 查看 Knife4j 在线文档（若提示“URL拼写可能存在错误”，请检查应用是否启动成功、端口是否被占用，或确认 Knife4j 依赖是否正确配置）。

---

## 贡献与支持 | Contribution
欢迎提交 Issue 和 Pull Request！

- Author: Yuan Yufan (donmgx)
- Email: yufanyuan70@gmail.com
