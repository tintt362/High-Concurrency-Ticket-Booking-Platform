package com.trongtin.asyncprocessingsys.dto.response;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PlaceOrderResponse {

    private boolean success;
    private String code;
    private String message;
    private String placeOrderTaskId;
    private Long orderId;

    public static PlaceOrderResponse success(String placeOrderTaskId) {
        return new PlaceOrderResponse()
                .setSuccess(true)
                .setPlaceOrderTaskId(placeOrderTaskId);
    }

    public static PlaceOrderResponse success(Long orderId) {
        return new PlaceOrderResponse()
                .setSuccess(true)
                .setOrderId(orderId);
    }

    public static PlaceOrderResponse failed(String code, String message) {
        return new PlaceOrderResponse()
                .setSuccess(false)
                .setCode(code)
                .setMessage(message);
    }

    public static PlaceOrderResponse success() {
        return new PlaceOrderResponse()
                .setSuccess(true);
    }

    public static PlaceOrderResponse isTrue() {
        return new PlaceOrderResponse()
                .setSuccess(true);
    }
}
