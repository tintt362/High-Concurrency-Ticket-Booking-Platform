package com.trongtin.asyncprocessingsys.service.order.impl;

import com.trongtin.asyncprocessingsys.model.entity.TickerOrder;
import com.trongtin.asyncprocessingsys.repository.order.OrderDeductionRepository;
import com.trongtin.asyncprocessingsys.service.order.OrderDeductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderDeductionServiceImpl implements OrderDeductionService {

    @Autowired
    private OrderDeductionRepository orderDeductionRepository;
    @Override
    public void insertOrder(String yearMonth, TickerOrder tickerOrder) {
        orderDeductionRepository.insertOrder(yearMonth, tickerOrder);
    }

    @Override
    public List<Object[]> findAll(String yearMonth) {
        return orderDeductionRepository.findAll(yearMonth);//List.of();
    }

    @Override
    public Object[] findByOrderNumber(String yearMonth, String orderNumber) {
        return orderDeductionRepository.findByOrderNumber(yearMonth, orderNumber);//new Object[0];
    }

    @Override
    public List<Object[]> findByDateRange(String yearMonth, LocalDateTime startDate, LocalDateTime endDate) {
        return List.of();
    }

    @Override
    public boolean updateOrderStatus(String yearMonth, String orderNumber, Integer status) {
        return orderDeductionRepository.updateOrderStatus(yearMonth, orderNumber, status);
    }

    @Override
    public List<Object[]> findPage(String yearMonth, long lastId, int limit) {
        return orderDeductionRepository.findPage(yearMonth, lastId, limit);
    }
}
