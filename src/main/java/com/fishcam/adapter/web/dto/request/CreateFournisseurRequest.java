package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateFournisseurRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String nom;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 50, message = "Le ville  ne peut pas dépasser 50 caractères")
    private String ville;

    @Size(max = 10, message = "Le numero ne peut pas dépasser 10 caractères")
    private String telephone;

}
