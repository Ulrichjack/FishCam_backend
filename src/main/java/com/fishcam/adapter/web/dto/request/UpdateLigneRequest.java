package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateLigneRequest {
    private Long produitId;

    @Min(1)
    private Integer quantiteCartons;

    @Positive
    private BigDecimal poidsKg;

    @Positive
    private BigDecimal prixUnitaireCarton;

    @Positive
    private BigDecimal prixVenteKilo;

}
