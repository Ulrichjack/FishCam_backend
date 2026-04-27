package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BilanMensuelResponse {

    private Long id;
    private Integer mois;
    private Integer annee;
    private Long poissonnerieId;
    private Long genereParId;
    private  String poissonnerieNom;
    private String genereParNom;
    private BigDecimal totalAchatMois;
    private BigDecimal totalVenteRealisee;
    private BigDecimal totalVentePrevisibleMois;
    private BigDecimal totalDepensesMois;
    private BigDecimal beneficeNetMois;
    private Integer    nombreJoursTravailles;
    private LocalDate meilleurJourBenefice;
    private BigDecimal beneficeMeilleurJour;
    private BigDecimal montantDettesMois;
    private LocalDateTime createdAt;

}
