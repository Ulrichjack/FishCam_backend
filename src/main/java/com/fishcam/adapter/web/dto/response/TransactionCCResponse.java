package com.fishcam.adapter.web.dto.response;

import com.fishcam.domain.comptecourant.TypeTransactionCC;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCCResponse {

    private Long id;
    private TypeTransactionCC type;
    private BigDecimal montant;
    private BigDecimal soldePrecedent;
    private BigDecimal soldeApres;
    private String description;
    private UserResponse effectuePar;
    private LocalDateTime transactionDate;
    private String notes;
}