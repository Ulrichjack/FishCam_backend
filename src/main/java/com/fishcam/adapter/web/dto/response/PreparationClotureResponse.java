package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreparationClotureResponse {

    private BigDecimal fondDeCaisseDefaut;
    private BigDecimal montantDettesJour;
    private BigDecimal montantRembourseJour;
    private Integer nombreDettesJour;
    private BigDecimal totalAchat;
    private BigDecimal totalVentePrevisible;
    private Integer facturesNonCloturees;

}
