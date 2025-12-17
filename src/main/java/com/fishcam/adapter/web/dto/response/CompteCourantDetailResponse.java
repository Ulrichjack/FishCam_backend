package com.fishcam.adapter.web.dto.response;

import com.fishcam.domain.comptecourant.StatutCompteCourant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompteCourantDetailResponse {

    private Long id;
    private ClientResponse client;
    //private PoissonnerieResponse poissonnerie;
    private BigDecimal solde;
    private BigDecimal limiteCredit;
    private StatutCompteCourant statut;
    private LocalDateTime dateOuverture;
    private LocalDateTime updatedAt;

    private List<TransactionCCResponse> transactions;
    private Integer nombreTransactions;
    private BigDecimal totalEmprunts;
    private BigDecimal totalRemboursements;
}