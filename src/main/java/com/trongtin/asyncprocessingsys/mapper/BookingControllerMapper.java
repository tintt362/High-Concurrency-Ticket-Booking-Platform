package com.trongtin.asyncprocessingsys.mapper;


import com.trongtin.asyncprocessingsys.dto.request.CreateBookingRequest;
import com.trongtin.asyncprocessingsys.model.command.CreateBookingCommand;

public class BookingControllerMapper {

    public static CreateBookingCommand toCommand(CreateBookingRequest req) {
        CreateBookingCommand cmd = new CreateBookingCommand();
        cmd.setTicketId(req.getTicketId());
        cmd.setQuantity(req.getQuantity());
        return cmd;
    }
}