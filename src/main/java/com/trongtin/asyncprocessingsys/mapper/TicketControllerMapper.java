package com.xxxx.ddd.controller.mapper;

import com.xxxx.ddd.application.model.command.CreateTicketCommand;
import com.xxxx.ddd.application.model.command.CreateTicketDetailCommand;
import com.xxxx.ddd.controller.dto.CreateTicketDetailRequest;
import com.xxxx.ddd.controller.dto.CreateTicketRequest;

import java.math.BigDecimal;

public class TicketControllerMapper {

    public static CreateTicketCommand toCommand(CreateTicketRequest req) {
        CreateTicketCommand cmd = new CreateTicketCommand();
        cmd.setTitle(req.getName());
        cmd.setDescription(req.getDescription());
        cmd.setValidFrom(req.getStartTime());
        cmd.setValidTo(req.getEndTime());
        return cmd;
    }

    public static CreateTicketDetailCommand toDetailCommand(CreateTicketDetailRequest req) {
        CreateTicketDetailCommand cmd = new CreateTicketDetailCommand();
        cmd.setName(req.getName());
        cmd.setDescription(req.getDescription());
        cmd.setStockInitial(req.getStockInitial());
        cmd.setStockAvailable(req.getStockAvailable());
        cmd.setPriceOriginal(toBigDecimal(req.getPriceOriginal()));
        cmd.setPriceFlash(toBigDecimal(req.getPriceFlash()));
        cmd.setStockPrepared(req.getStockPrepared() != null ? req.getStockPrepared() : false);
        return cmd;
    }

    private static BigDecimal toBigDecimal(Long value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}
