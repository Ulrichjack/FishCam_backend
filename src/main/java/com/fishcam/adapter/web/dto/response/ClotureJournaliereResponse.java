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
public class ClotureJournaliereResponse {

    private Long id;
    private LocalDate date;
    private Long poissonnerieId;
    private Long clotureParId;
    private String poissonnerieNom;
    private String clotureParNom;
    private BigDecimal ecartVente;
    private String descriptionAutres;
    private BigDecimal totalAchat;
    private BigDecimal totalVentePrevisible;
    private BigDecimal montantDettesJour;
    private BigDecimal montantRembourseJour;
    private Integer nombreDettesJour;
    private BigDecimal argentCaisse;
    private BigDecimal fondDeCaisse;
    private BigDecimal transport ;
    private BigDecimal ration;
    private BigDecimal autresFrais;
    private BigDecimal venteRealisee;
    private BigDecimal totalDepenses;
    private BigDecimal beneficeNet;
    private LocalDateTime createdAt;


}
