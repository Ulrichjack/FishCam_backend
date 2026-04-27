package com.fishcam.adapter.web.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeResponse {

    private Long id;
    private String prenom;
    private String nom;
    private String poste;
    private BigDecimal salaire;
    private String telephone;
    private Long poissonnerieId;
    private Long userId;
    private String poissonnerieNom;
    private Boolean actif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
