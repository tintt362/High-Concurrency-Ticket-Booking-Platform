package com.trongtin.asyncprocessingsys.service.audit.impl;

import com.trongtin.asyncprocessingsys.model.audit.OrderAuditLog;
import com.trongtin.asyncprocessingsys.service.audit.OrderAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAuditLogServiceImpl implements OrderAuditLogService {

    private final ElasticsearchOperations elasticsearchOperations;

    private static final String INDEX_NAME = "order_audit_log";

    @Async("auditLogExecutor")
    @Override
    public void index(OrderAuditLog auditLog) {
        try {
            if (auditLog.getId() == null) {
                auditLog.setId(java.util.UUID.randomUUID().toString());
            }
            if (auditLog.getCreatedAt() == null) {
                auditLog.setCreatedAt(System.currentTimeMillis());
            }

            elasticsearchOperations.save(auditLog, IndexCoordinates.of(INDEX_NAME));
            log.info("✅ Audit log indexed: {} | Event: {} | Order: {}",
                    auditLog.getTicketId(), auditLog.getEventType(), auditLog.getOrderNumber());
        } catch (Exception e) {
            log.error("❌ Failed to index audit log for order: {}", auditLog.getOrderNumber(), e);
        }
    }

    @Override
    public SearchPage<OrderAuditLog> search(String keyword, Pageable pageable) {
        Criteria criteria = new Criteria();

        if (StringUtils.hasText(keyword)) {
            criteria = criteria.or(new Criteria("orderNumber").contains(keyword))
                    .or(new Criteria("notes").contains(keyword))
                    .or(new Criteria("eventType").contains(keyword));
        }

        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(pageable);

        SearchHits<OrderAuditLog> searchHits = elasticsearchOperations.search(
                query, OrderAuditLog.class, IndexCoordinates.of(INDEX_NAME));

        // Cách đúng để convert SearchHits → SearchPage
        return SearchHitSupport.searchPageFor(searchHits, pageable);
    }

    @Override
    public SearchPage<OrderAuditLog> searchByTicketId(Long ticketId, Pageable pageable) {
        Criteria criteria = new Criteria("ticketId").is(ticketId);

        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(pageable);

        SearchHits<OrderAuditLog> searchHits = elasticsearchOperations.search(
                query, OrderAuditLog.class, IndexCoordinates.of(INDEX_NAME));

        return SearchHitSupport.searchPageFor(searchHits, pageable);
    }
}