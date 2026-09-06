package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateClotureJournaliereRequest {

    @NotNull
    @PositiveOrZero
    private BigDecimal argentCaisse;

    @NotNull
    @PositiveOrZero
    private BigDecimal fondDeCaisse;

    @PositiveOrZero
    private BigDecimal transport;

    @PositiveOrZero
    private BigDecimal ration;

    @PositiveOrZero
    private BigDecimal autresFrais;

    @Size(max = 500)
    private String descriptionAutres;

    @NotBlank
    @Size(max = 500)
    private String motifCorrection;
}
