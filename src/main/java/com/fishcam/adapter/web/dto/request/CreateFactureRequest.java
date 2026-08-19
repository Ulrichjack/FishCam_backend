package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class CreateFactureRequest {

    @NotNull
    private Long poissonnerieId;

    @NotNull
    private Long fournisseurId;

    @NotNull
    private LocalDate dateAchat;

    /**
     * Lignes créées dans la même transaction que la facture. La liste reste
     * optionnelle pour conserver la compatibilité avec les anciens clients.
     */
    @Valid
    private List<CreateLigneRequest> lignes = new ArrayList<>();
}
