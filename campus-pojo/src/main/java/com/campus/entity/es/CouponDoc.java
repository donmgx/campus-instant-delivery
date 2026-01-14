package com.campus.entity.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(indexName = "seckill_coupon")
public class CouponDoc implements Serializable {
    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String name;

    @Field(type = FieldType.Integer)
    private Integer type; // 1:满减 2:折扣

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal amount;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal minPoint; //使用门槛

    @Field(type = FieldType.Integer)
    private Integer status; // 0:未开始 1:进行中 2:已结束

    @Field(type = FieldType.Integer)
    private Integer remainingCount;//剩余数量

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime startTime;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime endTime;


}
