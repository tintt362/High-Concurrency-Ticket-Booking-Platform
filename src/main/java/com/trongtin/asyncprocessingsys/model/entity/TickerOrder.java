package com.trongtin.asyncprocessingsys.model.entity;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TickerOrder {
    private Integer id;
    private String orderNumber;
    private Integer userId;

    // -- BỔ SUNG 2 TRƯỜNG NÀY ĐỂ KHỚP VỚI DATABASE --
    private Integer ticketId;
    private Integer quantity;
    // ----------------------------------------------
    private BigDecimal totalAmount;
    private String terminalId;
    private LocalDateTime orderDate;
    private String orderNotes;
    /**
     * Trạng thái đơn hàng:
     * 0: PENDING (Đang chờ - Mới tạo)
     * 1: SUCCESS (Thanh toán thành công)
     * 2: CANCELLED (Người dùng chủ động hủy)
     * 3: EXPIRED (Hết hạn do không thanh toán kịp)
     * 4: REFUNDED (Đã hoàn tiền)
     */
    private Integer orderStatus;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

}
