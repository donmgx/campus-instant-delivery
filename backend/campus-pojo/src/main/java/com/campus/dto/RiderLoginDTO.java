package com.campus.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RiderLoginDTO implements Serializable {

    private String name;

    private String sex;

    private String phone;

    private String password;

}
