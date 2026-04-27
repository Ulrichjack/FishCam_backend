package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesCompteCourantResponse {
    private Long nombreComptesEnDette;
    private BigDecimal totalDettes;
}
