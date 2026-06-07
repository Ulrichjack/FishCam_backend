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
public class FactureResponse {

    private Long id;
    private LocalDate dateAchat;
    private Long poissonnerieId;
    private String poissonnerieNom;
    private LocalDateTime createdAt;
    private Long fournisseurId;
    private String fournisseurNom;
    private Long enregistreParId;
    private String enregistreParNom;
    private Boolean cloture = false;
    private BigDecimal totalAchat;

}
