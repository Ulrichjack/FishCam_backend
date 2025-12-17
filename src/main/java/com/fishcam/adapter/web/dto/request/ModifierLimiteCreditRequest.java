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
public class ModifierLimiteCreditRequest {

    @NotNull(message = "La limite de crédit est obligatoire")
    @DecimalMin(value = "5000", message = "La limite minimum est de 5000 FCFA")
    private BigDecimal nouvelleLimit;
}