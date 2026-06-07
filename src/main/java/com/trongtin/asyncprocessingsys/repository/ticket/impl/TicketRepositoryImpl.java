package com.trongtin.asyncprocessingsys.repository.ticket.impl;


import com.trongtin.asyncprocessingsys.mapper.TicketJPAMapper;
import com.trongtin.asyncprocessingsys.model.entity.Ticket;
import com.trongtin.asyncprocessingsys.model.entity.TicketDetail;
import com.trongtin.asyncprocessingsys.repository.ticket.TicketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TicketRepositoryImpl implements TicketRepository {

    @Autowired
    private TicketJPAMapper ticketJPAMapper;

    @Override
    public List<Ticket> findAllActive() {
        log.info("Finding all active tickets");
        return ticketJPAMapper.findByStatus(1);
    }

    @Override
    public Ticket save(Ticket ticket) {
        log.info("Saving ticket: {}", ticket.getName());
        return ticketJPAMapper.save(ticket);
    }

    @Override
    public Ticket update(Ticket ticket) {
        log.info("Updating ticket: {}", ticket.getId());
        return ticketJPAMapper.save(ticket);
    }

    @Override
    public Optional<Ticket> findById(Long id) {
        log.info("Finding ticket: {}", id);
        return ticketJPAMapper.findById(id);
    }

    @Override
    public void deleteTicket(Long ticketId) {
        log.info("Soft deleting ticket: {}", ticketId);
        Optional<Ticket> optionalTicket = ticketJPAMapper.findById(ticketId);
    }

    @Override
    public List<Ticket> getAllActiveTickets() {
        log.info("Finding all active tickets");
        return ticketJPAMapper.findByStatus(1);
    }

}