package com.trongtin.asyncprocessingsys.mapper;

import com.trongtin.asyncprocessingsys.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketJPAMapper extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStatus(Integer status);
}
