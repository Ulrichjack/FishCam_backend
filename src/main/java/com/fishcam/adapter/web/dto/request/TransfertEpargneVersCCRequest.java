package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransfertEpargneVersCCRequest {

    @NotNull(message = "Le compte courant est obligatoire")
    private Long compteCourantId;

    @NotNull(message = "L'épargne est obligatoire")
    private Long epargneId;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal montant;
}