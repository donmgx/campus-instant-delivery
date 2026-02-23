package com.campus.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/*
* 骑手的实体类
* */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rider implements Serializable {

    private Long id;
    private Long roleId;
    private String name;
    private String phone;
    private Integer sex; // 0 女  1 男
    private Integer status; //状态 0:禁用 1:启用
    private String password;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;

}
