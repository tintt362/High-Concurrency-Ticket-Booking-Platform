package com.trongtin.asyncprocessingsys.service.audit;

import com.trongtin.asyncprocessingsys.model.audit.OrderAuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchPage;

public interface OrderAuditLogService {

    void index(OrderAuditLog auditLog);

    SearchPage<OrderAuditLog> search(String keyword, Pageable pageable);

    SearchPage<OrderAuditLog> searchByTicketId(Long ticketId, Pageable pageable);
}