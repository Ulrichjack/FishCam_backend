package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateLigneRequest {

    @NotNull
    private Long produitId;

    @NotNull
    @Min(1)
    private Integer quantiteCartons;

    @NotNull
    @Positive
    private BigDecimal poidsKg;

    @NotNull
    @Positive
    private BigDecimal prixUnitaireCarton;

    @NotNull
    @Positive
    private BigDecimal prixVenteKilo;


}
