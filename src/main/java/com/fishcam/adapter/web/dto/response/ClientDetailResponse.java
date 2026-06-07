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
public class ClientDetailResponse {

    private Long id;
    private Long compteCourantId;
    private Long epargneId;
    private String firstName;
    private String lastName;
    private String phone;
    private String cni;
    private String address;
    private String quartier;
    private LocalDate dateOfBirth;
    private PoissonnerieResponse poissonnerie;
    private Boolean active;
    private BigDecimal soldeCompteCourant;
    private BigDecimal soldeEpargne;
    private BigDecimal limiteCredit;
    private String statutCompteCourant;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}