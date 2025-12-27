package com.campus.controller.admin;

import com.campus.dto.PasswordEditDTO;
import com.campus.result.Result;
import com.campus.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/employee")
public class PasswordController {
    @Autowired
    private EmployeeService employeeService;
    /*
     * 修改密码
     * */
    @PutMapping("/editPassword")
    public Result editPassword(@RequestBody PasswordEditDTO passwordEditDTO){
        log.info("修改密码：id：{}",passwordEditDTO);

        employeeService.editPassword(passwordEditDTO);

        return Result.success();

    }
}
