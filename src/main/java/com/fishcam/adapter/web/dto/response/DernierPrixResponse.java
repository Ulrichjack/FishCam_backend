package com.fishcam.adapter.web.dto.response;

import com.fishcam.domain.achat.TypeFluctuation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DernierPrixResponse {
    private BigDecimal poidsParCarton;
    private BigDecimal montantCarton;
    private BigDecimal prixVenteKilo;
    private BigDecimal ancienMontantCarton;
    private BigDecimal difference;
    private TypeFluctuation fluctuation;

}
