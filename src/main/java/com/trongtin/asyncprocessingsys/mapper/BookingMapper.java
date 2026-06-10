package com.trongtin.asyncprocessingsys.mapper;


import com.trongtin.asyncprocessingsys.dto.response.BookingDTO;
import com.trongtin.asyncprocessingsys.model.entity.Booking;

public class BookingMapper {

    public static BookingDTO toDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setTicketId(booking.getTicketId());
        dto.setQuantity(booking.getQuantity());
        dto.setBookingCode(booking.getBookingCode());
        dto.setStatus(booking.getStatus());
        return dto;
    }
}