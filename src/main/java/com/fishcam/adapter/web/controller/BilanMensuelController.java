package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.BilanMensuelResponse;
import com.fishcam.adapter.web.dto.response.ComparaisonBoutiquesResponse;
import com.fishcam.application.bilan.BilanMensuelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/bilans")
@RequiredArgsConstructor
@Tag(name = "bilan", description = "Gestion des Bilans")
public class BilanMensuelController {

    private final BilanMensuelService bilanMensuelService;
    

    @GetMapping
    @Operation(summary = "Affiche un bilan en fonction d'une période")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<BilanMensuelResponse> getBilanMensuel(
            @RequestParam Long poissonnerieId,
            @RequestParam Integer mois,
            @RequestParam Integer annee){

        BilanMensuelResponse response = bilanMensuelService
                .getBilanMensuel(poissonnerieId, mois, annee);
        return ApiResponse.<BilanMensuelResponse>builder()
                .success(true)
                .data(response)
                .message("Bilan récupérée avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/comparaison")
    @Operation(summary = "Comparaison des Bilans")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<ComparaisonBoutiquesResponse> comparaisonBoutique(
            @RequestParam Integer mois,
            @RequestParam Integer annee){

        ComparaisonBoutiquesResponse response = bilanMensuelService
                .compareBoutiques(mois, annee);
        return ApiResponse.<ComparaisonBoutiquesResponse>builder()
                .success(true)
                .data(response)
                .message("Historique récupéré")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

}
