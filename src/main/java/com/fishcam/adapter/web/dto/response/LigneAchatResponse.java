package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneAchatResponse {

    private Long id;
    private Long produitId;
    private String produitNom;
    private Integer quantiteCartons;
    private BigDecimal poidsKg;
    private BigDecimal prixUnitaireCarton;
    private BigDecimal montantCarton;
    private BigDecimal prixVenteKilo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal prixAchatKilo;
    private BigDecimal prixVenteTotal;
    private BigDecimal margeKilo;
    private BigDecimal margeTotal;
}
