package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.ResultatMensuelBoutiqueResponse;
import com.fishcam.adapter.web.dto.response.ResultatMensuelGlobalResponse;
import com.fishcam.adapter.web.dto.response.ResultatAnnuelResponse;
import com.fishcam.application.gestion.ResultatMensuelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/resultats-mensuels")
@RequiredArgsConstructor
@Tag(name = "Résultats mensuels", description = "Résultats provisoires et validés de gestion")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
public class ResultatMensuelController {

    private final ResultatMensuelService resultatService;

    @GetMapping("/boutique")
    @Operation(summary = "Calculer le résultat mensuel d'une poissonnerie")
    public ApiResponse<ResultatMensuelBoutiqueResponse> calculerBoutique(
            @RequestParam Long poissonnerieId,
            @RequestParam Integer mois,
            @RequestParam Integer annee) {
        ResultatMensuelBoutiqueResponse response = resultatService
                .calculerBoutique(poissonnerieId, mois, annee);
        return reponse(response);
    }

    @GetMapping("/global")
    @Operation(summary = "Calculer le résultat mensuel de toute l'entreprise")
    public ApiResponse<ResultatMensuelGlobalResponse> calculerGlobal(
            @RequestParam Integer mois,
            @RequestParam Integer annee) {
        ResultatMensuelGlobalResponse response = resultatService.calculerGlobal(mois, annee);
        return reponse(response);
    }

    @GetMapping("/annuel")
    @Operation(summary = "Calculer le résultat annuel consolidé")
    public ApiResponse<ResultatAnnuelResponse> calculerAnnuel(@RequestParam Integer annee) {
        return reponse(resultatService.calculerAnnuel(annee));
    }

    private <T> ApiResponse<T> reponse(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message("Résultat mensuel calculé")
                .code(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
