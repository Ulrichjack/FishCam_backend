package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultatAnnuelResponse {
    private Integer annee;
    private List<ResultatMensuelGlobalResponse> mois;
    private BigDecimal totalAchats;
    private BigDecimal totalEncaissements;
    private BigDecimal totalDepenses;
    private BigDecimal totalCharges;
    private BigDecimal resultatProvisoire;
    private BigDecimal resultatCorrigeStock;
    private BigDecimal resultatValide;
    private String statut;
}
