package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetteInitialeRequest {

    @NotNull(message = "Le compte courant est obligatoire")
    private Long compteCourantId;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal montant;

    @PastOrPresent(message = "La date d'origine ne peut pas être dans le futur")
    private LocalDate dateDetteOrigine;

    @Size(max = 500, message = "La note ne peut pas dépasser 500 caractères")
    private String notes;
}
