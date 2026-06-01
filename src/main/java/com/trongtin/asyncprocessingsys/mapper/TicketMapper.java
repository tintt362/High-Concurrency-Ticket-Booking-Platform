package com.trongtin.asyncprocessingsys.mapper;

import com.xxxx.ddd.application.model.TicketDTO;
import com.xxxx.ddd.application.model.command.CreateTicketCommand;
import com.xxxx.ddd.domain.model.entity.Ticket;

public class TicketMapper {

    /**
     * Command -> Entity
     */
    public static Ticket toEntity(CreateTicketCommand cmd) {
        Ticket ticket = new Ticket();

        ticket.setName(cmd.getTitle());
        ticket.setDescription(cmd.getDescription());
        ticket.setStartTime(cmd.getValidFrom());
        ticket.setEndTime(cmd.getValidTo());

        return ticket;
    }

    /**
     * Entity -> DTO
     */
    public static TicketDTO toDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();

        dto.setId(ticket.getId());
        dto.setName(ticket.getName());
        dto.setDescription(ticket.getDescription());
        dto.setStartTime(ticket.getStartTime());
        dto.setEndTime(ticket.getEndTime());
        dto.setStatus(ticket.getStatus());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());

        return dto;
    }
}