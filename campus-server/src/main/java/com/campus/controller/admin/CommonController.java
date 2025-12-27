package com.campus.controller.admin;

import com.campus.constant.MessageConstant;
import com.campus.result.Result;
import com.campus.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /*
     * 文件上传
     * */
    @PostMapping("upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) { //文件接收到要上传到aliyun
        log.info("文件上传：{}", file);

        try {
            /*//获取原始文件名
            String originalFilename = file.getOriginalFilename();
            //截取原始文件后缀名
            String extension = originalFilename.substring(originalFilename.indexOf("."));
            //构建新文件名
            String newName = UUID.randomUUID().toString() + extension;

            //文件的请求路径
            String filePath = aliOssUtil.upload(file.getBytes(), newName);*/
            log.info("文件上传：{}", file.getOriginalFilename());
            //交给阿里云
            String url = aliOssUtil.upload(file);
            log.info("文件上传完成，访问的URL：{}",url);
            return Result.success(url);
        } catch (IOException e) {
            log.info("文件上传失败：{}", e);
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
