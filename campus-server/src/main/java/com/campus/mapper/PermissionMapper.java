package com.campus.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper {


    /*
     * 根据roleId查询权限信息
     * */
    List<String> selectPermissionByRoleId(Integer roleId);

}
