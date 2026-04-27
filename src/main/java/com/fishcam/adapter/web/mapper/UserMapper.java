package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.request.CreateUserRequest;
import com.fishcam.adapter.web.dto.request.UpdateUserRequest;
import com.fishcam.adapter.web.dto.response.UserResponse;
import com.fishcam.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Convertit User en UserResponse
     * Le password ne sera JAMAIS inclus dans la réponse (pas de champ password dans UserResponse)
     */
    UserResponse toResponse(User entity);

    /**
     * Convertit CreateUserRequest en User
     * <p>
     * IMPORTANT :
     * - Le password est mappé automatiquement (même nom dans les 2 classes)
     * - defaultPoissonnerieId est ignoré (géré manuellement dans UserService)
     */
    @Mapping(target = "defaultPoissonnerie", ignore = true)  // Géré dans le service
    @Mapping(target = "id", ignore = true)                   // Auto-généré
    @Mapping(target = "active", ignore = true)               // Défini dans le service
    @Mapping(target = "createdAt", ignore = true)            // Auto-généré par @CreationTimestamp
    @Mapping(target = "updatedAt", ignore = true)            // Auto-généré par @UpdateTimestamp
    @Mapping(target = "avatarPath", ignore = true)   // ← AJOUTER
    @Mapping(target = "authorities", ignore = true)
    User toEntity(CreateUserRequest request);

    /**
     * Met à jour un User existant avec UpdateUserRequest
     * <p>
     * IMPORTANT :
     * - defaultPoissonnerieId est ignoré (géré manuellement dans UserService)
     * - Les champs null dans UpdateUserRequest ne modifient pas l'entité existante
     */
    @Mapping(target = "defaultPoissonnerie", ignore = true)  // Géré dans le service
    @Mapping(target = "id", ignore = true)                   // Ne doit pas changer
    @Mapping(target = "password", ignore = true)             // Ne jamais modifier via update simple
    @Mapping(target = "active", ignore = true)               // Géré séparément
    @Mapping(target = "createdAt", ignore = true)            // Ne change jamais
    @Mapping(target = "updatedAt", ignore = true)            // Auto-mis à jour
    @Mapping(target = "avatarPath", ignore = true)   // ← AJOUTER
    @Mapping(target = "authorities", ignore = true)
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User entity);
}