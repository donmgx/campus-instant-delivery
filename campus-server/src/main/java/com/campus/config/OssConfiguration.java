package com.campus.config;

import com.campus.properties.AliOssProperties;
import com.campus.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * 配置类：用于创建AliossUtil对象
 * */
@Configuration
@Slf4j
@EnableConfigurationProperties(AliOssProperties.class)
public class OssConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties) {
        log.info("开始创建文件上传对象:{}", aliOssProperties);
        AliOssUtil aliOSSUtil = new AliOssUtil();
        aliOSSUtil.setAliOSSProperties(aliOssProperties);
        return aliOSSUtil;
    }
}
