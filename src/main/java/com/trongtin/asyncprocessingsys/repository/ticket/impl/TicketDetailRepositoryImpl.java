package com.xxxx.ddd.infrastructure.persistence.repository;

import com.xxxx.ddd.domain.model.entity.TicketDetail;
import com.xxxx.ddd.domain.respository.TicketDetailRepository;
import com.xxxx.ddd.infrastructure.persistence.mapper.TicketDetailJPAMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TicketDetailRepositoryImpl implements TicketDetailRepository {

    @Autowired
    private TicketDetailJPAMapper ticketDetailJPAMapper;

    @Override
    public TicketDetail save(TicketDetail ticketDetail) {
        log.info("Saving TicketDetail: {}", ticketDetail.getName());
        return ticketDetailJPAMapper.save(ticketDetail);
    }

    @Override
    public TicketDetail update(TicketDetail ticketDetail) {
        log.info("Updating TicketDetail: {}", ticketDetail.getId());
        return ticketDetailJPAMapper.save(ticketDetail);
    }

    @Override
    public Optional<TicketDetail> findById(Long id) {
        log.info("Finding TicketDetail: {}", id);
        return ticketDetailJPAMapper.findById(id);
    }

    @Override
    public List<TicketDetail> findByActivityId(Long activityId) {
        log.info("Finding TicketDetail by activityId: {}", activityId);
        return ticketDetailJPAMapper.findByActivityId(activityId);
    }

    @Override
    public void updateStatusByActivityId(Long activityId, int status) {
        log.info("Updating status for TicketDetail with activityId: {}, status: {}", activityId, status);
        ticketDetailJPAMapper.updateStatusByActivityId(activityId, status);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Deleting TicketDetail: {}", id);
        ticketDetailJPAMapper.deleteById(id);
    }

    @Override
    public void deleteByActivityId(Long activityId) {
        log.info("Deleting TicketDetail by activityId: {}", activityId);
        ticketDetailJPAMapper.deleteByActivityId(activityId);
    }
}
