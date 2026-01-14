package com.campus.entity.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Document(indexName = "campus_setmeal")
public class SetmealDoc implements Serializable {

    @Id
    private Long id;

    //分类id
    @Field(type = FieldType.Long)
    private Long categoryId;

    //套餐名称
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    //套餐价格
    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal price;

    //状态 0:停用 1:启用
    @Field(type = FieldType.Integer)
    private Integer status;

    //描述信息
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String description;

    //图片
    @Field(type = FieldType.Keyword, index = false)
    private String image;
}
