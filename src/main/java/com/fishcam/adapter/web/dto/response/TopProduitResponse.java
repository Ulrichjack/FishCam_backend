package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProduitResponse {
    private String nomProduit;
    private Long totalCartons;
    private BigDecimal totalDepense; // How much money we spent on this product
}