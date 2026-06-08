package com.trongtin.asyncprocessingsys.controller;

import com.trongtin.asyncprocessingsys.dto.response.TicketDetailDTO;
import com.trongtin.asyncprocessingsys.model.enums.ResultUtil;
import com.trongtin.asyncprocessingsys.model.vo.ResultMessage;
import com.trongtin.asyncprocessingsys.service.ticket.TicketDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticket")
@Slf4j
public class TicketDetailController {

    // CALL Service Application
    @Autowired
    private TicketDetailService ticketDetailService;

    @GetMapping("/ping/java")
    public ResponseEntity<Object> ping() throws InterruptedException {
        // Giả lập tác vụ mất thời gian
        Thread.sleep(1000);  // Giống như time.Sleep(1 * time.Second)

        // Trả về response với status OK
        return ResponseEntity.status(HttpStatus.OK)
                .body(new Response("OK"));
    }

    // Lớp Response để trả về JSON response
    public static class Response {
        private String status;

        public Response(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * Get ticket detail
     * @param ticketId
     * @param detailId
     * @return ResultUtil
     */
    @GetMapping("/{ticketId}/detail/{detailId}")
    public ResultMessage<TicketDetailDTO> getTicketDetail(
            @PathVariable("ticketId") Long ticketId,
            @PathVariable("detailId") Long detailId,
            @RequestParam(name = "version", required = false) Long version
    ) {
        return ResultUtil.data(ticketDetailService.getTicketDetailById(detailId, version));
    }

    /**
     * order by User
     * @param ticketId
     * @param detailId
     * @return ResultUtil
     */
    @GetMapping("/{ticketId}/detail/{detailId}/order")
    public boolean orderTicketByUser(
            @PathVariable("ticketId") Long ticketId,
            @PathVariable("detailId") Long detailId
    ) {
        return ticketDetailService.orderTicketByUser(detailId);
    }
}
