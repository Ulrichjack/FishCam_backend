package com.fishcam.adapter.web.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateEmployeRequest {

    @NotBlank(message = "Le prenom est obligatoire")
    @Size(max = 50, message = "Le prenom ne peut pas dépasser 50 caractères")
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String nom;

    @NotBlank(message = "Le poste est obligatoire")
    @Size(max = 50, message = "Le poste ne peut pas dépasser 50 caractères")
    private String poste;

    @NotNull
    @Positive
    private BigDecimal salaire;

    @Size(max = 20)
    private String telephone;

    @NotNull
    private Long poissonnerieId;

    private Long userId;

}
