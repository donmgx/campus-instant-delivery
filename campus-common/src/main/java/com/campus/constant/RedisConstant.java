package com.campus.constant;

/*
* 存入Redis所需的相关key的组成部分
* */
public class RedisConstant {

    // Bitmap 签到所使用的 key: "sign:record:userId:202601"
    public static final String SIGN_RECORD_KEY = "sign:record:";

    // 连续签到天数 Bitmap key: "sign:streak:userId"
    public static final String SIGN_STREAK_KEY = "sign:streak:";


}
