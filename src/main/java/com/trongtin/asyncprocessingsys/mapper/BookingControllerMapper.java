package com.xxxx.ddd.controller.mapper;

import com.xxxx.ddd.application.model.command.CreateBookingCommand;
import com.xxxx.ddd.controller.dto.CreateBookingRequest;

public class BookingControllerMapper {

    public static CreateBookingCommand toCommand(CreateBookingRequest req) {
        CreateBookingCommand cmd = new CreateBookingCommand();
        cmd.setTicketId(req.getTicketId());
        cmd.setQuantity(req.getQuantity());
        return cmd;
    }
}