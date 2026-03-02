package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.ChangePasswordRequest;
import com.fishcam.adapter.web.dto.request.LoginRequest;
import com.fishcam.adapter.web.dto.request.ResetPasswordRequest;
import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.AuthResponse;
import com.fishcam.application.auth.AuthService;
import com.fishcam.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Endpoints de connexion et sécurité")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur", description = "Authentifie un utilisateur avec téléphone et mot de passe")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .data(authResponse)
                        .message("Connexion réussie")
                        .code(200)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping("/me")
    @Operation(summary = "Informations utilisateur connecté")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> getCurrentUser(
            @RequestHeader("Authorization") String token) {

        AuthResponse.UserInfo userInfo = authService.getCurrentUserInfo(token);

        return ResponseEntity.ok(
                ApiResponse.<AuthResponse.UserInfo>builder()
                        .success(true)
                        .data(userInfo)
                        .message("Informations récupérées")
                        .code(200)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * Changer SON PROPRE mot de passe.
     * Tout le monde peut le faire (connecté).
     */
    @Operation(
            summary = "Changer son mot de passe",
            description = "Permet à tout utilisateur connecté de changer son propre mot de passe. "
                    + "Il doit fournir son ancien mot de passe pour vérification."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mot de passe changé"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ancien mot de passe incorrect ou nouveau identique")
    })
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        String message = authService.changePassword(request, currentUser);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message(message)
                .build());
    }

    /**
     * Réinitialiser le mot de passe d'un AUTRE utilisateur.
     * Réservé au PATRON et SUPER_ADMIN.
     */
    @Operation(
            summary = "Réinitialiser le mot de passe d'un utilisateur",
            description = "Permet au PATRON ou SUPER_ADMIN de réinitialiser le mot de passe "
                    + "d'un employé qui l'a oublié. Pas besoin de l'ancien mot de passe."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mot de passe réinitialisé"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Accès refusé"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @PutMapping("/reset-password")
    @PreAuthorize("hasAnyRole('PATRON', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            Authentication authentication) {

        User admin = (User) authentication.getPrincipal();
        String message = authService.resetPassword(request, admin);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message(message)
                .build());
    }
}