package com.trongtin.asyncprocessingsys.repository.ticket.impl;


import com.trongtin.asyncprocessingsys.mapper.TicketOrderJPAMapper;
import com.trongtin.asyncprocessingsys.repository.ticket.TicketOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TicketOrderRepositoryImpl implements TicketOrderRepository {

    @Autowired
    private TicketOrderJPAMapper ticketOrderJPAMapper;

    @Override
    public boolean decreaseStockLevel1(Long tickerId, int quantity) {
        log.info("Run test:decreaseStockLevel1 with: | {}, {} ", tickerId, quantity);
        return ticketOrderJPAMapper.decreaseStockLevel1(tickerId, quantity) > 0;
    }




    @Override
    public boolean decreaseStockLevel3CAS(Long tickerId, int oldStockAvailable, int quantity) {
        log.info("Run test:decreaseStockLevel3CAS with: | {}, {}, {} ", tickerId, oldStockAvailable, quantity);
        return ticketOrderJPAMapper.decreaseStockLevel3CAS(tickerId, oldStockAvailable, quantity) > 0;
    }

    @Override
    public int getStockAvailable(Long ticketId) {
        return ticketOrderJPAMapper.getStockAvailable(ticketId);
    }

    @Override
    public boolean increaseStock(Long tickerId, int quantity) {
        log.info("Rollback stock: increaseStock for ticketId: {} | quantity: {}", tickerId, quantity);
        // Gọi method increaseStock mà bạn đã thêm vào TicketOrderJPAMapper (Bước 2.1 ở tin nhắn trước)
        return ticketOrderJPAMapper.increaseStock(tickerId, quantity) > 0;
    }
}