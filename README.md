# 校园即时配送平台 (Campus Instant Delivery)

一个专为高校场景设计的即时配送系统，支持餐饮外卖、跑腿代买、到店自取等多种配送模式。项目采用前后端分离架构，完整实现管理端与用户端业务闭环，解决大学生宿舍点餐难、配送效率低等痛点。

## 项目简介
平台提供从菜品浏览、下单支付到订单处理的完整流程，目前已实现：
- **管理端**：员工管理、菜品/套餐管理、订单处理、数据统计报表、来单实时提醒
- **用户端**：商品浏览、购物车、下单、支付、催单

后续迭代方向：骑手端接入，实现三端闭环（用户-商家-骑手），支持抢单、配送轨迹实时推送等功能。

## 技术栈
- **后端**：Spring Boot 3 + MyBatis-Plus + MySQL 8 + Redis 6 + JWT + WebSocket + Swagger
- **数据库连接池**：HikariCP（高性能、轻量级）
- **其他**：Nginx 反向代理（部署使用）、Linux 后台运行
- **前端**：Vue 3 + Element Plus + ECharts（管理端页面）

## 核心功能
- 用户/管理端统一认证（JWT）
- 菜品分类、套餐管理（支持上下架、图片上传）
- 购物车与订单全流程（下单、支付、取消、催单）
- 实时来单提醒（WebSocket 推送）
- 多维度数据统计报表（ECharts 可视化）
- Redis 缓存热点数据，提升查询性能与系统响应速度

## 项目亮点（已完成优化）
- 独立完成项目工程重构与配置优化，提升代码可维护性与启动性能
- 切换至 HikariCP 高性能连接池，显著优化资源利用率与系统响应速度
- 自定义业务逻辑，支持多种配送模式扩展
- 高并发场景基础优化，为后续分布式锁、限流等功能预留空间

## 项目截图

![管理端登录页](screenshots/admin-login.png)  
![菜品管理](screenshots/dish-manage.png)  
![套餐管理](screenshots/setmeal-manage.png)  
![订单列表](screenshots/order-list.png)  
![数据统计报表](screenshots/report.png)  
![用户端下单流程](screenshots/user-order.png)

## 本地运行指南
1. 创建 MySQL 数据库 `campus_delivery`，执行 `sql/campus_delivery.sql` 完成建表与初始数据插入
2. 启动 Redis 服务
3. 修改 `src/main/resources/application-dev.yml` 中的数据库账号密码（如需）
4. 使用 IDEA 启动主类 `CampusDeliveryApplication`
5. 管理端访问：`http://localhost:8080/admin` （默认账号：admin / 123456）

## 未来迭代规划
- 骑手端开发（抢单、实时位置上报、配送轨迹）
- RBAC 基于角色的权限管理系统
- Redisson 分布式锁防止并发超卖
- Sentinel 限流熔断保护
- RocketMQ 异步消息解耦（如订单超时自动取消）
- Docker 容器化一键部署

## 声明
本项目为个人学习与求职作品，持续迭代中，代码仅供学习交流使用。

欢迎 Star ⭐ 和 Fork，有任何问题欢迎提 Issue！
