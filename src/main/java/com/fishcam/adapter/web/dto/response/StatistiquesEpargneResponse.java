package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesEpargneResponse {
    private Long poissonnerieId;
    private String poissonnerieNom;
    private Integer nombreComptes;
    private BigDecimal totalEpargne;
    private BigDecimal moyenneParCompte;


}
