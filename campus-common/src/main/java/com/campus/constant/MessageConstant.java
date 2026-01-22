package com.campus.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {

    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String ACCOUNT_LOCKED = "账号被锁定";
    public static final String ALREADY_EXISTS = "已存在";
    public static final String UNKNOWN_ERROR = "未知错误";
    public static final String USER_NOT_LOGIN = "用户未登录";
    public static final String CATEGORY_BE_RELATED_BY_SETMEAL = "当前分类关联了套餐,不能删除";
    public static final String CATEGORY_BE_RELATED_BY_DISH = "当前分类关联了菜品,不能删除";
    public static final String SHOPPING_CART_IS_NULL = "购物车数据为空，不能下单";
    public static final String ADDRESS_BOOK_IS_NULL = "用户地址为空，不能下单";
    public static final String LOGIN_FAILED = "登录失败";
    public static final String UPLOAD_FAILED = "文件上传失败";
    public static final String SETMEAL_ENABLE_FAILED = "套餐内包含未启售菜品，无法启售";
    public static final String SETMEAL_NOT_ENABLE_DELETE = "套餐内包含启售菜品，无法删除";
    public static final String PASSWORD_EDIT_FAILED = "密码修改失败";
    public static final String PASSWORD_CAN_NOT_SAME = "新密码不能与旧密码相同";
    public static final String DISH_ON_SALE = "起售中的菜品不能删除";
    public static final String SETMEAL_ON_SALE = "起售中的套餐不能删除";
    public static final String DISH_BE_RELATED_BY_SETMEAL = "当前菜品关联了套餐,不能删除";
    public static final String ORDER_STATUS_ERROR = "订单状态错误";
    public static final String ORDER_NOT_FOUND = "订单不存在";
    public static final String DISH_NOT_FAILED_BECAUSE_SETMEAL = "套餐已启售，菜品不能停售";
    public static final String ORDER_TIMEOUT = "订单超时，自动取消";
    public static final String PICK_UP_IN_STORE = "到店自取";
    public static final String TAKEAWAY_DELIVERY = "外卖配送";
    public static final String TABLE_RANGE_IS_INCORRECT = "桌号范围有误";
    public static final String TABLE_NUMBER = "桌号";
    public static final String AUTHENTICATION_FAILED = "认证失败，请重新登录";
    public static final String INSUFFICIENT_PERMISSIONS = "您的权限不足";
    public static final String NOT_SUBMIT_REPEATEDLY_OR_TOKEN_EXPIRED = "您的请求已处理，请勿重复提交";
    public static final String TOKEN_EXPIRED_OR_BELONGS_TO_OTHERS = "幂等性Token已过期或属于其他用户";
    public static final String IDEMPOTENT_TOKEN_NOT_EXIST = "幂等性校验 Token 不存在";
    public static final String CAPTCHA_VERIFICATION_NOT_COMPLETE = "请先完成滑动验证";
    public static final String COUPON_NOT_EXIST = "优惠券不存在";
    public static final String COUPON_IS_ONGOING = "活动正在进行";
    public static final String COUPON_REMAINCOUNT_IS_ZERO = "优惠券库存为零";
    public static final String COUPON_IS_ENDED = "活动已结束，禁止修改";
    public static final String REPEAT_CLAIM = "亲，您已经领取过了呦";
    public static final String COUPONS_HAVE_BEEN_SNAPPED_UP = "手慢了，优惠券已抢光";
    public static final String COUPON_CLAIM_SUCCESS = "抢购成功";
    public static final String COUPON_STATUS_ERROR = "优惠未开始或已结束";
    public static final String COUPON_USED_OR_EXPIRED = "优惠已使用或已过期";
    public static final String COUPON_THRESHOLD_NOT_REACHED = "未达到优惠券使用门槛";
    public static final String COUPON_RECOMMENDED = "已自动为您选取最佳优惠券";
    public static final String COUPON_NO_AVAILABLE = "暂无可用优惠券";
    public static final String DISH_NOT_FOUND = "菜品不存在";
    public static final String SETMEAL_NOT_FOUND = "套餐不存在";
    public static final String SYSTEM_ERROR = "系统异常";
    public static final String SYSTEM_BUSY = "系统繁忙";
    public static final String SIGN_SUCCESS = "签到成功";
    public static final String SIGNED_IN_TODAY = "今日已签到，无需重复签到";
    public static final String RIDER_EXIST = "当前骑手已存在";
    public static final String PHONE_OR_PASSWORD_IS_NULL = "当前手机号或密码为空";
    public static final String NAME_IS_NULL = "当前用户名为空";
    public static final String RIDER_REGISTER_SUCCESS = "注册成功";
    public static final String UPDATE_SUCCESS = "修改成功";
    public static final String ORDER_SNAPPED_UP = "手慢了亲，订单已被抢走";
    public static final String TAKE_ORDER_SUCCESS = "抢单成功";
    public static final String ORDER_COMPLETE = "已完成订单";
    public static final String ORDER_OPERATION_FORBIDDEN = "您无权操作他人订单";
    public static final String ORDER_NOT_EXIST = "订单不存在";
    public static final String TIME_ERROR = "时间错误";
    public static final String CANCEL_ORDER_SUCCESS = "订单取消成功";
    public static final String NEW_ORDER_NOTICE = "你有新的外卖订单待抢，请及时处理！";
    public static final String PICKUP_CODE_ERROR = "取餐码错误";
}
