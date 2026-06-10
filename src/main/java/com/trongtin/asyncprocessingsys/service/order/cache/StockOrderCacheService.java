package com.trongtin.asyncprocessingsys.service.order.cache;


import com.trongtin.asyncprocessingsys.cache.redis.RedisInfrasService;
import com.trongtin.asyncprocessingsys.model.TicketDetailCache;
import com.trongtin.asyncprocessingsys.service.ticket.cache_ticket.TicketDetailCacheServiceRefactor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;

@Service
@Slf4j
public class StockOrderCacheService {

    @Autowired
    private TicketDetailCacheServiceRefactor ticketDetailCacheServiceRefactor;

    @Autowired
    private RedisInfrasService redisInfrasService;

    public boolean addStockAvailableToCache(Long ticketId) {
        // That's remember check validation(*)
        if(ticketId == null) {
            return false;
        }
        // get stock_available from mysql
        TicketDetailCache ticketDetailCache = ticketDetailCacheServiceRefactor.getTicketDetail(ticketId, null);
        if(ticketDetailCache == null || ticketDetailCache.getTicketDetail() == null) {
            return false;
        }
        String keyStockItemCache = getKeyStockItemCache(ticketId);
        log.info("get->getKeyStockItemCache() | {}, {}, {}", ticketId, keyStockItemCache,
                ticketDetailCache.getTicketDetail().getStockAvailable());
        // stockAvailable = ticketDetailCache.getTicketDetail().getStockAvailable();
        redisInfrasService.setInt(keyStockItemCache, ticketDetailCache.getTicketDetail().getStockAvailable());
        return true;
    }

    // decreaseStockCache
    public int decreaseStockCache(Long ticketId, Integer quantity) {
        // 1. Get Stock Available
        String keyStockNormal = getKeyStockItemCache(ticketId);
        int stockAvailable = redisInfrasService.getInt(keyStockNormal); // 100
        log.info("stockAvailable Normal: {}, {}, {} ", keyStockNormal, stockAvailable, String.valueOf(stockAvailable - quantity));
        // 2. Decrease Stock

        if(stockAvailable >= quantity){ // 100 > 1 = 99
            redisInfrasService.setInt(keyStockNormal, stockAvailable - quantity); // 99
            log.info("stockAvailable racing...: {}", stockAvailable - quantity);
            return 1;
        }
        return 0; // stockAvailable = 0 , quantity = 1
    }

    public int decreaseStockCacheByLUA(Long ticketId, Integer quantity) {
        String keyStockLUA = getKeyStockItemCache(ticketId);
        // return -1 when key doesn't exist (cache not warmed), 0 when out of stock, 1 when success
        String luaScript =
                "local stock = redis.call('GET', KEYS[1]); " +
                        "if stock == false then return -1 end; " +
                        "stock = tonumber(stock); " +
                        "if (stock >= tonumber(ARGV[1])) then " +
                        "   redis.call('SET', KEYS[1], stock - tonumber(ARGV[1])); " +
                        "   return 1; " +
                        "end; " +
                        "return 0; ";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Long result = redisInfrasService.getRedisTemplate().execute(redisScript, Collections.singletonList(keyStockLUA), quantity);
        return result != null ? result.intValue() : -1;
    }


    private String getKeyStockItemCache(Long ticketId) {
        return "TICKET:"+ ticketId + ":STOCK";
    }

    private String getKeyStockCacheLUA(Long ticketId){
        return "LUA:TICKET:" + ticketId + ":STOCK";
    }

    // Trả về giá hiệu lực: priceFlash nếu có, ngược lại priceOriginal. -1 nếu không tìm thấy ticket.
    public long getEffectivePrice(Long ticketId) {
        TicketDetailCache cache = ticketDetailCacheServiceRefactor.getTicketDetail(ticketId, null);
        if (cache == null || cache.getTicketDetail() == null) return -1L;
        BigDecimal flash = cache.getTicketDetail().getPriceFlash();
        BigDecimal original = cache.getTicketDetail().getPriceOriginal();
        if (flash != null && flash.compareTo(BigDecimal.ZERO) > 0) return flash.longValue();
        return original != null ? original.longValue() : -1L;
    }

    // tăng stock trong cache nếu user cancel đơn hàng trong khi diễn ra flash sale
    public boolean increaseStockCache(Long ticketId, Integer quantity) {
        String keyStock = getKeyStockItemCache(ticketId);
        String luaScript =
                "local stock = redis.call('GET', KEYS[1]); " +
                        "if (stock) then " +
                        "   redis.call('SET', KEYS[1], tonumber(stock) + tonumber(ARGV[1])); " +
                        "   return 1; " +
                        "end; " +
                        "return 0;";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Long result = redisInfrasService.getRedisTemplate().execute(
                redisScript,
                Collections.singletonList(keyStock),
                quantity
        );
        return result != null && result == 1;
    }
}
