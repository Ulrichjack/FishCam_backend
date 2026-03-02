package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    private Long userId;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit faire au moins 6 caractères")
    private String nouveauMotDePasse;
}
