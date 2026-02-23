package com.campus.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSignedEvent {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 当前连续签到天数
     */
    private Integer continuousDays;
}