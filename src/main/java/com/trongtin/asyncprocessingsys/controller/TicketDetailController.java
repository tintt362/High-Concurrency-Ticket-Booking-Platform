package com.xxxx.ddd.controller.http;

import com.xxxx.ddd.application.model.TicketDetailDTO;
import com.xxxx.ddd.application.service.ticket.TicketDetailAppService;
import com.xxxx.ddd.controller.model.enums.ResultUtil;
import com.xxxx.ddd.controller.model.vo.ResultMessage;
import com.xxxx.ddd.domain.model.entity.TicketDetail;
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
    private TicketDetailAppService ticketDetailAppService;

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
        return ResultUtil.data(ticketDetailAppService.getTicketDetailById(detailId, version));
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
        return ticketDetailAppService.orderTicketByUser(detailId);
    }
}
