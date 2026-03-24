package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ClotureJournaliereRequest {


    @NotNull
    private LocalDate date;

    @NotNull
    private Long poissonnerieId;

    @NotNull
    @Positive(message = "L'argent en caisse doit être positif")
    private BigDecimal argentCaisse;

    @NotNull
    @PositiveOrZero(message = "Le fond de caisse ne peut pas être négatif")
    private BigDecimal fondDeCaisse;

    @PositiveOrZero(message = "Le transport ne peut pas être négatif")
    private BigDecimal transport;

    @PositiveOrZero(message = "La ration ne peut pas être négatif")
    private BigDecimal ration;

    @PositiveOrZero(message = "Les autres frais ne peut pas être négatif")
    private BigDecimal autresFrais;

    private String descriptionAutres;



}
