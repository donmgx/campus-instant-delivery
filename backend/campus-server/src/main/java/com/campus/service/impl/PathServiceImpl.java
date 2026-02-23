package com.campus.service.impl;

import com.campus.constant.MessageConstant;
import com.campus.service.PathService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PathServiceImpl implements PathService {

    /*
     * 生成小程序路径
     * */
    public List<String> generate(Integer start, Integer end) throws Exception {
        if (start > end) {
            throw new Exception(MessageConstant.TABLE_RANGE_IS_INCORRECT);
        }
        //生成小程序路径
        List<String> pathList = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            pathList.add("pages/index/index?tableName=" + i);
        }

        return pathList;
    }
}
