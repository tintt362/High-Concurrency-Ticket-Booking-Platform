package com.trongtin.asyncprocessingsys.warnup;


import com.trongtin.asyncprocessingsys.service.order.cache.StockOrderCacheService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WarmupDataBeforeEvent {

    @Autowired
    private StockOrderCacheService stockOrderCacheService;
//    @Scheduled(cron = "*/10 * * * * ?")
//    public void loadDataTicketItemEveryTenSecond() {
//        log.info("Load ticket item... warmup..| {}", System.currentTimeMillis());
//    }

    @PostConstruct
    public void loadDataTicketItemOnce() {
        // get list events
        // for
        log.info("Load ticket item Once... warmup..| {}", System.currentTimeMillis());
        stockOrderCacheService.addStockAvailableToCache(4L);
    }
}