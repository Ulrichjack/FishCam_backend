package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecapitulatifResponse {

    private List<RecapitulatifLigneResponse> lignes;
    private BigDecimal totalAchat;
    private BigDecimal totalPrevu;
    private BigDecimal totalRealise;
    private BigDecimal totalDepenses;
    private BigDecimal totalBenefice;

}


