package com.trongtin.asyncprocessingsys.service.ticket.impl;


import com.trongtin.asyncprocessingsys.dto.response.TicketDetailDTO;
import com.trongtin.asyncprocessingsys.mapper.TicketDetailMapper;
import com.trongtin.asyncprocessingsys.model.TicketDetailCache;
import com.trongtin.asyncprocessingsys.service.ticket.TicketDetailService;
import com.trongtin.asyncprocessingsys.service.ticket.cache_ticket.TicketDetailCacheServiceRefactor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TicketDetailServiceImpl implements TicketDetailService {

    // CALL CACHE

    @Autowired
    private TicketDetailCacheServiceRefactor ticketDetailCacheServiceRefactor;

    @Override
    public TicketDetailDTO getTicketDetailById(Long ticketId, Long version) {
//        log.info("Implement Application : {}, {}: ", ticketId, version);
        TicketDetailCache ticketDetailCache = ticketDetailCacheServiceRefactor.getTicketDetail(ticketId, version);
        // mapper to DTO
        TicketDetailDTO ticketDetailDTO = TicketDetailMapper.toDTO(ticketDetailCache.getTicketDetail());
        ticketDetailDTO.setVersion(ticketDetailCache.getVersion());
        return ticketDetailDTO;
    }



    @Override
    public boolean orderTicketByUser(Long ticketId) {
        return ticketDetailCacheServiceRefactor.orderTicketByUser(ticketId);
    }


}
