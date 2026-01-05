package com.campus.service;

public interface TokenService {
    /*
    * 创建token
    * */
    String createToken();


    /*
    * 检验token
    * */
    Boolean checkToken(String token);

}
