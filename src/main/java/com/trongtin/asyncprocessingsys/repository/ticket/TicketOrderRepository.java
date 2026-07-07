package com.trongtin.asyncprocessingsys.repository.ticket;

public interface TicketOrderRepository {
    boolean decreaseStock1(Long tickerId, int quantity);
    boolean decreaseStockCAS(Long tickerId, int oldStockAvailable, int quantity);

    int getStockAvailable(Long ticketId);

    /**
     * Thực hiện câu lệnh SQL hoàn kho vào Database
     */
    boolean increaseStock(Long tickerId, int quantity);
}
