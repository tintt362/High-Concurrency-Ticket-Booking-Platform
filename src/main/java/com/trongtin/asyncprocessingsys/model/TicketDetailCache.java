package com.trongtin.asyncprocessingsys.model;


import com.trongtin.asyncprocessingsys.model.entity.TicketDetail;
import lombok.Data;

import java.util.Optional;

@Data
public class TicketDetailCache {

    private Long version;
    private TicketDetail ticketDetail;

    public TicketDetailCache withClone(Optional<TicketDetail> ticketDetail) {
        this.ticketDetail = ticketDetail;
        return this;
    }

    public TicketDetailCache withVersion(Long version) {
        this.version = version;
        return this;
    }

}