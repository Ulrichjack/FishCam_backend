package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEvaluationRequest {

    @NotNull(message = "L'achat du jour est obligatoire")
    private  Long achatJournalierId;

    @NotNull(message = "Le livreur est obligatoire")
    private Long livreurId;

    @NotNull(message = "La note de qualité est obligatoire")
    @Min(1)
    @Max(5)
    private Integer qualiteProduit;

    @NotNull(message = "La note de respect du poids est obligatoire")
    @Min(1)
    @Max(5)
    private Integer respectPoids;

    private String commentaire;

    @NotNull(message = "Veuillez indiquer s'il y a un problème")
    private Boolean problemeSignale = false;


}
