package com.trongtin.asyncprocessingsys.mapper;

import com.trongtin.asyncprocessingsys.model.entity.TicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketDetailJPAMapper extends JpaRepository<TicketDetail, Long> {
    /**
     * Tìm tất cả TicketDetail theo activityId
     */
    List<TicketDetail> findByActivityId(Long activityId);

    /**
     * Cập nhật status theo activityId
     */
    @Modifying
    @Query("UPDATE TicketDetail td SET td.status = :status WHERE td.activityId = :activityId")
    void updateStatusByActivityId(@Param("activityId") Long activityId, @Param("status") int status);

    /**
     * Xoá tất cả TicketDetail theo activityId
     */
    @Modifying
    void deleteByActivityId(Long activityId);
}