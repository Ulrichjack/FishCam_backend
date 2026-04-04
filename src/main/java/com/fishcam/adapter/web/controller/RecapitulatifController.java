package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.RecapitulatifResponse;
import com.fishcam.application.rapport.RecapitulatifService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/recapitulatifs")
@RequiredArgsConstructor
@Tag(name = "Récapitulatifs", description = "Génération des récapitulatifs journaliers")
public class RecapitulatifController {

    private final RecapitulatifService recapitulatifService;

    @GetMapping
    @Operation(summary = "Générer un récapitulatif pour une période donnée")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<RecapitulatifResponse> generateRecapitulatif(
            @RequestParam Long poissonnerieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate end) {

        RecapitulatifResponse response = recapitulatifService
                .generateRecapitulatif(poissonnerieId,start,end);
        return ApiResponse.<RecapitulatifResponse>builder()
                .success(true)
                .data(response)
                .message("récapitulatif générer avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();

    }




}
