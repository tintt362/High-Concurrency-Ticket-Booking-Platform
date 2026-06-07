package com.trongtin.asyncprocessingsys.repository.ticket;


import com.trongtin.asyncprocessingsys.model.entity.TicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketDetailRepository {
    /**
     * Lưu TicketDetail mới
     * @param ticketDetail
     * @return TicketDetail được lưu
     */
    TicketDetail save(TicketDetail ticketDetail);

    /**
     * Cập nhật TicketDetail
     * @param ticketDetail
     * @return TicketDetail được cập nhật
     */
    TicketDetail update(TicketDetail ticketDetail);

    /**
     * Tìm TicketDetail theo ID
     * @param id
     * @return Optional chứa TicketDetail
     */
    Optional<TicketDetail> findById(Long id);

    /**
     * Tìm tất cả TicketDetail theo activityId (ticketId)
     * @param activityId
     * @return List TicketDetail
     */
    List<TicketDetail> findByActivityId(Long activityId);

    /**
     * Cập nhật status của tất cả TicketDetail theo activityId
     * @param activityId
     * @param status
     */
    void updateStatusByActivityId(Long activityId, int status);

    /**
     * Xoá TicketDetail (hard delete)
     * @param id
     */
    void deleteById(Long id);

    /**
     * Xoá tất cả TicketDetail theo activityId
     * @param activityId
     */
    void deleteByActivityId(Long activityId);
}
