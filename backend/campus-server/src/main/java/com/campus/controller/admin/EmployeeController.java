package com.campus.controller.admin;

import com.anji.captcha.service.CaptchaService;
import com.campus.dto.EmployeeDTO;
import com.campus.dto.EmployeeLoginDTO;
import com.campus.dto.EmployeePageQueryDTO;
import com.campus.dto.PasswordEditDTO;
import com.campus.entity.Employee;
import com.campus.result.PageResult;
import com.campus.result.Result;
import com.campus.service.EmployeeService;
import com.campus.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private CaptchaService captchaService;//注入验证码服务

    /*
    * 登录
    * */
    @PostMapping("/login")
    @ApiOperation("员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        /*CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(employeeLoginDTO.getCaptchaVerification());
        //校验
        ResponseModel check = captchaService.verification(captchaVO);
        if (!check.isSuccess()){
            log.info("滑动验证失败：{}",check.getRepMsg());
            return Result.error(MessageConstant.CAPTCHA_VERIFICATION_NOT_COMPLETE);
        }*/

        EmployeeLoginVO employeeLoginVO = employeeService.login(employeeLoginDTO);
        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("退出登录状态")
    public Result<String> logout() {
        log.info("退出登录");
        return Result.success();
    }

    /*
     * 新增员工
     * */
    @PostMapping
    @ApiOperation("新增员工")
    @PreAuthorize("hasRole('ADMIN')")
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工:{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /*
     * 员工分页查询
     * */
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult> selectPage(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("员工分页查询:{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.selectPage(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /*
     * 启用禁用员工
     * */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用员工")
    @PreAuthorize("hasRole('ADMIN')")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用或禁用员工：状态：{},id：{}", status, id);
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    /*
     * 根据id查询员工
     * */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Employee> selectById(@PathVariable Long id) {
        log.info("根据id查询员工:id:{}", id);
        Employee employee = employeeService.selectById(id);
        return Result.success(employee);
    }

    /*
     * 编辑员工
     * */
    @PutMapping
    @ApiOperation("编辑员工信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result updateEmp(@RequestBody EmployeeDTO employeeDTO) {
        log.info("编辑的员工信息：{}", employeeDTO);
        employeeService.updateEmp(employeeDTO);
        return Result.success();
    }


    /*
     * 修改密码
     * */
    @PutMapping("/editPassword")
    public Result editPassword(@RequestBody PasswordEditDTO passwordEditDTO) {
        log.info("修改密码：id：{}", passwordEditDTO);
        employeeService.editPassword(passwordEditDTO);
        return Result.success();

    }


}
