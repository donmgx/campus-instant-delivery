package com.campus.service;

import java.util.List;

public interface PathService {

    /*
     * 生成小程序路径
     * */
    List<String> generate(Integer start, Integer end) throws Exception;
}
