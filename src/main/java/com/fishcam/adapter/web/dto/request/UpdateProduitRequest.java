package com.fishcam.adapter.web.dto.request;

import com.fishcam.domain.produit.Unite;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProduitRequest {

    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String nom;

    private Unite unite;

    @Positive
    private BigDecimal poidsParCarton;
}
