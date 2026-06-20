package com.trongtin.asyncprocessingsys.service.booking.impl;


import com.trongtin.asyncprocessingsys.model.entity.Booking;
import com.trongtin.asyncprocessingsys.repository.booking.BookingRepository;
import com.trongtin.asyncprocessingsys.service.booking.BookingDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class BookingDomainServiceImpl implements BookingDomainService {

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public Booking createBooking(Long ticketId, int quantity) {
        if (ticketId == null || ticketId <= 0) {
            throw new IllegalArgumentException("Invalid ticket ID");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (quantity > 10) {
            throw new IllegalArgumentException("Cannot book more than 10 tickets at once");
        }

        Booking booking = new Booking();
        booking.setTicketId(ticketId);
        booking.setQuantity(quantity);
        booking.setBookingCode(generateBookingCode());
        booking.setStatus(1); // CONFIRMED
        booking.setCreatedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);
        log.info("Created booking: {} for ticket: {}", saved.getBookingCode(), ticketId);
        return saved;
    }

    private String generateBookingCode() {
        return "BK" + System.currentTimeMillis()
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}