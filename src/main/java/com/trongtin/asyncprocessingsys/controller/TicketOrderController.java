package com.trongtin.asyncprocessingsys.controller;


import com.trongtin.asyncprocessingsys.dto.request.CreateBookingRequest;
import com.trongtin.asyncprocessingsys.dto.response.PagedOrdersDTO;
import com.trongtin.asyncprocessingsys.dto.response.PlaceOrderResponse;
import com.trongtin.asyncprocessingsys.dto.response.TicketOrderDTO;
import com.trongtin.asyncprocessingsys.model.enums.ResultUtil;
import com.trongtin.asyncprocessingsys.model.vo.ResultMessage;
import com.trongtin.asyncprocessingsys.service.order.TicketOrderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@Slf4j
public class TicketOrderController {

    @Autowired
    private TicketOrderService ticketOrderAppService;

    /**
     Level 1,2
     */
    @GetMapping("/{ticketId}/{quantity}/order")
    public boolean orderTicketByLevel(
            @PathVariable("ticketId") Long ticketId,
            @PathVariable("quantity") int quantity
    ) {
        log.info("Controller:->orderTicketByLevel | {}, {}", ticketId, quantity);
        return ticketOrderAppService.decreaseStock1(ticketId, quantity);
    }

    /**
     Level 3
     */

    @GetMapping("/{ticketId}/{quantity}/cas")
    public boolean orderTicketByLevel3(
            @PathVariable("ticketId") Long ticketId,
            @PathVariable("quantity") int quantity
    ) {
        log.info("Controller:->orderTicketByLevel3 | {}, {}", ticketId, quantity);
        return ticketOrderAppService.decreaseStockCAS(ticketId, quantity);
    }

    @PostMapping("/cas")
    public ResultMessage<PlaceOrderResponse> placeOrderCAS(@Valid @RequestBody CreateBookingRequest request) {
        log.info("Controller:->placeOrderCAS | ticketId={}, quantity={}", request.getTicketId(), request.getQuantity());
        try {
            PlaceOrderResponse response = ticketOrderAppService.placeOrderCAS(request.getTicketId(), request.getQuantity());
            return ResultUtil.data(response);
        } catch (Exception e) {
            log.error("placeOrderCAS: unhandled error ticketId={}", request.getTicketId(), e);
            return ResultUtil.data(PlaceOrderResponse.failed("SERVER_ERROR", "Lỗi hệ thống, vui lòng thử lại"));
        }
    }




    // V1 — load toàn bộ đơn hàng
    @GetMapping("/{userId}/list")
    public ResultMessage<List<TicketOrderDTO>> getListOrderByUser(
            @PathVariable("userId") Long userId,
            @RequestParam("ntable") String ntable
    ) {
        log.info("Controller:->getListOrderByUser [V1] | userId={} ntable={}", userId, ntable);
        return ResultUtil.data(ticketOrderAppService.findAll(ntable));
    }

    // V2 — cursor-based pagination
    @GetMapping("/{userId}/list/page")
    public ResultMessage<PagedOrdersDTO> getListOrderByUserPaged(
            @PathVariable("userId") Long userId,
            @RequestParam("ntable") String ntable,
            @RequestParam(value = "cursor", defaultValue = "0") long cursor,
            @RequestParam(value = "limit",  defaultValue = "50") int limit
    ) {
        log.info("Controller:->getListOrderByUserPaged [V2] | userId={} ntable={} cursor={} limit={}", userId, ntable, cursor, limit);
        int safeLimit = Math.min(limit, 100);
        return ResultUtil.data(ticketOrderAppService.findPage(ntable, cursor, safeLimit));
    }

    // get orderItem
    @GetMapping("/{userId}/{orderNumber}")
    public ResultMessage<TicketOrderDTO> getOrderByUser(
            @PathVariable("userId") Long userId,
            @PathVariable("orderNumber") String orderNumber
    ) {
        log.info("Controller:->getOrderByUser | {}, {}", userId, orderNumber);
        return ResultUtil.data(ticketOrderAppService.findByOrderNumber("2025xx",orderNumber));
    }

    @PutMapping("/{userId}/{orderNumber}/cancel")
    public ResultMessage<Boolean> cancelOrder(
            @PathVariable("userId") Long userId,
            @PathVariable("orderNumber") String orderNumber
    ) {
        log.info("Controller:->cancelOrder | userId: {}, orderNumber: {}", userId, orderNumber);
        boolean result = ticketOrderAppService.cancelOrder(userId, orderNumber);
        return ResultUtil.data(result);
    }
}