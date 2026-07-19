package com.trongtin.asyncprocessingsys.model.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderAuditLog {

    private String id;
    private String orderNumber;
    private Long ticketId;
    private Integer userId;
    private Integer quantity;
    private Integer orderStatus;
    private String eventType;           // PLACE_ORDER, CANCEL_ORDER, STOCK_ADJUST
    private Integer oldStock;
    private Integer newStock;
    private Long createdAt;
    private String notes;

    public static OrderAuditLog createPlaceOrderLog(Long ticketId, Integer userId, Integer quantity,
                                                    Integer oldStock, Integer newStock, String orderNumber) {
        OrderAuditLog log = new OrderAuditLog();
        log.setEventType("PLACE_ORDER");
        log.setTicketId(ticketId);
        log.setUserId(userId);
        log.setQuantity(quantity);
        log.setOldStock(oldStock);
        log.setNewStock(newStock);
        log.setOrderNumber(orderNumber);
        log.setCreatedAt(Instant.now().toEpochMilli());
        log.setNotes("Đặt vé thành công");
        return log;
    }

    public static OrderAuditLog createCancelOrderLog(Long ticketId, Integer userId, Integer quantity,
                                                     Integer oldStock, Integer newStock, String orderNumber) {
        OrderAuditLog log = new OrderAuditLog();
        log.setEventType("CANCEL_ORDER");
        log.setTicketId(ticketId);
        log.setUserId(userId);
        log.setQuantity(quantity);
        log.setOldStock(oldStock);
        log.setNewStock(newStock);
        log.setOrderNumber(orderNumber);
        log.setCreatedAt(Instant.now().toEpochMilli());
        log.setNotes("Hủy đơn hàng thành công");
        return log;
    }
}