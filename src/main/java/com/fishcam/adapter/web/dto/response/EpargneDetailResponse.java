package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EpargneDetailResponse {

    private Long id;
    private ClientResponse client;
    //private PoissonnerieResponse poissonnerie;
    private BigDecimal currentBalance;
    private LocalDateTime createdAt;

    private List<TransactionEpargneResponse> transactions;
    private UserResponse createdBy;
    private Integer nombreTransactions;
    private BigDecimal totalDepots;
    private BigDecimal totalRetraits;
}
