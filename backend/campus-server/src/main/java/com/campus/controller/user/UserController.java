package com.campus.controller.user;

import com.campus.constant.JwtClaimsConstant;
import com.campus.dto.UserLoginDTO;
import com.campus.entity.User;
import com.campus.properties.JwtProperties;
import com.campus.result.Result;
import com.campus.service.UserService;
import com.campus.utils.JwtUtil;
import com.campus.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user/user")
@Api(tags = "C端用户相关接口")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    /*
     * 微信登录
     * */
    @PostMapping("/login")
    @ApiOperation("微信登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {  //code
        log.info("微信登录,授权码:{}", userLoginDTO.getCode());

        User user = userService.wxLogin(userLoginDTO);

        //生成 JWT令牌
        Map<String, Object> claims = new HashMap<>();

        claims.put(JwtClaimsConstant.USER_ID, user.getId());

        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        //返回响应
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();

        return Result.success(userLoginVO);
    }
}
