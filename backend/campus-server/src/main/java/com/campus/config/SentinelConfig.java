package com.campus.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/*
* Sentinel 限流
* */
@Slf4j
@Configuration
public class SentinelConfig {

    // 注入 Sentinel 切面，让@SentinelResouce注解生效
    @Bean
    public SentinelResourceAspect sentinelResourceAspect(){
        return new SentinelResourceAspect();
    }

    // 初始化限流规则
    @PostConstruct
    public void initFlowRules(){
        List<FlowRule> rules = new ArrayList<>();

        //定义规则：针对资源submitOrder
        FlowRule rule = new FlowRule();
        // 1.用户下单，保护数据库 - QPS 100
        rules.add(buildRule("submitOrder",100));
        // 2. 优惠券秒杀，保护 Redis 和 线程池 - QPS 500
        rules.add(buildRule("seckillCoupon",500));
        // 3. 骑手抢单，保护 Redis 锁竞争 - QPS 50
        rules.add(buildRule("riderTakeOrder",50));

        // 加载规则
        FlowRuleManager.loadRules(rules);
        log.info("Sentinel 限流规则加载完成：下单(100), 秒杀(500), 抢单(50)");
    }

    // 构建规则
    private FlowRule buildRule(String resourceName, int count) {
        FlowRule rule = new FlowRule();
        rule.setResource(resourceName);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(count);
        return rule;
    }
}
