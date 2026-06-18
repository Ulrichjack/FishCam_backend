package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateLivreurRequest {
    @Size(max = 50)
    private String nom;

    @Size(max = 50)
    private String prenom;

    @Size(max = 20)
    private String telephone;

    private Long fournisseurId;
}