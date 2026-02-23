package com.campus.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/*
* RabbitMQ 配置类
* */
@Configuration
public class RabbitConfig {
    //队列名
    public static final String SECKILL_QUEUE = "seckill.coupon.queue";

    //定义队列,durable:true,持久化，重启MQ队列还在
    @Bean
    public Queue seckillQueue(){
        return new Queue(SECKILL_QUEUE,true);
    }

    // 2. 新增的订单延迟队列配置 (用于超时取消)
    // 2.1 延迟队列（死等队列，消息在这里过期）
    public static final String QUEUE_TTL_ORDER = "campus.order.ttl.queue";
    // 2.2 死信交换机（收尸的交换机）
    public static final String EXCHANGE_DLX = "campus.order.dlx.exchange";
    // 2.3 死信队列（真正处理超时逻辑的队列）
    public static final String QUEUE_DLX_ORDER = "campus.order.dlx.queue";

    // 定义路由键
    public static final String ROUTING_KEY_DLX = "campus.order.timeout";

    /**
     * 定义延迟队列 (消息过期后转发给死信交换机)
     */
    @Bean
    public Queue ttlQueue() {
        return QueueBuilder.durable(QUEUE_TTL_ORDER)
                .deadLetterExchange(EXCHANGE_DLX) // 绑定死信交换机
                .deadLetterRoutingKey(ROUTING_KEY_DLX) // 绑定死信路由键
                .ttl(15 * 60 * 1000) // 统一设置过期时间 15分钟 (单位毫秒)
                .build();
    }

    /**
     * 定义死信交换机
     */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(EXCHANGE_DLX);
    }

    /**
     * 定义死信队列 (消费者监听这个)
     */
    @Bean
    public Queue dlxQueue() {
        return new Queue(QUEUE_DLX_ORDER);
    }

    /**
     * 绑定死信队列到死信交换机
     */
    @Bean
    public Binding bindingDlx() {
        return BindingBuilder.bind(dlxQueue())
                .to(dlxExchange())
                .with(ROUTING_KEY_DLX);
    }

    //消息转换器，JSON序列化，使管理端看见JSON数据
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}
