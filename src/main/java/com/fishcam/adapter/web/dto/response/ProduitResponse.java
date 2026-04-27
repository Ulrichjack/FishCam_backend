package com.fishcam.adapter.web.dto.response;

import com.fishcam.domain.produit.Unite;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProduitResponse {
    private Long id;
    private String nom;
    private Unite unite;
    private BigDecimal poidsParCarton;
    private Boolean actif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
