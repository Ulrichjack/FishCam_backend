package com.fishcam.adapter.web.dto.request;

import com.fishcam.domain.user.Role;
import com.fishcam.domain.user.UserScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "le nom est obligatoire")
    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    private String firstName;

    @NotBlank(message = "le prenom est obligatoire")
    @Size(max = 50, message = "Le nom de famille ne peut pas dépasser 50 caractères")
    private String lastName;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Le numéro de téléphone n'est pas valide")
    private String phone;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    @NotNull(message = "Le scope est obligatoire")
    private UserScope scope;

    private Long defaultPoissonnerieId;

}
