package com.trongtin.asyncprocessingsys.repository.order;

import com.trongtin.asyncprocessingsys.model.entity.TickerOrder;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderDeductionRepository {

    void insertOrder(String yearMonth, TickerOrder tickerOrder);
    List<Object[]> findAll(String yearMonth);
    Object[] findByOrderNumber(String yearMonth, String orderNumber);
    List<Object[]> findByDateRange(String yearMonth, LocalDateTime startDate, LocalDateTime endDate);

    // update status
    boolean updateOrderStatus(String yearMonth, String orderNumber, Integer status);

    List<Object[]> findPage(String yearMonth, long lastId, int limit);
}
