package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepotEpargneRequest {

    @NotNull(message = "L'ID de l'épargne est obligatoire")
    private Long epargneId;

    @NotNull(message = "Le montant du dépôt est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal amount;
}
