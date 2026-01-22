package com.campus.controller.rider;

import com.campus.constant.MessageConstant;
import com.campus.dto.RiderDTO;
import com.campus.dto.RiderLoginDTO;
import com.campus.result.Result;
import com.campus.service.RiderService;
import com.campus.vo.RiderLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/rider/rider")
public class RiderController {
    @Autowired
    private RiderService riderService;



    /*
    * 注册成为骑手
    * */
    @PostMapping("/register")
    public Result register(@RequestBody RiderLoginDTO riderLoginDTO){
        log.info("用户名：{} 申请注册成为骑手",riderLoginDTO.getName());
        riderService.register(riderLoginDTO);
        log.info("用户名：{} 成功注册成为骑手",riderLoginDTO.getName());
        return Result.success(MessageConstant.RIDER_REGISTER_SUCCESS);
    }

    /*
    * 骑手登录
    * */
    @PostMapping("/login")
    public Result login(@RequestBody RiderLoginDTO riderLoginDTO){
        log.info("用户：{} 登录",riderLoginDTO.getPhone());
        RiderLoginVO login = riderService.login(riderLoginDTO);
        return Result.success(login);
    }

    /*
    * 修改骑手信息
    * */
    @PostMapping("/update")
    public Result updateById(@RequestBody RiderDTO riderDTO){
        log.info("骑手id为：{} 修改个人信息",riderDTO.getId());
        riderService.updateById(riderDTO);
        log.info("骑手id为：{} 信息修改成功",riderDTO.getId());
        return Result.success(MessageConstant.UPDATE_SUCCESS);
    }
}
