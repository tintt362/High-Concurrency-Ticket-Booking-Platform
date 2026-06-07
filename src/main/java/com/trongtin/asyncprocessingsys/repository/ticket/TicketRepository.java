package com.trongtin.asyncprocessingsys.repository.ticket;

import com.trongtin.asyncprocessingsys.model.entity.Ticket;
import com.trongtin.asyncprocessingsys.model.entity.TicketDetail;

import java.util.List;
import java.util.Optional;

public interface TicketRepository {

    List<Ticket> findAllActive();

    /**
     * Lưu ticket mới
     * @param ticket
     * @return Ticket được lưu (kèm ID)
     */
    Ticket save(Ticket ticket);

    /**
     * Cập nhật ticket
     * @param ticket
     * @return Ticket được cập nhật
     */
    Ticket update(Ticket ticket);

    /**
     * Tìm ticket theo ID
     * @param id
     * @return Optional chứa Ticket
     */
    Optional<Ticket> findById(Long id);

    /**
     * Xoá ticket (Soft Delete)
     * - Set status = 2 (DELETED)
     * - Delete tất cả TicketDetail liên quan (soft delete)
     *
     * @param ticketId
     * @throws RuntimeException nếu ticket không tồn tại
     */
    void deleteTicket(Long ticketId);

    /**
     * Lấy tất cả ticket đang active
     * @return List<Ticket>
     */
    List<Ticket> getAllActiveTickets();

}
