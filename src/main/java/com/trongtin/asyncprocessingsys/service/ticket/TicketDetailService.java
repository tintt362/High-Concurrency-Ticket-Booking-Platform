package com.trongtin.asyncprocessingsys.service.ticket;

import com.trongtin.asyncprocessingsys.dto.response.TicketDetailDTO;

public interface TicketDetailService {

    // TicketDetailService dùng để cho user get request ticket, còn TicketService cho User CRUD ticket
    TicketDetailDTO getTicketDetailById(Long ticketId, Long version);
    // order ticket
    boolean orderTicketByUser(Long ticketId);
}
