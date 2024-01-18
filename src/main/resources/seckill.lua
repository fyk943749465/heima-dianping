-- 1. 参数列表
-- 1.1. 优惠券id
local voucherId = ARGV[1]
-- 1.2. 用户id
local userId = ARGV[2]
-- 1.3. 订单Id
local orderId = ARGV[3]


-- 2. 数据key
-- 2.1 库存key
local stockKey = 'seckill:stock:' .. voucherId
-- 2.2 订单key
local orderKey = 'seckill:order:' .. voucherId

-- 3. 脚本业务
-- 3.1 判断库存是否充足
if(tonumber(redis.call('get', stockKey)) <= 0) then
	-- 库存不足
	return 1
end

-- 3.2 判断用户是否下单
if(redis.call('sismember', orderKey, userId) == 1) then
    -- 3.3 存在，说明已经下单，不允许再次下单
	return 2
end

-- 3.4 扣减库存
redis.call('incrby', stockKey, -1)
-- 3.5 下单
redis.call('sadd', orderKey, userId)
--3.6 发送消息到redis xgroup 队列当中 (在redis中提前创建好队列, xgroup create stream.orders g1 0 mkstream)
-- XADD stream.orders * k1 v1 k2 v2 ...
redis.call('XADD', 'stream.orders', '*', 'userId', userId, 'voucherId', voucherId, 'id', orderId)
return 0