package com.trongtin.asyncprocessingsys.service.order.impl;

import com.trongtin.asyncprocessingsys.repository.ticket.TicketOrderRepository;
import com.trongtin.asyncprocessingsys.service.order.StockTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Slf4j
public class StockTransactionServiceImpl implements StockTransactionService {

    @Autowired
    private TicketOrderRepository ticketOrderRepository;

    @Override
    public boolean decreaseStock1(Long ticketId, int quantity) {
        long startTime = System.nanoTime();
        boolean result = ticketOrderRepository.decreaseStock1(ticketId, quantity);
        long endTime = System.nanoTime();

        long durationNano = endTime - startTime;
        double durationMillis = durationNano / 1_000_000.0; // Đổi sang mili giây
        log.info("StockTransactionServiceImpl -> decreaseStock1={} ", durationMillis + "ms");
        return result;
    }
}
