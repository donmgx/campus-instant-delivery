package com.campus.controller.admin;

import com.campus.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

//该Bean的名称与user中的是相同的，都是shopController，需指定bean的名称
@RestController("adminShopController")
@Slf4j
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关操作")
public class ShopController {
    public static final String key = "SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;

    /*
     * 设置营业状态
     * */
    @PutMapping("/{status}")
    @ApiOperation("设置营业状态")
    public Result setStatus(@PathVariable Integer status) {

        log.info("设置营业状态为:{}", status == 1 ? "营业中" : "打烊中");
        //可以使用redis而非mysql，注入配置类RedisTemplate
        redisTemplate.opsForValue().set(key, status);
        return Result.success();
    }

    /*
     * 获取营业状态
     * */
    @GetMapping("/status")
    @ApiOperation("获取营业状态")
    public Result getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(key);
        log.info("获取营业状态为:{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);

    }
}
