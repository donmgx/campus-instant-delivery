package com.campus.service.impl;

import com.campus.constant.MessageConstant;
import com.campus.context.BaseContext;
import com.campus.exception.BaseException;
import com.campus.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TokenServiceImpl implements TokenService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private static final String IDEMPOTENT_TOKEN_PREFIX = "idempotent_token_";


    /*
    * 获取 幂等性 token
    * */
    public String createToken() {
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new BaseException(MessageConstant.USER_NOT_LOGIN);
        }

        String str = UUID.randomUUID().toString().replace("-", "");
        StringBuilder token = new StringBuilder();
        token.append(IDEMPOTENT_TOKEN_PREFIX).append(str);


        //存入Redis，设置过期时间5分钟
        stringRedisTemplate.opsForValue().set(token.toString(), String.valueOf(currentId), 5, TimeUnit.MINUTES);

        return token.toString();
    }

    @Override
    public Boolean checkToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BaseException(MessageConstant.IDEMPOTENT_TOKEN_NOT_EXIST);
        }

        Long currentUserId = BaseContext.getCurrentId();
        String storedUserId = stringRedisTemplate.opsForValue().get(token);

        if (storedUserId == null) {
            // Token 不存在或已过期
            throw new BaseException(MessageConstant.NOT_SUBMIT_REPEATEDLY_OR_TOKEN_EXPIRED);
        }
        //判断userId是否为当前用户
        if (!storedUserId.equals(String.valueOf(currentUserId))) {
            throw new BaseException(MessageConstant.TOKEN_EXPIRED_OR_BELONGS_TO_OTHERS);
        }

        //使用delete操作的返回值来保证原子性
        //如果删除成功，返回true，说明 Redis 里有这个 key，且被当前线程抢占删除了
        Boolean delete = stringRedisTemplate.delete(token);

        //如果删除失败,返回false，说明Redis里没有该key
        if (delete == null || !delete) {
            throw new BaseException(MessageConstant.NOT_SUBMIT_REPEATEDLY_OR_TOKEN_EXPIRED);
        }

        return true;
    }
}
