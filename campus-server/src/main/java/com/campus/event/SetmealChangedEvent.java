package com.campus.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetmealChangedEvent {

    //套餐id
    private List<Long> setmealIds;

    //操作类型
    private Integer type; //1更新或插入  2删除

    public static final Integer OPERATE_SYNC = 1;

    public static final Integer OPERATE_DELETE = 2;
}
