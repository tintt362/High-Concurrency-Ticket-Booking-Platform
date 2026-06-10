package com.trongtin.asyncprocessingsys.repository.order;

import com.trongtin.asyncprocessingsys.dto.response.TicketOrderDTO;
import com.trongtin.asyncprocessingsys.model.entity.TickerOrder;

import java.util.List;

public interface TicketOrderRepository {
    boolean decreaseStockLevel1(Long tickerId, int quantity);
    boolean decreaseStockLevel3CAS(Long tickerId, int oldStockAvailable, int quantity);

    int getStockAvailable(Long ticketId);

    /**
     * Thực hiện câu lệnh SQL hoàn kho vào Database
     */
    boolean increaseStock(Long tickerId, int quantity);
}
