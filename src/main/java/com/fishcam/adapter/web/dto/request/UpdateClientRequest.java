package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateClientRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String lastName;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Le numéro de téléphone n'est pas valide")
    private String phone;

    private String address;
    private String quartier;
    private Long poissonnerieId;
    private String cni;

    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateOfBirth;
}
