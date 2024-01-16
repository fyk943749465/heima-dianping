package com.dianping.controller;

import com.dianping.dto.Result;
import com.dianping.entity.Voucher;
import com.dianping.entity.VoucherOrder;
import com.dianping.service.IVoucherService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Resource
    private IVoucherService voucherService;

    @PostMapping("seckill")
    public Result addSeckillVoucher(@RequestBody Voucher voucher) {

        voucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }
}
