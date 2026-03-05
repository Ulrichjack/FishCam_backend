package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.CreateEpargneRequest;
import com.fishcam.adapter.web.dto.request.DepotEpargneRequest;
import com.fishcam.adapter.web.dto.request.RetraitEpargneRequest;
import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.EpargneDetailResponse;
import com.fishcam.adapter.web.dto.response.EpargneResponse;
import com.fishcam.application.epargne.EpargneService;
import com.fishcam.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/epargnes")
@RequiredArgsConstructor
@Tag(name = "Épargnes", description = "Gestion des comptes épargne")
public class EpargneController {

    private final EpargneService epargneService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ouvrir un compte épargne pour un client")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE')")
    public ApiResponse<EpargneResponse> createEpargne(
            @Valid @RequestBody CreateEpargneRequest request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        log.info("Création épargne pour client {} par user {}", request.getClientId(), currentUser.getId());

        EpargneResponse data = epargneService.createEpargne(request, currentUser.getId());
        return ApiResponse.<EpargneResponse>builder()
                .success(true)
                .data(data)
                .message("Compte épargne créé avec succès")
                .code(201)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PostMapping("/depot")
    @Operation(summary = "Effectuer un dépôt sur un compte épargne")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE')")
    public ApiResponse<EpargneResponse> deposer(
            @Valid @RequestBody DepotEpargneRequest request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        log.info("Dépôt {} FCFA sur épargne {} par user {}",
                request.getAmount(), request.getEpargneId(), currentUser.getId());

        EpargneResponse data = epargneService.deposer(request, currentUser.getId());
        return ApiResponse.<EpargneResponse>builder()
                .success(true)
                .data(data)
                .message("Dépôt effectué avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PostMapping("/retrait")
    @Operation(summary = "Effectuer un retrait sur un compte épargne")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE')")
    public ApiResponse<EpargneResponse> retirer(
            @Valid @RequestBody RetraitEpargneRequest request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        log.info("Retrait {} FCFA sur épargne {} par user {}",
                request.getAmount(), request.getEpargneId(), currentUser.getId());

        EpargneResponse data = epargneService.retirer(request, currentUser.getId());
        return ApiResponse.<EpargneResponse>builder()
                .success(true)
                .data(data)
                .message("Retrait effectué avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer les détails d'un compte épargne avec historique")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<EpargneDetailResponse> getEpargneDetail(@PathVariable Long id) {
        log.debug("Récupération détail épargne {}", id);
        EpargneDetailResponse data = epargneService.getEpargneDetail(id);
        return ApiResponse.<EpargneDetailResponse>builder()
                .success(true)
                .data(data)
                .message("Détail de l'épargne récupéré")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }
}