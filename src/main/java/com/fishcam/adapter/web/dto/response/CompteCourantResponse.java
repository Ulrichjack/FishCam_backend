package com.fishcam.adapter.web.dto.response;

import com.fishcam.domain.comptecourant.StatutCompteCourant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompteCourantResponse {

    private Long id;
    private ClientResponse client;
    private BigDecimal solde;
    private BigDecimal limiteCreditMax;
    private StatutCompteCourant statut;
    private LocalDateTime dateOuverture;
    private LocalDateTime updatedAt;
}