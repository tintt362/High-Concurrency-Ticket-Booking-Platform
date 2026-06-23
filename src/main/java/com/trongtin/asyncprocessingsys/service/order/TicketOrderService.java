package com.trongtin.asyncprocessingsys.service.order;

import com.trongtin.asyncprocessingsys.dto.response.PagedOrdersDTO;
import com.trongtin.asyncprocessingsys.dto.response.PlaceOrderResponse;
import com.trongtin.asyncprocessingsys.dto.response.TicketOrderDTO;
import com.trongtin.asyncprocessingsys.model.entity.TickerOrder;

import java.util.List;

public interface TicketOrderService {

    boolean decreaseStock1(Long tickerId, int quantity);
    boolean decreaseStockCAS(Long tickerId, int quantity);
    PlaceOrderResponse placeOrderCAS(Long ticketId, int quantity);

   // boolean decreaseStockQueue(Long userId, Long tickerId, int quantity);

    int getStockAvailable(Long ticketId);

    // order..
    List<TicketOrderDTO> findAll(String yearMonth);
    boolean insertOrder(String yearMonth, TickerOrder tickerOrder);
    TicketOrderDTO findByOrderNumber(String yearMonth, String orderNumber);

    /**
     * Hủy đơn hàng và hoàn lại tồn kho trong Database + Redis
     *
     * @param userId ID của người dùng thực hiện hủy
     * @param orderNumber Mã đơn hàng (VD: OKX-SGN-1-171204...)
     * @return true nếu hủy thành công, ngược lại false
     */
    boolean cancelOrder(Long userId, String orderNumber);

    PagedOrdersDTO findPage(String yearMonth, long lastId, int limit);
}
