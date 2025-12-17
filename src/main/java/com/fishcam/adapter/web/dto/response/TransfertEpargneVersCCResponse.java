package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransfertEpargneVersCCResponse {

    private Boolean success;
    private String message;
    private BigDecimal montantTransfere;
    private BigDecimal nouveauSoldeEpargne;
    private BigDecimal nouveauSoldeCompteCourant;
    private Boolean compteSolde;
}