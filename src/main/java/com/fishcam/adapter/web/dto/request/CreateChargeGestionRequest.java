package com.fishcam.adapter.web.dto.request;

import com.fishcam.domain.gestion.CategorieCharge;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateChargeGestionRequest {

    /** Null pour une charge générale commune aux trois boutiques. */
    private Long poissonnerieId;

    @NotNull
    private CategorieCharge categorie;

    @NotBlank
    @Size(max = 120)
    private String libelle;

    @NotNull
    @PositiveOrZero
    private BigDecimal montant;

    @NotNull
    private Boolean recurrente;

    @NotNull
    private LocalDate dateDebut;

    private LocalDate dateFin;
}
