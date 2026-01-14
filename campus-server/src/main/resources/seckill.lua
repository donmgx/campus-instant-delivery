--KEYS[1]: 库存 Key (seckill:coupon:stock:{id})
--KEYS[2]: 已领用户 Set Key (seckill:coupon:users:{categoryId})
--ARGV[1]: 用户 ID

--1.校验用户是否已领取
if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
    return -1 -- 重复领取
end

--2.校验库存是否充足
local stock = tonumber(redis.call('get', KEYS[1]))
if stock == nil or stock <= 0 then
    return -2 -- 库存不足
end

--3.扣减库存 + 记录用户
redis.call('decr', KEYS[1])
redis.call('sadd', KEYS[2], ARGV[1])

return 1 --抢购成功