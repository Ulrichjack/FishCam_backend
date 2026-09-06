package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultatMensuelBoutiqueResponse {
    private Long poissonnerieId;
    private String poissonnerieNom;
    private Integer mois;
    private Integer annee;
    private Integer nombreJoursSaisis;
    private LocalDate dateDerniereCloture;
    private BigDecimal totalAchats;
    private BigDecimal totalEncaissements;
    private BigDecimal depensesJournalieres;
    private BigDecimal chargesMensuelles;
    private BigDecimal resultatProvisoire;
    private LocalDate dateSituationInitiale;
    private BigDecimal stockInitial;
    private BigDecimal creancesInitiales;
    private LocalDate dateSituationFinale;
    private BigDecimal stockFinal;
    private BigDecimal creancesFinales;
    private BigDecimal variationStock;
    private BigDecimal variationCreances;
    /** Résultat corrigé du seul stock : disponible même quand les créances sont inconnues. */
    private BigDecimal resultatCorrigeStock;
    private BigDecimal resultatValide;
    private String statut;
    private List<String> informationsManquantes;
    /** Incohérences détectées : double comptage d'une dépense, etc. */
    private List<String> alertes;
}
