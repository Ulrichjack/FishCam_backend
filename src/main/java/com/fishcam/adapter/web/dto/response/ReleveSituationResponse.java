package com.fishcam.adapter.web.dto.response;

import com.fishcam.domain.gestion.ModeEvaluation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReleveSituationResponse {
    private Long id;
    private Long poissonnerieId;
    private String poissonnerieNom;
    private LocalDate dateReleve;
    private BigDecimal valeurStock;
    private BigDecimal totalCreancesClients;
    private ModeEvaluation modeEvaluation;
    private String note;
    private String saisiParNom;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
