package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateEpargneRequest {

    @NotNull
    private Long clientId;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal initialAmount;
}
