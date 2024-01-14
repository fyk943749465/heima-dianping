package com.dianping.controller;

import com.dianping.dto.Result;
import com.dianping.entity.Shop;
import com.dianping.service.IShopService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    public IShopService shopService;

    @GetMapping("/{id}")
    public Result queryShopById(@PathVariable("id")Long id) {
        return Result.ok(shopService.queryById(id));
    }


    @PutMapping
    public Result updateShop(@RequestBody Shop shop) {
        return shopService.update(shop);
    }
}
