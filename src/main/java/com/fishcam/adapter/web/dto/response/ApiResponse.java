package com.fishcam.adapter.web.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private int code;
    private LocalDateTime timestamp;
    private Map<String, String> fieldErrors; // pour validations
}