package com.fishcam.adapter.web.dto.response;

import com.fishcam.domain.epargne.TypeTransactionEpargne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEpargneResponse {
    private Long id;
    private TypeTransactionEpargne type;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private UserResponse effectuePar;
    private String notes;


}
