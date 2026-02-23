package com.campus.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class RiderDTO implements Serializable {

    private Long id;
    private Long roleId;
    private String name;
    private String phone;
    private Integer sex; // 0 女  1 男
    private Integer status; //状态 0:禁用 1:启用
    private String password;

}
