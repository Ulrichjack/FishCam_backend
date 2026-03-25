package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenererBilanRequest {

    @NotNull
    private Long poissonnerieId;

    @NotNull
    @Min(1) @Max(12)
    private Integer mois;

    @NotNull
    private Integer annee;

}
