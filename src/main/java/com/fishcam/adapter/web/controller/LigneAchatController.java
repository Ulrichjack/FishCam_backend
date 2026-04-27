package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.DernierPrixResponse;
import com.fishcam.application.achat.AchatJournalierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/lignes")
@RequiredArgsConstructor
@Tag(name = "Lignes d'achat", description = "Gestion des lignes de facture")
public class LigneAchatController {

    private final AchatJournalierService achatJournalierService;

    @GetMapping("/dernier-prix")
    @Operation(summary = "Récupérer le dernier prix d'un produit dans une boutique")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<DernierPrixResponse> getDernierPrix(
            @RequestParam Long produitId,
            @RequestParam Long poissonnerieId) {
        DernierPrixResponse response = achatJournalierService.getDernierPrix(produitId, poissonnerieId);
        return ApiResponse.<DernierPrixResponse>builder()
                .success(true)
                .data(response)
                .message("Dernier prix récupéré")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }
}