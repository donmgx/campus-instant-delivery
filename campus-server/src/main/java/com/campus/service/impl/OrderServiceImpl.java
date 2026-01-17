package com.campus.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.campus.config.RabbitConfig;
import com.campus.constant.StatusConstant;
import com.campus.exception.BaseException;
import com.campus.vo.*;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.campus.constant.MessageConstant;
import com.campus.context.BaseContext;
import com.campus.dto.*;
import com.campus.entity.*;
import com.campus.exception.AddressBookBusinessException;
import com.campus.exception.OrderBusinessException;
import com.campus.exception.ShoppingCartBusinessException;
import com.campus.mapper.*;
import com.campus.result.PageResult;
import com.campus.service.OrderService;
import com.campus.utils.WeChatPayUtil;

import com.campus.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.splitmap.AbstractIterableGetMapDecorator;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;


    /*
     * 用户下单
     * */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //获取地址簿信息
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        //创建订单对象
        Orders orders = new Orders();
        //判断取餐方式
        if (ordersSubmitDTO.getDeliveryMode() == 0) {
            //如果是到店自取，则不需要配送
            if (ordersSubmitDTO.getTableNumber() != null) {
                //如果是扫码点餐
                orders.setAddress(MessageConstant.TABLE_NUMBER + ":" + ordersSubmitDTO.getTableNumber());
            } else {
                //到店自取
                orders.setAddress(MessageConstant.PICK_UP_IN_STORE);
            }

        } else {
            //判断地址簿是否为空
            if (addressBook == null) {
                throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
            }
            //地址不为空
            orders.setAddress(addressBook.getProvinceName() + addressBook.getCityName()
                    + addressBook.getDistrictName() + addressBook.getDetail());
        }

        //判断购物车是否为空
        ShoppingCart shoppingCart = new ShoppingCart();
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        if (shoppingCarts == null && shoppingCarts.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //获取购物车商品的原价
        BigDecimal originalAmount = new BigDecimal(0);
        for (ShoppingCart cart : shoppingCarts) {
            //原本单价
            BigDecimal itemPrice = cart.getAmount();
            //每个购买数量
            BigDecimal quantity = BigDecimal.valueOf(cart.getNumber());
            //总原价
            originalAmount = originalAmount.add(itemPrice.multiply(quantity));
        }

        //实际支付价钱
        BigDecimal payAmount = originalAmount;
        //获取用户选择的优惠券
        Long userCouponId = ordersSubmitDTO.getUserCouponId();

        //如果用户使用了优惠券
        if (userCouponId != null) {
            //判断优惠券是否存在且属于该用户
            UserCoupon userCoupon = userCouponMapper.getById(userCouponId);
            if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
                throw new BaseException(MessageConstant.COUPON_NOT_EXIST);
            }

            //判断优惠券是否使用或过期
            if (userCoupon.getStatus() != StatusConstant.UNUSED) {
                throw new BaseException(MessageConstant.COUPON_USED_OR_EXPIRED);
            }

            //获取优惠券信息
            Coupon coupon = couponMapper.getById(userCoupon.getCouponId());

            //判断优惠券是否可以使用
            if (coupon.getStatus() != StatusConstant.ONGOING) {
                throw new BaseException(MessageConstant.COUPON_STATUS_ERROR);
            }

            //判断是否达到优惠券使用门槛
            if (originalAmount.compareTo(coupon.getMinPoint()) < 0) {
                throw new BaseException(MessageConstant.COUPON_THRESHOLD_NOT_REACHED);
            }

            //可以使用优惠券
            if (coupon.getType() == StatusConstant.FULL_REDUCTION) {
                //如果是满减类型
                payAmount = payAmount.subtract(coupon.getAmount());
            } else if (coupon.getType() == StatusConstant.DISCOUNT) {
                //如果类型是折扣
                payAmount = payAmount.multiply(coupon.getAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
            }

            //防止金额为负数
            if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
                payAmount = BigDecimal.ZERO;
            }

        }

        //向订单表插入一条数据
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setAmount(payAmount); //实际支付金额
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setCouponId(userCouponId); //优惠券id

        orderMapper.insert(orders);

        //向订单明细表插入多条数据
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }
        //批量插入
        orderDetailMapper.insert(orderDetails);

        //删除购物车
        if (orders.getStatus() != 1) {
            shoppingCartMapper.delete(userId);
        }

        //如果使用了优惠券，将优惠券标记为已使用
        if (userCouponId != null) {
            userCouponMapper.userCoupon(userCouponId, orders.getId(), StatusConstant.USED, LocalDateTime.now());
        }

        //返回OrderSubmitVO
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

        //发送延迟消息
        //只需要发订单ID，发到 TTL 队列
        rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_TTL_ORDER, orders.getId());
        log.info("订单创建成功，已发送延迟消息，订单ID: {}", orders.getId());

        return orderSubmitVO;
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //向客户端浏览器推送提醒 type , orderId, content
        Map map = new HashMap();
        map.put("type", 1); //1来单提醒  2 客户催单
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + outTradeNo);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);

    }


    /*
     * 查询订单详情
     * */
    public OrderVO getById(Long id) {
        //查询订单表
        Orders orders = orderMapper.getById(id);

        //查询订单详情表
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        if (orders.getDeliveryMode() == 1) {
            orderVO.setDeliveryModeName(MessageConstant.TAKEAWAY_DELIVERY);
        } else {
            orderVO.setDeliveryModeName(MessageConstant.PICK_UP_IN_STORE);
        }
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;

    }


    /*
     * 历史订单查询(分页查询)
     * */
    @Override
    public PageResult getByUserIds(OrdersPageQueryDTO ordersPageQueryDTO) {

        //查询订单表
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Long userId = BaseContext.getCurrentId();
        ordersPageQueryDTO.setUserId(userId);
        Page<Orders> page = orderMapper.selectPage(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();

        //查询订单明细
        if (page != null && page.getTotal() > 0) {
            for (Orders order : page) {
                Long orderId = order.getId();

                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                orderVO.setOrderDetailList(orderDetailList);

                list.add(orderVO);

            }
        }
        return new PageResult(page.getTotal(), list);
    }


    /*
     * 用户端取消订单
     * */
    public void concel(Long id) throws Exception {
        //先查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //待支付和待接单状态下，用户可直接取消订单
        Integer status = ordersDB.getStatus();
        if (status != 1 && status != 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //退回优惠券
        if (ordersDB.getCouponId() != null) {
            userCouponMapper.returnCoupon(ordersDB.getId(), StatusConstant.UNUSED);
        }

        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
            weChatPayUtil.refund(
                    ordersDB.getNumber(), //商户订单号
                    ordersDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            ordersDB.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        ordersDB.setStatus(Orders.CANCELLED);
        ordersDB.setCancelReason("用户取消");
        ordersDB.setCancelTime(LocalDateTime.now());
        orderMapper.update(ordersDB);

    }


    /*
     * 再来一单
     * */
    public void submitAgain(Long id) {
        //查询订单明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        Long userId = BaseContext.getCurrentId();

        List<ShoppingCart> shoppingCartList = new ArrayList<>();

        orderDetailList.stream().forEach(orderDetail -> {
            //将重新复制到购物车
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(shoppingCart);
        });

        shoppingCartMapper.insertBatch(shoppingCartList);
    }


    /*
     * 订单搜索
     * */
    public PageResult getOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        //查询订单表
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.selectPage(ordersPageQueryDTO);

        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);
    }


    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);

                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }


    /*
     * 各个状态的订单数量统计
     * */
    public OrderStatisticsVO getStatus() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();


        Integer countConfirmed = orderMapper.countByStatus(Orders.CONFIRMED);
        Integer countDelivery = orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer countToBeConfirmed = orderMapper.countByStatus(Orders.TO_BE_CONFIRMED);

        orderStatisticsVO.setConfirmed(countConfirmed);
        orderStatisticsVO.setToBeConfirmed(countToBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(countDelivery);

        return orderStatisticsVO;
    }


    /*
     * 管理端取消订单
     * */
    public void adminConcel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == 1) {
            //用户已支付，需要退款
            String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("申请退款：{}", refund);
        }

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }


    /*
     * 拒单
     * */
    public void reject(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        // 订单只有存在且状态为2（待接单）才可以拒单
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == Orders.PAID) {
            //用户已支付，需要退款
            String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("申请退款：{}", refund);
        }

        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);

    }


    /*
     * 接单
     * */
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder().id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();

        orderMapper.update(orders);
    }


    /*
     * 派送订单
     * */
    public void delivery(Long id) {

        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }


    /*
     * 完成订单
     * */
    @Override
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }


    /*
     * 催单
     * */
    public void remider(Long id) {
        Orders orders = orderMapper.getById(id);
        //检验订单是否存在
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Map map = new HashMap();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号：" + orders.getNumber());

        String json = JSON.toJSONString(map);

        webSocketServer.sendToAllClient(json);
    }


    /*
     * 预计算支付价格
     * */
    public OrderCalculateVO calculate() {
        //获取该用户信息
        Long userId = BaseContext.getCurrentId();
        //获取购物车数据
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);

        if (shoppingCarts == null) {
            //如果购物车为空，直接返回0
            return OrderCalculateVO.builder()
                    .originalAmount(BigDecimal.ZERO)
                    .discountAmount(BigDecimal.ZERO)
                    .payAmount(BigDecimal.ZERO)
                    .desc(MessageConstant.SHOPPING_CART_IS_NULL)
                    .build();
        }

        //计算原价
        BigDecimal goodsAmount = BigDecimal.ZERO;
        for (ShoppingCart cart : shoppingCarts) {
            BigDecimal amount = cart.getAmount();
            BigDecimal number = BigDecimal.valueOf(cart.getNumber());
            goodsAmount = goodsAmount.add(amount.multiply(number));
        }
        //打包费
        BigDecimal packAmount = new BigDecimal(0);
        BigDecimal totalAmount = goodsAmount.add(packAmount);

        //贪心算法：寻找最大折扣优惠券
        //初始最大优惠券id
        Long bestCouponId = null;
        //初始最大折扣金额
        BigDecimal maxDiscount = BigDecimal.ZERO;
        //查询用户所有优惠券信息
        List<UserCoupon> availableCoupons = userCouponMapper.listAvailable(userId);
        if (availableCoupons != null) {
            for (UserCoupon uc : availableCoupons) {
                Coupon coupon = couponMapper.getById(uc.getCouponId());
                //校验门槛
                if (goodsAmount.compareTo(coupon.getMinPoint()) < 0){
                    //如果未达到最小使用金额，直接下一轮
                    continue;
                }
                //校验过期时间
                if (uc.getEndTime() != null && LocalDateTime.now().isAfter(uc.getEndTime())){
                    //防止脏数据
                    continue;
                }

                //可优惠金额
                BigDecimal currentDiscount = BigDecimal.ZERO;
                //计算最大可优惠金额
                if (coupon.getType() == StatusConstant.FULL_REDUCTION){
                    //如果是满减
                    currentDiscount = coupon.getAmount();
                } else if (coupon.getType() == StatusConstant.DISCOUNT) {
                    //如果是折扣
                    currentDiscount = currentDiscount.subtract(currentDiscount.multiply(coupon.getAmount()));
                }
                //获取折扣更多的券
                if (currentDiscount.compareTo(maxDiscount) > 0){
                    maxDiscount = currentDiscount;
                    //获取券id
                    bestCouponId = coupon.getId();
                }
            }
        }

        //最大折扣后的实际价格
        BigDecimal payAmount = totalAmount.subtract(maxDiscount);
        //校验是否小于0
        if (payAmount.compareTo(BigDecimal.ZERO) < 0){
            payAmount = BigDecimal.ZERO;
        }

        return OrderCalculateVO.builder()
                .payAmount(payAmount)
                .discountAmount(maxDiscount)
                .originalAmount(goodsAmount)
                .recommendCouponId(bestCouponId)
                .desc(bestCouponId != null?MessageConstant.COUPON_RECOMMENDED:MessageConstant.COUPON_NO_AVAILABLE)
                .build();
    }
}
