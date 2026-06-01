package com.trongtin.asyncprocessingsys.repository.ticket;

import com.trongtin.asyncprocessingsys.model.entity.TicketDetail;

public interface TicketRepository {

    TicketDetail getTicketDetailById(Long ticketId);

}
