package com.xxxx.ddd.infrastructure.persistence.mapper;

import com.xxxx.ddd.domain.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketJPAMapper extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStatus(Integer status);
}
