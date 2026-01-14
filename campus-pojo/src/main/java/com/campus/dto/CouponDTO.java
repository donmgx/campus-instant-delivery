package com.campus.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class CouponDTO implements Serializable {
    private Long id;
    private String name;
    private Integer type;
    private Integer status;
    private BigDecimal amount;
    private BigDecimal minPoint;
    private Integer totalCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}