package com.campus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j //记录日志
@EnableAsync //开启异步执行
@EnableCaching //开启缓存
@EnableScheduling  //开启spring task 定时任务调度
@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
public class CampusApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusApplication.class, args);
        log.info("server started");
    }
}
