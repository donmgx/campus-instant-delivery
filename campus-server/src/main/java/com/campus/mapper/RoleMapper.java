package com.campus.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper {


    /*
     * 查询角色
     * */
    @Select("select code from sys_role where id = #{roleId}")
    List<String> selectCodeByRoleId(Integer roleId);
}
