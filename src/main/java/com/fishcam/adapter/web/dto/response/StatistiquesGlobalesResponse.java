package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesGlobalesResponse {
    private Integer totalClientsGlobal;
    private BigDecimal totalEpargneGlobal;
    private BigDecimal totalDettesGlobal;

    // This will hold the individual stats for each of the 3 stores
    private List<StatistiquesPoissonnerieResponse> detailsParBoutique;
}