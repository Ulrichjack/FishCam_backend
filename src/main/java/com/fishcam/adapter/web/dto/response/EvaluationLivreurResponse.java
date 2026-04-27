package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationLivreurResponse {

    private Long id;
    private LocalDate dateEvaluation;
    private Integer qualiteProduit;
    private Integer respectPoids;
    private String commentaire;
    private Boolean problemeSignale = false;
    private Long livreurId;
    private String livreurNom;
    private String livreurPrenom;
    private String evaluatorNom;

}
