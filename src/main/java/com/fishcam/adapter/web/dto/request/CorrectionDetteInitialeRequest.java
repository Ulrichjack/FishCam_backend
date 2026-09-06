package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CorrectionDetteInitialeRequest {

    /** Zéro annule la dette sans en créer une nouvelle. */
    @NotNull(message = "Le nouveau montant est obligatoire")
    @PositiveOrZero(message = "Le nouveau montant ne peut pas être négatif")
    private BigDecimal nouveauMontant;

    @PastOrPresent(message = "La date d'origine ne peut pas être dans le futur")
    private LocalDate nouvelleDateDetteOrigine;

    @NotBlank(message = "Le motif de la correction est obligatoire")
    @Size(max = 500, message = "Le motif ne peut pas dépasser 500 caractères")
    private String motif;
}
