package com.trongtin.asyncprocessingsys.service.ticket.cache_ticket;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.trongtin.asyncprocessingsys.cache.distributed.RedisDistributedLocker;
import com.trongtin.asyncprocessingsys.cache.distributed.RedisDistributedService;
import com.trongtin.asyncprocessingsys.cache.redis.RedisInfrasService;
import com.trongtin.asyncprocessingsys.model.TicketDetailCache;
import com.trongtin.asyncprocessingsys.model.entity.TicketDetail;
import com.trongtin.asyncprocessingsys.repository.ticket.TicketDetailRepository;
import com.trongtin.asyncprocessingsys.service.ticket.TicketDetailDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TicketDetailCacheServiceRefactor {

    @Autowired
    private RedisDistributedService redisDistributedService;
    @Autowired // Khai bao cache
    private RedisInfrasService redisInfrasService;
    @Autowired
    private TicketDetailDomainService ticketDetailDomainService;

    // private static final Logger log = LoggerFactory.getLogger(TicketDetailCacheService.class);
    // use guava
    private final static Cache<Long, TicketDetailCache> ticketDetailLocalCache = CacheBuilder.newBuilder()
            .initialCapacity(10)
            .concurrencyLevel(8)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public boolean orderTicketByUser(Long ticketId){
        ticketDetailLocalCache.invalidate(ticketId); // remove local cache
        redisInfrasService.delete(genEventItemKey(ticketId));
        return true;
    }
    /**
     * get ticket item by id in cache
     */
    public TicketDetailCache getTicketDetail(Long ticketId, Long version) {
        // test: get data from mysql
//        TicketDetail ticketDetail = ticketDetailDomainService.getTicketDetailById(ticketId);
//        log.info("01: GET TICKET FROM MYSQL: ticketId: {}, version: {}", ticketId, version);
//        if (ticketDetail == null) {
//            return null;
//        }
//        return new TicketDetailCache().withClone(ticketDetail).withVersion(System.currentTimeMillis());
//         Remember that: Check validation()
        if(ticketId == null){
            return null;
        }
        // 1 - get data from local cache
        TicketDetailCache ticketDetailCache = getTicketDetailLocalCache(ticketId);

        if (ticketDetailCache != null) {

            // User:version, cache:version
            // 1. version = null
            if (version == null){
                log.info("01: GET TICKET FROM LOCAL CACHE: versionUser:{}, versionLocal: {}", version, ticketDetailCache.getVersion());
                return ticketDetailCache;
            }

            if (version.equals(ticketDetailCache.getVersion())){
                log.info("02: GET TICKET FROM LOCAL CACHE: versionUser:{}, versionLocal: {}", version, ticketDetailCache.getVersion());
                return ticketDetailCache;
            }

            // version < ticketDetailCache.getVersion()
            if (version < ticketDetailCache.getVersion()){
                log.info("03: GET TICKET FROM LOCAL CACHE: versionUser:{}, versionLocal: {}", version, ticketDetailCache.getVersion());
                return ticketDetailCache;
            }

            if (version > ticketDetailCache.getVersion()){
                return getTicketDetailDistributedCache(ticketId);
            }
//            return ticketDetailCache;
        }
        return getTicketDetailDistributedCache(ticketId);
    }

    /**
     * get ticket from database
     */
    public TicketDetailCache getTicketDetailDatabase1(Long ticketId) {
        RedisDistributedLocker locker = redisDistributedService.getDistributedLock(genEventItemKeyLock(ticketId));
        try {
            // 1 - Tao lock
            boolean isLock = locker.tryLock(1, 5, TimeUnit.SECONDS);
            // Lưu ý:  unLock
            if (!isLock) {
                return null; // return retry
            }
            // Get cache
            TicketDetailCache ticketDetailCache = redisInfrasService.getObject(genEventItemKey(ticketId), TicketDetailCache.class);
            // 2. YES
            if (ticketDetailCache != null) {
                return ticketDetailCache;
            }
           // TicketDetail ticketDetail = ticketDetailRepository.findById(ticketId);
            TicketDetail ticketDetail = ticketDetailDomainService.getTicketDetailById(ticketId);
            log.info("FROM DBS --> {}",  ticketDetail);

            if (ticketDetail == null) {
                return null;

            }
            ticketDetailCache = new TicketDetailCache().withClone(ticketDetail).withVersion(System.currentTimeMillis());
            // set data to distributed cache
            redisInfrasService.setObject(genEventItemKey(ticketId), ticketDetailCache);
            return ticketDetailCache;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            locker.unlock();
        }
    }
    public TicketDetailCache getTicketDetailDatabase(Long ticketId) {
        RedisDistributedLocker locker = redisDistributedService.getDistributedLock(genEventItemKeyLock(ticketId));
        int retryCount = 3; // Thử lại tối đa 3 lần

        while (retryCount > 0) {
            try {
                // Cố gắng đợi lấy lock trong 1 giây, lease time 5 giây
                boolean isLock = locker.tryLock(1, 5, TimeUnit.SECONDS);

                if (isLock) {
                    try {
                        // Double check: Khi lấy được lock, phải kiểm tra lại Redis một lần nữa
                        // Tránh trường hợp thread trước đó vừa mới nạp cache xong và nhả lock ra.
                        TicketDetailCache ticketDetailCache = redisInfrasService.getObject(genEventItemKey(ticketId), TicketDetailCache.class);
                        log.info("Đọc   CACHE ở đầu khi Query DB ->  ở Thread: {} , {}",  Thread.currentThread().getName(), ticketDetailCache);
                        if (ticketDetailCache == null) {
                            Thread.sleep(50);
                            ticketDetailCache = redisInfrasService.getObject(genEventItemKey(ticketId), TicketDetailCache.class);
                        }
                        if (ticketDetailCache != null) {
                            return ticketDetailCache;
                        }

                        // Nếu Redis vẫn trống thật, tiến hành chọc DB
                        TicketDetail ticketDetail = ticketDetailDomainService.getTicketDetailById(ticketId);
                        log.info("FROM DBS {}", ticketDetail);

                        if (ticketDetail == null) {
                            return null;
                        }

                        ticketDetailCache = new TicketDetailCache().withClone(ticketDetail).withVersion(System.currentTimeMillis());
                        redisInfrasService.setObject(genEventItemKey(ticketId), ticketDetailCache);
                        log.info("Đã SET CACHE sau khi Query DB ->  ở Thread: {}",  Thread.currentThread().getName());
                        ticketDetailCache = redisInfrasService.getObject(genEventItemKey(ticketId), TicketDetailCache.class);
                        log.info("Đã ĐỌC CACHE sau khi SET CACHE  ->  ở Thread: {}, cache: {}",  Thread.currentThread().getName(),ticketDetailCache );

                        return ticketDetailCache;
                    } finally {
                        locker.unlock(); // Đảm bảo luôn nhả lock khi xử lý xong
                    }
                }

                // NẾU KHÔNG LẤY ĐƯỢC LOCK (tryLock trả về false)
                retryCount--;
                log.info("Lock acquisition failed, retrying... Remaining attempts: {}", retryCount);
                Thread.sleep(100); // Ngủ 100ms chờ thread khác nạp cache xong rồi vòng lại check

                TicketDetailCache cacheCheck = redisInfrasService.getObject(genEventItemKey(ticketId), TicketDetailCache.class);
                if (cacheCheck != null) {
                    return cacheCheck;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted during lock acquisition", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    /**
     * get ticket from distributed cache
     */
    public TicketDetailCache getTicketDetailDistributedCache(Long ticketId) {
        // 1 - get data
        TicketDetailCache ticketDetailCache = redisInfrasService.getObject(genEventItemKey(ticketId), TicketDetailCache.class);
        if(ticketDetailCache == null){
            log.info("GET TICKET FROM DISTRIBUTED LOCK");
            ticketDetailCache = getTicketDetailDatabase(ticketId);
        }
        // 2 - put data to local cache
        // lock()
        ticketDetailLocalCache.put(ticketId, ticketDetailCache); //.. consistency cache
        // unLock()
        log.info("GET TICKET FROM DISTRIBUTED CACHE | {}", ticketDetailCache.getTicketDetail().getStockAvailable());
        return ticketDetailCache;
    }

    /**
     * get ticket from local cache
     */
    public TicketDetailCache getTicketDetailLocalCache(Long ticketId) {
        // get cache from GUAVA
        // get cache from Caffein
        return ticketDetailLocalCache.getIfPresent(ticketId);
    }

    private String genEventItemKey(Long ticketId) {
        return "PRO_TICKET:ITEM:" + ticketId;
    }

    private String genEventItemKeyLock(Long ticketId) {
        return "PRO_LOCK_KEY_ITEM" + ticketId;
    }
}
