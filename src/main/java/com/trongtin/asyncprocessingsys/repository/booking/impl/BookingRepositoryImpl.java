package com.trongtin.asyncprocessingsys.repository.booking.impl;

import com.trongtin.asyncprocessingsys.mapper.BookingJPAMapper;
import com.trongtin.asyncprocessingsys.model.entity.Booking;
import com.trongtin.asyncprocessingsys.repository.booking.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class BookingRepositoryImpl implements BookingRepository {

    @Autowired
    private BookingJPAMapper bookingJPAMapper;

    @Override
    public Booking save(Booking booking) {
        log.info("Saving booking for ticket: {}", booking.getTicketId());
        return bookingJPAMapper.save(booking);
    }

    @Override
    public Optional<Booking> findByBookingCode(String bookingCode) {
        return bookingJPAMapper.findByBookingCode(bookingCode);
    }
}