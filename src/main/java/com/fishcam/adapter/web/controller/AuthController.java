package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.LoginRequest;
import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.AuthResponse;
import com.fishcam.application.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}