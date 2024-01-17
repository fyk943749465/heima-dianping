package com.dianping.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dianping.dto.Result;
import com.dianping.entity.SeckillVoucher;
import com.dianping.entity.Voucher;
import com.dianping.entity.VoucherOrder;
import com.dianping.mapper.VoucherOrderMapper;
import com.dianping.service.ISeckillVoucherService;
import com.dianping.service.IVoucherOrderService;
import com.dianping.service.IVoucherService;
import com.dianping.util.RedisIDWorker;
import com.dianping.util.SimpleRedisLock;
import com.dianping.util.UserHolder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.aop.framework.AopProxy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.*;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIDWorker idWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 秒杀脚本，判断是否有库存，已经扣减库存，下单
     * 由于逻辑较多，封装在 lua 脚本中，保证操作的原子性
     */
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    /**
     * 初始化 unlock.lua 脚本
     */
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        // 设置脚本的位置 -- classpath 下的 unlock.lua 文件
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        // 返回类型（与脚本的返回类型一致）
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private ArrayBlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<VoucherOrder>(1024*1204);

    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    private class  VoucherOrderHandler implements Runnable {

        @Override
        public void run() {

            try {
                // 1. 获取阻塞队列中的订单信息
                VoucherOrder voucherOrder = orderTasks.take();
                // 2. 创建订单
                handleVoucherOrder(voucherOrder);
            } catch (Exception e) {
                log.error("处理订单异常", e);
            }
        }
    }

    private  IVoucherOrderService proxy;
    /**
     * 处理订单
     * @param voucherOrder
     */
    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        // 这里的userId不能使用UserHolder去取，因为这是另外一个线程执行的方法，是取不到的
        RLock lock = redissonClient.getLock("lock:order:" + voucherOrder.getUserId());
        boolean isLock = lock.tryLock(); // 默认的参数是不重试，等待30秒超时释放锁
        if (!isLock) {
            // 获取锁失败
            log.error("不允许重复下单");
            return;
        }
        try {
            proxy.createVoucherOrder2(voucherOrder);
        } finally {
            lock.unlock(); // 释放锁
        }
    }

    /**
     * 依赖注入完成，
     * 狗仔函数执行完成后，
     * 自动执行该方法
     */
    @PostConstruct
    public void init() {
        // 开启一个线程，处理任务
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    /**
     * 异步秒杀，让耗时操作，交给另外一个线程处理。
     * @param voucherId
     * @return
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        // 获取用户
        Long userId = UserHolder.getUser().getId();

        // 1. 执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString()
        );
        int r = result.intValue();
        // 2.1 不为0，代表没有购买资格
        if (r != 0) { // 表示没有购买资格，
            // 返回异常信息
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        // 2. 判断结果是否为0
        long orderId = idWorker.nextId("order");
        // 2.2 为0，有购买资格，把下单保存到阻塞队列中

        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        // 订单保存到阻塞队列中
        orderTasks.add(voucherOrder);
        // 获取代理对象，是为了让其他线程使用
        proxy = (IVoucherOrderService)AopContext.currentProxy();
        return Result.ok(orderId);
    }


    /**
     * 这个代码最大的问题，就是性能问题。
     * 因为，这里多次对数据库进行了操作，而操作数据库是比较耗时的操作
     * @param voucherId
     * @return
     */
    public Result seckillVoucher2(Long voucherId) {
        // 1. 查询优惠券是否开始
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        // 2. 判断秒杀是否开始
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀活动尚未开始");
        }
        // 3. 判断秒杀是否结束
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀活动已经结束");
        }
        // 4. 判断库存是否充足
        if (voucher.getStock() < 1) {
            return Result.fail("库存不足");
        }
        // 一人一单的判断
        Long userId = UserHolder.getUser().getId();
        // 锁的力度不要太大，只要对用户id加锁，因为每次调用 toString方法，都生成一个字符串对象，
        // 而不是同一个字符串对象，因此使用intern方法，在jvm常量池中，找到同一个字符串对象加锁
        // id值一样的作为同一把锁，要保证是同一个对象，而不是全新的对象
        // 这里是为了一人一单问题

        // synchronized(userId.toString().intern()) {  //  synchronized 加锁，没办法进程间加锁，多个实例的情况下，就会有并发安全问题

        // 自己实现的锁
        // SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);

        // boolean isLock = lock.tryLock(200);

        // 使用 redisson 框架提供的锁
        RLock lock = redissonClient.getLock("lock:order:" + userId);

        boolean isLock = lock.tryLock(); // 默认的参数是不重试，等待30秒超时释放锁

        if (!isLock) {
            // 获取锁失败
            return Result.fail("不允许重复下单");
        }

        try {
            // 事物注解 @Transactional 和 synchronized 锁 的先后顺序，需要体会。
            // 如果是在 createVoucherOrder 方法内部加锁，那么 事物可能还没提交
            // 如果在方法createVoucherOrder上加锁，锁的粒度又太大了。
            // 因此，对某个对象加锁，这样的粒度比较合适。不会影响其他的用户

            // @Transactional 这个注解要想生效，是spring对当前对象做了动态代理，要想保证事物，不能用当前对象调用@Transactional
            // 标注的方法，因为这样事务会失效，要用代理类对象调用，保证事务的正确执行。
            IVoucherOrderService proxy = (IVoucherOrderService)AopContext.currentProxy();

            // 这么做，需要在启动类上添加 @EnableAspectJAutoProxy(exposeProxy = true) 并且加入 aspectjweaver 依赖
            return proxy.createVoucherOrder(voucherId, userId);
        } finally {
            lock.unlock(); // 释放锁
        }
    }

    @Transactional
    public Result createVoucherOrder(Long voucherId, Long userId) {

        Long count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        if (count > 0) {
            return Result.fail("该用户已经有该优惠券，不能再次下单");
        }
        // 5. 扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)  // 基于乐观锁的思想，只要库存大于0，就可以做这个操作
                .update();
        if (!success) {
            return Result.fail("库存不足");
        }
        // 6. 创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(idWorker.nextId("order"));
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        save(voucherOrder);
        return Result.ok(voucherOrder);
    }

    @Override
    @Transactional
    public void createVoucherOrder2(VoucherOrder voucherOrder) {
        Long count = query().eq("user_id", voucherOrder.getUserId()).eq("voucher_id", voucherOrder.getVoucherId()).count();
        if (count > 0) {
            log.error("该用户已经有该优惠券，不能再次下单");
        }
        // 5. 扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherOrder.getVoucherId())
                .gt("stock", 0)  // 基于乐观锁的思想，只要库存大于0，就可以做这个操作
                .update();
        if (!success) {
            log.error("库存不足");
            return;
        }
        // 6. 创建订单
        save(voucherOrder);
    }
}
