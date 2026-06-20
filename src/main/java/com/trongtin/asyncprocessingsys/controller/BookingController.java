package com.trongtin.asyncprocessingsys.controller;


import com.trongtin.asyncprocessingsys.dto.request.CreateBookingRequest;
import com.trongtin.asyncprocessingsys.dto.response.BookingDTO;
import com.trongtin.asyncprocessingsys.mapper.BookingControllerMapper;
import com.trongtin.asyncprocessingsys.model.command.CreateBookingCommand;
import com.trongtin.asyncprocessingsys.model.enums.ResultUtil;
import com.trongtin.asyncprocessingsys.model.vo.ResultMessage;
import com.trongtin.asyncprocessingsys.service.booking.BookingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@Slf4j
public class BookingController {

    @Autowired
    private BookingService bookingAppService;

    @PostMapping
    public ResultMessage<BookingDTO> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        log.info("Creating booking for ticket: {}, quantity: {}", request.getTicketId(), request.getQuantity());
        try {
            CreateBookingCommand command = BookingControllerMapper.toCommand(request);
            BookingDTO dto = bookingAppService.createBooking(command);
            return ResultUtil.data(dto);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            return ResultUtil.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("Error creating booking", e);
            return ResultUtil.error(500, "Failed to create booking");
        }
    }
}