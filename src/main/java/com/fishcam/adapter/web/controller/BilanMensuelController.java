package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.GenererBilanRequest;
import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.BilanMensuelResponse;
import com.fishcam.application.bilan.BilanMensuelService;
import com.fishcam.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bilans")
@RequiredArgsConstructor
@Tag(name = "bilan", description = "Gestion des Bilans")
public class BilanMensuelController {

    private final BilanMensuelService bilanMensuelService;

    @PostMapping
    @Operation(summary = "Générer les bilans")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<BilanMensuelResponse> generateBilan(
            @RequestBody @Valid GenererBilanRequest request,
            @AuthenticationPrincipal User currentUser){

        BilanMensuelResponse response = bilanMensuelService
                .generateBilan(request, currentUser);
        return ApiResponse.<BilanMensuelResponse>builder()
                .success(true)
                .data(response)
                .message("bilan générer avec succès")
                .code(201)
                .timestamp(LocalDateTime.now())
                .build();

    }

    @GetMapping
    @Operation(summary = "Affiche un bilan")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<BilanMensuelResponse> getBilan(
            @RequestParam Long poissonnerieId,
            @RequestParam Integer mois,
            @RequestParam Integer annee){

        BilanMensuelResponse response = bilanMensuelService
                .getBilan(poissonnerieId, mois, annee);
        return ApiResponse.<BilanMensuelResponse>builder()
                .success(true)
                .data(response)
                .message("Bilan récupérée")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/historique")
    @Operation(summary = "Historique des Bilans")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<List<BilanMensuelResponse>> getHistorique(
            @RequestParam Long poissonnerieId){

        List<BilanMensuelResponse> response = bilanMensuelService
                .getHistorique(poissonnerieId);
        return ApiResponse.<List<BilanMensuelResponse>>builder()
                .success(true)
                .data(response)
                .message("Historique récupéré")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

}
