package com.trongtin.asyncprocessingsys.service.booking.impl;

import com.trongtin.asyncprocessingsys.dto.response.BookingDTO;
import com.trongtin.asyncprocessingsys.mapper.BookingMapper;
import com.trongtin.asyncprocessingsys.model.command.CreateBookingCommand;
import com.trongtin.asyncprocessingsys.model.entity.Booking;
import com.trongtin.asyncprocessingsys.repository.booking.BookingRepository;
import com.trongtin.asyncprocessingsys.service.booking.BookingDomainService;
import com.trongtin.asyncprocessingsys.service.booking.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingDomainService bookingDomainService;

    @Override
    public BookingDTO createBooking(CreateBookingCommand command){
        Booking booking = bookingDomainService.createBooking(command.getTicketId(), command.getQuantity());
        log.info("App Service: booking created with code: {}", booking.getBookingCode());
        return BookingMapper.toDTO(booking);
    }
}
