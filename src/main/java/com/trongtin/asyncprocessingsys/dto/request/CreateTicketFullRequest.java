package com.trongtin.asyncprocessingsys.dto.request;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class CreateTicketFullRequest {
    @Valid
    private CreateTicketRequest ticket;

    @Valid
    private CreateTicketDetailRequest detail;
}
