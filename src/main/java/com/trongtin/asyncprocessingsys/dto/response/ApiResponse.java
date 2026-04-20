package com.trongtin.asyncprocessingsys.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// dto/response/ApiResponse.java — wrapper chuẩn cho mọi response
@Builder
@Getter
@Setter
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}