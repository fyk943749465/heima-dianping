package com.dianping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dianping.entity.User;
import com.dianping.entity.Voucher;

public interface IVoucherService extends IService<Voucher> {
    void addSeckillVoucher(Voucher voucher);
}
