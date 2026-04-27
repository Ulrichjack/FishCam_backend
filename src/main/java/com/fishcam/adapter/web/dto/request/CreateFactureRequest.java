package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateFactureRequest {

    @NotNull
    private Long poissonnerieId;

    @NotNull
    private Long fournisseurId;

    @NotNull
    private LocalDate dateAchat;
}
