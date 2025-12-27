package com.campus.service;

import com.campus.dto.UserLoginDTO;
import com.campus.entity.User;

public interface UserService {

    /*
    * 微信登录
    * */
    User wxLogin(UserLoginDTO userLoginDTO);
}
