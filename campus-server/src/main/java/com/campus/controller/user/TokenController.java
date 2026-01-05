package com.campus.controller.user;

import com.campus.result.Result;
import com.campus.service.TokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/token")
@Api(tags = "幂等性令牌接口")
@Slf4j
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @GetMapping
    @ApiOperation("获取幂等性Token")
    public Result<String> getToken() {
        String token = tokenService.createToken();
        return Result.success(token);
    }
}