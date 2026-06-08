package com.trongtin.asyncprocessingsys.model.command;

import lombok.Data;

@Data
public class CreateBookingCommand {
    private Long ticketId;
    private int quantity;
}