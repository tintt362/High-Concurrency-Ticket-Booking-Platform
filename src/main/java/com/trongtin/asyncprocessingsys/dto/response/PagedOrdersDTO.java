package com.trongtin.asyncprocessingsys.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedOrdersDTO {
    private List<TicketOrderDTO> items;
    private Long nextCursor;
    private boolean hasMore;
}
