package com.campus.service.impl;

import com.campus.config.RabbitConfig;
import com.campus.constant.MessageConstant;
import com.campus.constant.StatusConstant;
import com.campus.context.BaseContext;
import com.campus.dto.CouponDTO;
import com.campus.dto.CouponPageQueryDTO;
import com.campus.entity.Coupon;
import com.campus.entity.es.CouponDoc;
import com.campus.exception.BaseException;
import com.campus.mapper.CouponMapper;
import com.campus.repository.CouponDocRepository;
import com.campus.result.PageResult;
import com.campus.service.CouponService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class CouponServiceImpl implements CouponService {
    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private CouponDocRepository couponDocRepository;

    private static final String SECKILL_COUPON_USER = "seckill_coupon_users_";

    private static final String SECKILL_COUPON_STOCK_KEY = "seckill_coupon_stock_";


    //预加载Lua脚本
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }


    /*
     * 新增优惠券
     * */
    public void addCoupon(CouponDTO couponDTO) {
        //创建Coupon的对象
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(couponDTO, coupon);

        //设置其他属性
        //设置状态为未开始
        coupon.setStatus(StatusConstant.NOT_START);
        //未开始时剩余数量的等于总数
        coupon.setRemainingCount(couponDTO.getTotalCount());

        couponMapper.insert(coupon);

        log.info("新增优惠券成功：ID：{}", coupon.getId());
    }

    /*
     * 根据id查询优惠券
     * */
    public Coupon getById(Long id) {
        return couponMapper.getById(id);
    }


    /*
     * 优惠券的分页查询
     * */
    public PageResult listPage(CouponPageQueryDTO couponPageQueryDTO) {
        PageHelper.startPage(couponPageQueryDTO.getPage(), couponPageQueryDTO.getPageSize());

        Page<Coupon> page = couponMapper.selectPage(couponPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }


    /*
     * 修改优惠券
     * */
    public void edit(CouponDTO couponDTO) {
        //如果活动已结束即优惠券库存为零或正在进行中禁止修改
        Coupon coupon = couponMapper.getById(couponDTO.getId());
        if (coupon.getRemainingCount() == StatusConstant.ZERO) {
            log.info("活动已结束，优惠券修改失败:id{}", coupon.getId());
            throw new BaseException(MessageConstant.COUPON_IS_ENDED);
        }

        if (coupon.getStatus() == StatusConstant.ONGOING) {
            log.info("活动进行中，优惠券修改失败:id{}", coupon.getId());
            throw new BaseException(MessageConstant.COUPON_IS_ONGOING);
        }

        BeanUtils.copyProperties(couponDTO, coupon);

        couponMapper.update(coupon);

    }


    /*
     * 秒杀活动开始
     * */
    public void startSeckill(Long id) {
        //查询该优惠券并判断
        Coupon coupon = couponMapper.getById(id);
        if (coupon == null) {
            throw new BaseException(MessageConstant
                    .COUPON_NOT_EXIST);
        }
        if (coupon.getStatus() != StatusConstant.NOT_START) {
            throw new BaseException(MessageConstant
                    .COUPON_IS_ONGOING);
        }
        if (coupon.getRemainingCount() == 0) {
            throw new BaseException(MessageConstant
                    .COUPON_REMAINCOUNT_IS_ZERO);
        }

        coupon.setStatus(StatusConstant.ONGOING);
        //更改优惠券状态
        couponMapper.update(coupon/*Coupon
                .builder()
                .status(StatusConstant.ONGOING)
                .build()*/);

        //写入Redis
        String key = SECKILL_COUPON_STOCK_KEY + id;
        stringRedisTemplate.opsForValue().set(key, String.valueOf(coupon.getRemainingCount()));

        //同步写入ES
        CouponDoc couponDoc = new CouponDoc();
        BeanUtils.copyProperties(coupon, couponDoc);
        //如果id不存在则新增，存在则更新
        couponDocRepository.save(couponDoc);

        log.info("活动开始，下发优惠券，优惠券id：{}，Redis Key：{}，库存：{}", id, key, coupon.getRemainingCount());

    }


    /*
     * 用户领取优惠券
     * */
    @Transactional
    public void claimCoupon(Long couponId) {
        //获取用户id
        Long userId = BaseContext.getCurrentId();

        //构造redis key
        List<String> keys = List.of(
                SECKILL_COUPON_STOCK_KEY + couponId,
                SECKILL_COUPON_USER + couponId
        );

        //执行Lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                keys,
                String.valueOf(userId)
        );

        //用户已经领取过一次
        if (result != null && result == -1) {
            throw new BaseException(MessageConstant.REPEAT_CLAIM);
        }
        //优惠券库存不足
        if (result != null && result == -2) {
            throw new BaseException(MessageConstant.COUPONS_HAVE_BEEN_SNAPPED_UP);
        }

        //Lua返回1，领取成功，异步落库：发消息给MQ
        //构造消息体
        Map<String, Object> map = new HashMap();
        map.put("userId", userId);
        map.put("couponId", couponId);
        map.put("endTime",couponMapper.getById(couponId).getEndTime().toString());
        //发送消息,队列名，消息对象
        rabbitTemplate.convertAndSend(RabbitConfig.SECKILL_QUEUE, map);

        log.info("id为{}的用户抢券id：{}成功,已发消息至MQ", userId, couponId);

    }
}
