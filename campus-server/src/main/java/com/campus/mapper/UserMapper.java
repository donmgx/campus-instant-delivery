package com.campus.mapper;

import com.campus.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /*
    * 根据openid查询用户
    * */
    @Select("select * from campus_delivery.user where openid = #{openid}")
    User getByOpenId(String openid);

    /*
    * 插入用户
    * */
    void insert(User user);


    /*
    *
    * */
    @Select("select * from campus_delivery.user where id = #{id}")
    User getById(Long userId);


    /*
    * 获取用户数
    * */
    Integer countByMap(Map map);
}
