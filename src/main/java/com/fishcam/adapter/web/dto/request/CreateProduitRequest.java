package com.fishcam.adapter.web.dto.request;

import com.fishcam.domain.produit.Unite;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProduitRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String nom;

    @NotNull
    private Unite unite;

    @NotNull
    @Positive
    private BigDecimal poidsParCarton;

}
