package com.trongtin.asyncprocessingsys.service.order;

public interface StockTransactionService {

    boolean decreaseStock1(Long ticketId, int quantity);
}
