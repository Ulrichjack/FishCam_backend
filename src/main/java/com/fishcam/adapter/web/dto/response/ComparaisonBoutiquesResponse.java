package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComparaisonBoutiquesResponse {

    private List<BilanMensuelResponse> bilans;
    private BigDecimal totalVentesGlobal;
    private BigDecimal totalAchatsGlobal;
    private  BigDecimal totalDepensesGlobal;
    private BigDecimal beneficeNetGlobal;
    private  Integer mois;
    private Integer annee;
}
