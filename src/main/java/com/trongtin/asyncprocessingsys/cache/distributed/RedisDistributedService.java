package com.trongtin.asyncprocessingsys.cache.distributed;

public interface RedisDistributedService {
    RedisDistributedLocker getDistributedLock(String lockKey);
}
