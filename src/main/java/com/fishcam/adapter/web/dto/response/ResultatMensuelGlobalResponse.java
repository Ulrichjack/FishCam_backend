package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultatMensuelGlobalResponse {
    private Integer mois;
    private Integer annee;
    private List<ResultatMensuelBoutiqueResponse> boutiques;
    private BigDecimal totalAchats;
    private BigDecimal totalEncaissements;
    private BigDecimal totalDepensesJournalieres;
    private BigDecimal totalChargesBoutiques;
    private BigDecimal chargesGenerales;
    private BigDecimal resultatProvisoire;
    private BigDecimal variationStock;
    private BigDecimal variationCreances;
    private BigDecimal resultatCorrigeStock;
    private BigDecimal resultatValide;
    private String statut;
    private List<String> informationsManquantes;
    private List<String> alertes;
}
