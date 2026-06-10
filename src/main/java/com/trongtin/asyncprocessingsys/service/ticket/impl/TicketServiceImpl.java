package com.trongtin.asyncprocessingsys.service.ticket.impl;

import com.trongtin.asyncprocessingsys.dto.response.TicketDTO;
import com.trongtin.asyncprocessingsys.model.command.CreateTicketCommand;
import com.trongtin.asyncprocessingsys.model.command.CreateTicketDetailCommand;
import com.trongtin.asyncprocessingsys.model.command.UpdateTicketCommand;
import com.trongtin.asyncprocessingsys.service.ticket.TicketService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {
    @Override
    public TicketDTO createTicket(CreateTicketCommand createRequest, CreateTicketDetailCommand createDetailRequest) {
        return null;
    }

    @Override
    public TicketDTO getTicketById(Long ticketId) {
        return null;
    }

    @Override
    public TicketDTO updateTicket(Long ticketId, UpdateTicketCommand updateRequest) {
        return null;
    }

    @Override
    public TicketDTO activeTicket(Long ticketId) {
        return null;
    }

    @Override
    public TicketDTO inactiveTicket(Long ticketId) {
        return null;
    }

    @Override
    public void deleteTicket(Long ticketId) {

    }

    @Override
    public List<TicketDTO> getAllActiveTickets() {
        return List.of();
    }
}
