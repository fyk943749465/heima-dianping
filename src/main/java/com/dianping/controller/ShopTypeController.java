package com.dianping.controller;

import com.dianping.dto.Result;
import com.dianping.entity.ShopType;
import com.dianping.service.IShopTypeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {


    @Resource
    private IShopTypeService shopTypeService;
    /**
     * 查询商铺类型
     * @return
     */

    @GetMapping("/list")
    public Result queryShopTypeList() {
        return shopTypeService.queryShoptypes();
    }
}
