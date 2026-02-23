package com.campus.service.impl;

import com.campus.constant.*;
import com.campus.dto.RiderDTO;
import com.campus.dto.RiderLoginDTO;
import com.campus.entity.Rider;
import com.campus.exception.BaseException;
import com.campus.mapper.RiderMapper;
import com.campus.properties.JwtProperties;
import com.campus.service.RiderService;
import com.campus.utils.JwtUtil;
import com.campus.vo.RiderLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RiderServiceImpl implements RiderService {
    @Autowired
    private RiderMapper riderMapper;

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    @Qualifier("myRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;


    /*
     * 注册成为骑手
     * */
    public void register(RiderLoginDTO riderLoginDTO) {

        String phone = riderLoginDTO.getPhone();
        String password = riderLoginDTO.getPassword();
        String name = riderLoginDTO.getName();

        if (phone == null || password == null) {
            throw new BaseException(MessageConstant.PHONE_OR_PASSWORD_IS_NULL);
        }
        if (name == null) {
            throw new BaseException(MessageConstant.NAME_IS_NULL);
        }

        // 判断该手机号是否存在
        Rider existRider = riderMapper.getByPhone(riderLoginDTO.getPhone());
        if (existRider != null) {
            throw new BaseException(MessageConstant.RIDER_EXIST);
        }

        try {
            // 对象转换
            Rider rider = new Rider();
            BeanUtils.copyProperties(riderLoginDTO, rider);

            // 密码加密
            String encodePassword = passwordEncoder.encode(rider.getPassword());

            rider.setPassword(encodePassword);
            rider.setStatus(StatusConstant.ENABLE);
            rider.setRoleId(RoleConstant.RIDER);
            rider.setCreateTime(LocalDateTime.now());
            rider.setUpdateTime(LocalDateTime.now());
            //插入数据库
            riderMapper.insert(rider);

            // 更新字段
            rider.setCreateUser(rider.getId());
            rider.setUpdateUser(rider.getId());
            riderMapper.update(rider);

        } catch (BeansException e) {
            log.error("手机号为：{} 的骑手注册失败", riderLoginDTO.getPhone());
            throw new BaseException(MessageConstant.SYSTEM_ERROR);
        }

    }

    /*
     * 用户登录
     * */
    public RiderLoginVO login(RiderLoginDTO riderLoginDTO) {

        String phone = riderLoginDTO.getPhone();
        String password = riderLoginDTO.getPassword();

        if (phone == null || password == null) {
            throw new BaseException(MessageConstant.PHONE_OR_PASSWORD_IS_NULL);
        }

        // 判断登录账号是否存在
        Rider existRider = riderMapper.getByPhone(riderLoginDTO.getPhone());
        if (existRider == null) {
            throw new BaseException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 判断是否禁用
        if (existRider.getStatus() == 0) {
            throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
        }
        //判断密码是否正确
        boolean matches = passwordEncoder.matches(riderLoginDTO.getPassword(), existRider.getPassword());
        if (!matches) {
            // 如果密码不正确
            throw new BaseException(MessageConstant.PASSWORD_ERROR);
        }

        // 下发JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.RIDER_ID, existRider.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getRiderSecretKey(),
                jwtProperties.getRiderTtl(),
                claims
        );

        RiderLoginVO riderLoginVO = new RiderLoginVO();
        BeanUtils.copyProperties(existRider, riderLoginVO);
        riderLoginVO.setToken(token);

        // 写入 redis
        String key = RedisConstant.LOGIN_RIDER_KEY + riderLoginVO.getId();
        try {
            redisTemplate.delete(key);
            redisTemplate.opsForValue().set(key, riderLoginVO, jwtProperties.getRiderTtl(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("登录用户数据存入Redis失败:{}", e.getMessage());

        }
        return riderLoginVO;
    }


    /*
    * 修改骑手个人信息
    * */
    public void updateById(RiderDTO riderDTO) {
        Rider rider = new Rider();
        BeanUtils.copyProperties(riderDTO, rider);
        riderMapper.update(rider);
    }
}
