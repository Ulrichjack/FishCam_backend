package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecapitulatifLigneResponse {

    private LocalDate jour;
    private BigDecimal achat;
    private BigDecimal prevu;
    private BigDecimal realise;
    private BigDecimal depenses;
    private BigDecimal benefice;



}
