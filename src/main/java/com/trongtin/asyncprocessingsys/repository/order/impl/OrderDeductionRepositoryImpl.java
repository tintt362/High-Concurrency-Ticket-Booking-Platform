package com.trongtin.asyncprocessingsys.repository.order.impl;

import com.trongtin.asyncprocessingsys.model.entity.TickerOrder;
import com.trongtin.asyncprocessingsys.repository.order.OrderDeductionRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderDeductionRepositoryImpl implements OrderDeductionRepository {

    @Autowired
    private EntityManager entityManager;
    private static String tablePrefix = "ticket_order_";

    private String getTableName(String monthOrder) {
        return tablePrefix + monthOrder;
    }

    @Override
    @Transactional
    public void insertOrder(String yearMonth, TickerOrder order) {
        // AUTO: Create Table...
        ensureTableExists(yearMonth);
        //
        String tableName = getTableName(yearMonth); // 202604

        String sql = "INSERT INTO " + tableName + " (order_number, user_id, ticket_id, quantity, order_status, total_amount, terminal_id, order_date, order_notes, updated_at, created_at) " +
                "VALUES (:orderNumber, :userId, :ticketId, :quantity, :orderStatus, :totalAmount, :terminalId, :orderDate, :orderNotes, :updatedAt, :createdAt)";
        LocalDateTime now = LocalDateTime.now();
        order.setOrderDate(now);
        order.setUpdatedAt(now);
        order.setCreatedAt(now);
        entityManager.createNativeQuery(sql)
                .setParameter("orderNumber", order.getOrderNumber())
                .setParameter("userId", order.getUserId())
                // Thêm các tham số mới vào đây
                .setParameter("ticketId", order.getTicketId())
                .setParameter("quantity", order.getQuantity())
                .setParameter("orderStatus", order.getOrderStatus())

                .setParameter("totalAmount", order.getTotalAmount())
                .setParameter("terminalId", order.getTerminalId())
                .setParameter("orderDate", order.getOrderDate())
                .setParameter("orderNotes", order.getOrderNotes())
                .setParameter("updatedAt", order.getUpdatedAt())
                .setParameter("createdAt", order.getCreatedAt())
                .executeUpdate();
    }

    @Override
    public List<Object[]> findAll(String yearMonth) {
        String tableName = getTableName(yearMonth);
        String sql = "SELECT * FROM " + tableName + " ORDER BY created_at DESC";
        return entityManager.createNativeQuery(sql).getResultList();
    }

    @Override
    public Object[] findByOrderNumber(String yearMonth, String orderNumber) {
        String tableName = getTableName(yearMonth);
        String sql = "SELECT * FROM " + tableName + " WHERE order_number = :orderNumber";
        List<Object[]> resultList = entityManager.createNativeQuery(sql)
                .setParameter("orderNumber", orderNumber)
                .getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    @Override
    public List<Object[]> findByDateRange(String yearMonth, LocalDateTime startDate, LocalDateTime endDate) {
        String tableName = getTableName(yearMonth);
        String sql = "SELECT * FROM " + tableName + " WHERE order_date BETWEEN :startDate AND :endDate";
        return entityManager.createNativeQuery(sql)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    // TEMP DDL -> 28
    // 1. Khai báo Template DDL
    private static final String CREATE_TABLE_TEMPLATE =
            "CREATE TABLE IF NOT EXISTS `%s` (" +
                    "  id INT(8) NOT NULL AUTO_INCREMENT," +
                    "  user_id INT(8) NOT NULL," +
                    "  ticket_id INT(8) NOT NULL," +
                    "  quantity INT NOT NULL DEFAULT 1," +
                    "  order_status TINYINT NOT NULL DEFAULT 0," +
                    "  order_number VARCHAR(50) NOT NULL," +
                    "  total_amount DECIMAL(10,3) NOT NULL," +
                    "  terminal_id VARCHAR(20) NOT NULL," +
                    "  order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "  order_notes VARCHAR(100) NULL DEFAULT 'None'," +
                    "  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "  PRIMARY KEY (id) USING BTREE," +
                    "  UNIQUE KEY uk_order_number (order_number)," +
                    "  KEY idx_user_id (user_id)," +
                    "  KEY idx_ticket_id (ticket_id)," +
                    "  KEY idx_order_date (order_date)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
    // 2. Bộ nhớ đệm (Cache)
    private static final Map<String, Boolean> tableCreatedCache = new java.util.concurrent.ConcurrentHashMap<>();
//    // use hashtable
    ////    private static final Map<String, Boolean> tableCreatedCache = new Hashtable<>();
//    // use hashmap
//    private static final Map<String, Boolean> tableCreatedCache = new HashMap<>();
    @Override
    @Transactional
    public boolean updateOrderStatus(String yearMonth, String orderNumber, Integer status) {
        String tableName = getTableName(yearMonth);
        String sql = "UPDATE " + tableName + " SET order_status = :status, updated_at = :updatedAt WHERE order_number = :orderNumber";
        int result = entityManager.createNativeQuery(sql)
                .setParameter("status", status)
                .setParameter("updatedAt", LocalDateTime.now())
                .setParameter("orderNumber", orderNumber)
                .executeUpdate();
        return result > 0;
    }

    @Override
    public List<Object[]> findPage(String yearMonth, long lastId, int limit) {
        String tableName = getTableName(yearMonth);
        if (lastId <= 0) {
            String sql = "SELECT * FROM " + tableName + " ORDER BY id DESC LIMIT :limit";
            return entityManager.createNativeQuery(sql)
                    .setParameter("limit", limit)
                    .getResultList();
        }
        String sql = "SELECT * FROM " + tableName + " WHERE id < :lastId ORDER BY id DESC LIMIT :limit";
        return entityManager.createNativeQuery(sql)
                .setParameter("lastId", lastId)
                .setParameter("limit", limit)
                .getResultList();
    }

    /**
     * Tự động tạo bảng nếu chưa tồn tại (Just-In-Time)
     */
    private void ensureTableExists(String yearMonth) { // 202604
        String tableName = getTableName(yearMonth);

        // Nếu trong RAM đã xác nhận bảng này tồn tại rồi thì return ngay (Tối ưu CCU)
        if (tableCreatedCache.containsKey(tableName)) {
            return;
        }
        synchronized (tableCreatedCache) {
            // Double-check locking: Kiểm tra lại một lần nữa trong khối sync
            if (tableCreatedCache.containsKey(tableName)) {
                return;
            }
            log.info("Checking and creating table if not exists: {}", tableName);
            try {
                // Thực thi tạo bảng trực tiếp vào MySQL
                String sql = String.format(CREATE_TABLE_TEMPLATE, tableName);
                entityManager.createNativeQuery(sql).executeUpdate();

                // Đánh dấu đã tạo bảng thành công vào Cache RAM
                tableCreatedCache.put(tableName, true);
            } catch (Exception e) {
                log.error("Failed to ensure table exists for: {}", tableName, e);
                // Không throw exception ở đây để tránh làm gián đoạn luồng chính
                // Nếu bảng thực sự không có, Database sẽ báo lỗi ở câu lệnh SQL phía sau.
            }
        }
    }
}


