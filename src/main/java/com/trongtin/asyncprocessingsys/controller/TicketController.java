package com.xxxx.ddd.controller.http;

import com.xxxx.ddd.application.model.TicketDTO;
import com.xxxx.ddd.application.model.command.CreateTicketCommand;
import com.xxxx.ddd.application.model.command.CreateTicketDetailCommand;
import com.xxxx.ddd.application.service.ticket.TicketAppService;
import com.xxxx.ddd.controller.dto.CreateTicketDetailRequest;
import com.xxxx.ddd.controller.dto.CreateTicketFullRequest;
import com.xxxx.ddd.controller.dto.CreateTicketRequest;
import com.xxxx.ddd.controller.dto.UpdateTicketRequest;
import com.xxxx.ddd.controller.mapper.TicketControllerMapper;
import com.xxxx.ddd.controller.model.enums.ResultCode;
import com.xxxx.ddd.controller.model.enums.ResultUtil;
import com.xxxx.ddd.controller.model.vo.ResultMessage;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ticket")
@Slf4j
public class TicketController {


    @Autowired
    private TicketAppService ticketAppService;

    /**
     * Lấy tất cả ticket đang active
     *
     * GET /ticket/active
     *
     * @return ResultMessage<List<TicketDTO>>
     */
    @GetMapping("/active")
    public ResultMessage<List<TicketDTO>> getAllActiveTickets() {
        log.info("Fetching all active tickets");
        try {
            List<TicketDTO> tickets = ticketAppService.getAllActiveTickets();
            return ResultUtil.data(tickets);
        } catch (Exception e) {
            log.error("Error fetching active tickets", e);
            return ResultUtil.error(500, "Failed to fetch active tickets");
        }
    }

    /**
     * Tạo ticket mới
     *
     * POST /ticket/create
     *
     * Request Body:
     {
     "ticket": {
     "name": "Concert ABC",
     "description": "Concert held in HCM City",
     "startTime": "2024-05-01 18:00:00",
     "endTime": "2024-05-01 22:00:00"
     },
     "detail": {
     "name": "VIP",
     "stockInitial": 100,
     "stockAvailable": 100,
     "priceOriginal": 500000
     }
     }
     *
     * @param request
     * @return ResultMessage<TicketDTO>
     */
    @PostMapping("/create")
    public ResultMessage<TicketDTO> createTicket(
            @Valid @RequestBody CreateTicketFullRequest request) {
        log.info("Creating ticket: {}", request.getTicket().getName());
        try {
            // ✅ map request -> command
            CreateTicketCommand ticketCmd =
                    TicketControllerMapper.toCommand(request.getTicket());

            CreateTicketDetailCommand detailCmd =
                    TicketControllerMapper.toDetailCommand(request.getDetail());

            // ✅ gọi service bằng command
            TicketDTO ticketDTO =
                    ticketAppService.createTicket(ticketCmd, detailCmd);

            return ResultUtil.data(ticketDTO);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            return ResultUtil.error(500, e.getMessage());
        } catch (Exception e) {
            log.error("Error creating ticket", e);
            return ResultUtil.error(500, "Failed to create ticket");
        }
    }

    /**
     * Lấy thông tin ticket
     *
     * GET /ticket/1
     *
     * @param ticketId
     * @return ResultMessage<TicketDTO>
     */
    @GetMapping("/{ticketId}")
    public ResultMessage<TicketDTO> getTicket(
            @PathVariable Long ticketId) {
        log.info("Fetching ticket: {}", ticketId);
        try {
            TicketDTO ticketDTO = ticketAppService.getTicketById(ticketId);
            return ResultUtil.data(ticketDTO);
        } catch (Exception e) {
            log.error("Error fetching ticket", e);
            return ResultUtil.error(500, e.getMessage());
        }
    }

    /**
     * Cập nhật ticket
     *
     * PUT /ticket/1
     *
     * @param ticketId
     * @param updateRequest
     * @return ResultMessage<TicketDTO>
     */
    @PutMapping("/{ticketId}")
    public ResultMessage<TicketDTO> updateTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketRequest updateRequest) {
        log.info("Updating ticket: {}", ticketId);
        try {
           // TicketDTO ticketDTO = ticketAppService.updateTicket(ticketId, updateRequest);
            return ResultUtil.data(null);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            return ResultUtil.error(500, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating ticket", e);
            return ResultUtil.error(500, e.getMessage());
        }
    }

    /**
     * Kích hoạt ticket
     *
     * PUT /ticket/1/active
     *
     * @param ticketId
     * @return ResultMessage<TicketDTO>
     */
    @PutMapping("/{ticketId}/active")
    public ResultMessage<TicketDTO> activeTicket(
            @PathVariable Long ticketId) {
        log.info("Activating ticket: {}", ticketId);
        try {
            TicketDTO ticketDTO = ticketAppService.activeTicket(ticketId);
            return ResultUtil.data(ticketDTO);
        } catch (Exception e) {
            log.error("Error activating ticket", e);
            return ResultUtil.error(500, e.getMessage());
        }
    }

    /**
     * Vô hiệu hóa ticket
     *
     * PUT /ticket/1/inactive
     *
     * @param ticketId
     * @return ResultMessage<TicketDTO>
     */
    @PutMapping("/{ticketId}/inactive")
    public ResultMessage<TicketDTO> inactiveTicket(
            @PathVariable Long ticketId) {
        log.info("Inactivating ticket: {}", ticketId);
        try {
            TicketDTO ticketDTO = ticketAppService.inactiveTicket(ticketId);
            return ResultUtil.data(ticketDTO);
        } catch (Exception e) {
            log.error("Error inactivating ticket", e);
            return ResultUtil.error(500, e.getMessage());
        }
    }

    /**
     * Xoá ticket (Soft Delete)
     *
     * DELETE /ticket/1
     *
     * @param ticketId
     * @return ResultMessage<String>
     */
    @DeleteMapping("/{ticketId}")
    public ResultMessage<String> deleteTicket(
            @PathVariable Long ticketId) {
        log.info("Deleting ticket: {}", ticketId);
        try {
            ticketAppService.deleteTicket(ticketId);
            return ResultUtil.data("Ticket deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting ticket", e);
            return ResultUtil.error(500, e.getMessage());
        }
    }
}
