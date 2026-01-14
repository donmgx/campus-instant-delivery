package com.campus.config;

import org.springframework.amqp.core.Queue;
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

    //JSON序列化，使管理端看见JSON数据
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}
