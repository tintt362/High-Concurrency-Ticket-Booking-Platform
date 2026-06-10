package com.trongtin.asyncprocessingsys.dto.response;
import lombok.Data;

@Data
public class BookingDTO {
    private Long id;
    private Long ticketId;
    private int quantity;
    private String bookingCode;
    private int status;
}