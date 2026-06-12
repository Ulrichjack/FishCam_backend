package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionGlobalResponse {
    private String id; // String car on va fusionner "CC_1", "EP_2" pour éviter les doublons d'ID
    private LocalDateTime dateHeure;
    private String clientNom;
    private String clientTelephone;
    private String type; // "DETTE", "REMBOURSEMENT", "DEPOT", "RETRAIT"
    private BigDecimal montant;
}