package com.trongtin.asyncprocessingsys.service.ticket.impl;


import com.trongtin.asyncprocessingsys.model.entity.TicketDetail;
import com.trongtin.asyncprocessingsys.repository.ticket.TicketDetailRepository;
import com.trongtin.asyncprocessingsys.service.ticket.TicketDetailDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TicketDetailDomainServiceImpl implements TicketDetailDomainService {
    // Call repository in domain
//    private static final Logger log = LoggerFactory.getLogger(TicketDetailDomainServiceImpl.class);
    @Autowired
    private TicketDetailRepository ticketDetailRepository;

    @Override
    public TicketDetail getTicketDetailById(Long ticketId) {
//        log.info("Implement Domain : {}", ticketId);
        return ticketDetailRepository.findById(ticketId).orElse(null);
    }
}
