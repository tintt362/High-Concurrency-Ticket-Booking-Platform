package com.trongtin.asyncprocessingsys.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ticket_item")
public class TicketDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private int stockInitial;        // Số lượng vé ban đầu
    private int stockAvailable;      // Số lượng vé còn lại
    private boolean isStockPrepared; // Đã chuẩn bị kho?
    private BigDecimal priceOriginal;      // Giá gốc
    private BigDecimal priceFlash;         // Giá flash sale
    private LocalDateTime saleStartTime;      // Thời gian bắt đầu bán
    private LocalDateTime saleEndTime;        // Thời gian kết thúc bán
    private int status;              // 0=INACTIVE, 1=ACTIVE, 2=DELETED
    private Long activityId;         // Có thể là FK tới Ticket
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

//    @Override
//    public String toString() {
//        return "TicketDetail{id=" + id + ", name='" + name + "', otherField=1}";
//    }
}
