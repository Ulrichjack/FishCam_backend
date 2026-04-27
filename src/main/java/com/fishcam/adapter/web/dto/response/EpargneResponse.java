package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EpargneResponse {

    private Long id;
    private ClientResponse client;
    // private PoissonnerieResponse poissonnerie;
    private BigDecimal currentBalance;
    private LocalDateTime createdAt;
}
