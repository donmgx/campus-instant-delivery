package com.campus.controller.rider;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.campus.constant.MessageConstant;
import com.campus.context.BaseContext;
import com.campus.result.Result;
import com.campus.service.RiderOrderService;
import com.campus.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/rider/order")
public class RiderOrderController {
    @Autowired
    private RiderOrderService riderOrderService;

    /**
     * 查询待抢订单列表
     * 场景：骑手打开“抢单大厅”，看到所有能抢的单子
     */
    @GetMapping("/waitList")
    @ApiOperation("查询待抢订单")
    public Result<List<OrderVO>> getWaitList() {
        log.info("骑手{}查询可派送订单", BaseContext.getCurrentId());
        List<OrderVO> list = riderOrderService.getWaitList();
        return Result.success(list);
    }

    /*
     * 骑手抢单
     * */
    @GetMapping("/{orderId}")
    @ApiOperation("骑手抢单")
    @SentinelResource(value = "riderTakeOrder", blockHandler = "takeOrderBlockHandler")
    public Result takeOrder(@PathVariable Long orderId) {
        Long riderId = BaseContext.getCurrentId();
        log.info("骑手{}抢单{}", riderId, orderId);
        riderOrderService.takeOrder(riderId, orderId);
        log.info("骑手{}抢单{}成功", riderId, orderId);
        return Result.success(MessageConstant.TAKE_ORDER_SUCCESS);
    }

    public Result takeOrderBlockHandler(Long id, BlockException ex) {
        log.warn("抢单接口流量过大，触发限流保护！");
        return Result.error("抢单人数过多，请手动刷新");
    }


    /*
     * 骑手查看我的已接订单
     * */
    @GetMapping("/runningList")
    @ApiOperation("查询待配送订单")
    public Result<List<OrderVO>> getMyRunningList(Integer status) {
        Long riderId = BaseContext.getCurrentId();
        log.info("骑手{}查询待配送订单，订单状态:{}", riderId, status);
        return Result.success(riderOrderService.getMyRunningList(riderId, status));
    }


    /*
     * 订单派送完成
     * */
    @PostMapping("complete/{orderId}")
    public Result completeOrder(@PathVariable Long orderId, @RequestParam String pickupCode) {
        Long riderId = BaseContext.getCurrentId();
        log.info("骑手{}完成订单{}，配送码：{}", riderId, orderId, pickupCode);
        riderOrderService.completeOrder(riderId, orderId, pickupCode);
        return Result.success(MessageConstant.ORDER_COMPLETE);
    }

    /*
     * 骑手取消订单配送
     * */
    @PostMapping("cancel/{orderId}")
    public Result cancelOrder(@PathVariable Long orderId) {
        Long riderId = BaseContext.getCurrentId();
        log.info("骑手{}取消订单配送，订单id：{}", riderId, orderId);
        riderOrderService.cancelOrder(riderId, orderId);
        return Result.success(MessageConstant.CANCEL_ORDER_SUCCESS);
    }
}
