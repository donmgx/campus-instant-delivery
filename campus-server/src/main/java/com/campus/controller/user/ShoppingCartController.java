package com.campus.controller.user;

import com.campus.dto.ShoppingCartDTO;
import com.campus.entity.ShoppingCart;
import com.campus.result.Result;
import com.campus.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Api(tags = "C端购物车相关接口")
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /*
     * 添加购物车
     * */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车:{}");

        shoppingCartService.addShoppingCart(shoppingCartDTO);

        return Result.success();

    }


    /*
     * 查询购物车
     * */
    @GetMapping("/list")
    @ApiOperation("查询购物车")
    public Result<List<ShoppingCart>> getByUserId() {

        List<ShoppingCart> shoppingCarts = shoppingCartService.getByUserId();
        log.info("查询购物车:{}", shoppingCarts);
        return Result.success(shoppingCarts);
    }


    /*
     * 清空购物车
     * */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result delete() {
        log.info("清空购物车");
        shoppingCartService.delete();
        return Result.success();
    }


    /*
    * 删除购物车中一个商品
    * */
    @PostMapping("/sub")
    @ApiOperation("删除购物车中一个商品")
    public Result deleteOne(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("删除购物车中一个商品:{}",shoppingCartDTO);
        shoppingCartService.deleteOne(shoppingCartDTO);
        return Result.success();
    }


}
