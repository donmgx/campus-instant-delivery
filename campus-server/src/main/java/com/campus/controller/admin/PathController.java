package com.campus.controller.admin;

import com.campus.result.Result;
import com.campus.service.PathService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Pattern;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/table")
@Validated
public class PathController {
    @Autowired
    private PathService pathService;

    /*
     * 生成小程序路径
     * */
    @GetMapping("/generate")
    public Result<List<String>> generate(@Pattern(regexp = "^\\S{1,10}$") Integer start, @Pattern(regexp = "^\\S{1,10}$") Integer end) throws Exception {
        log.info("生成小程序路径,桌号范围：{}~{}", start, end);
        List<String> pathList = pathService.generate(start, end);
        return Result.success(pathList);
    }
}
