package com.campus.entity.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Document(indexName = "campus_dish")
public class DishDoc implements Serializable {

    @Id
    private Long id;

    //菜品名称,分词查询
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String name;

    //菜品分类id
    @Field(type = FieldType.Long)
    private Long categoryId;

    //菜品价格
    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal price;

    //图片,不索引，只存储用于展示
    @Field(type = FieldType.Keyword, index = false)
    private String image;

    //描述信息
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String description;

    //0 停售 1 起售
    @Field(type = FieldType.Integer)
    private Integer status;

/*    //销量,用于排序
    @Field(type = FieldType.Integer)
    private Integer sales;*/


}
