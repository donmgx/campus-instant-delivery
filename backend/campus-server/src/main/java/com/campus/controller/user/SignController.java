package com.campus.controller.user;

import com.campus.context.BaseContext;
import com.campus.result.Result;
import com.campus.service.SignService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * 用户签到相关接口
 * */
@Slf4j
@RequestMapping("user/sign")
@RestController
@Api(tags = "C端-用户签到相关接口")
public class SignController {
    @Autowired
    private SignService signService;

    /*
     * 用户签到
     * */
    @PostMapping
    @ApiOperation("签到")
    public Result sign() {
        log.info("用户{}签到", BaseContext.getCurrentId());
        return signService.sign();
    }
}
