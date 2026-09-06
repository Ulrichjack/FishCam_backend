package com.fishcam.adapter.web.dto.request;

import com.fishcam.domain.gestion.ModeEvaluation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SaveReleveSituationRequest {

    @NotNull
    private Long poissonnerieId;

    @NotNull
    private LocalDate dateReleve;

    @NotNull
    @PositiveOrZero
    private BigDecimal valeurStock;

    @PositiveOrZero
    private BigDecimal totalCreancesClients;

    @NotNull
    private ModeEvaluation modeEvaluation;

    @Size(max = 500)
    private String note;
}
