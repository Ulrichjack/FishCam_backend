package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LivreurResponse {

    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private Long fournisseurId;
    private String fournisseurNom;
    private Boolean actif;
    private LocalDateTime createdAt;


}
