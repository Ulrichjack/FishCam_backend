package com.fishcam.adapter.web.dto.response;

import com.fishcam.domain.gestion.CategorieCharge;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargeGestionResponse {
    private Long id;
    private Long poissonnerieId;
    private String poissonnerieNom;
    private CategorieCharge categorie;
    private String libelle;
    private BigDecimal montant;
    private Boolean recurrente;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean active;
    private Boolean supprimee;
}
