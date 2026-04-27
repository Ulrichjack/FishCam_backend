package com.fishcam.adapter.web.dto.request;


import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateEmployeRequest {

    @Size(max = 50, message = "Le prenom ne peut pas dépasser 50 caractères")
    private String prenom;

    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String nom;

    @Size(max = 50, message = "Le poste ne peut pas dépasser 50 caractères")
    private String poste;

    @Positive
    private BigDecimal salaire;

    @Size(max = 20)
    private String telephone;

    private Long poissonnerieId;

    private Long userId;
}
