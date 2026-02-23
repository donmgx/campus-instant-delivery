package com.campus.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
 * 菜品事件类
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishChangedEvent {

    //变动的菜品ID列表
    private List<Long> dishIds;

    //操作类型：1更新/新增  2删除
    private Integer type;

    public static final Integer OPERATE_SYNC = 1;

    public static final Integer OPERATE_DELETE = 2;

}
