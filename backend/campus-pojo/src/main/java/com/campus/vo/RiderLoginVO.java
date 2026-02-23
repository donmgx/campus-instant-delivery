package com.campus.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class RiderLoginVO implements Serializable {
    private Long id;
    private String phone;
    private String sex;
    private String name;
    private String token;
}
