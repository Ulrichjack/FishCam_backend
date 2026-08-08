package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.ClotureJournaliereRequest;
import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.ClotureJournaliereResponse;
import com.fishcam.adapter.web.dto.response.PreparationClotureResponse;
import com.fishcam.application.cloture.ClotureJournaliereService;
import com.fishcam.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clotures")
@RequiredArgsConstructor
@Tag(name = "Cloture", description = "Gestion des Clotures journaliers")
public class ClotureJournaliereController {

    private final ClotureJournaliereService clotureJournaliereService;

    @GetMapping("/preparer")
    @Operation(summary = "Preparer les donnees")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<PreparationClotureResponse> preparer(
            @RequestParam Long poissonnerieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        PreparationClotureResponse response = clotureJournaliereService
                .preparerCloture(poissonnerieId, date);
        return ApiResponse.<PreparationClotureResponse>builder()
                .success(true)
                .data(response)
                .message("Preparation effectue")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Clôturer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<ClotureJournaliereResponse> cloturer(
            @RequestBody @Valid ClotureJournaliereRequest request,
            @AuthenticationPrincipal User currentUser ){

        ClotureJournaliereResponse response = clotureJournaliereService
                .cloturer(request,currentUser.getId());
        return ApiResponse.<ClotureJournaliereResponse>builder()
                .success(true)
                .data(response)
                .message("Journée clôturée avec succès")
                .code(201)
                .timestamp(LocalDateTime.now())
                .build();
    }
    @GetMapping
    @Operation(summary = "affiche une cloture")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<ClotureJournaliereResponse> getCloturer(
            @RequestParam Long poissonnerieId
            , @RequestParam @DateTimeFormat (iso = DateTimeFormat.ISO.DATE) LocalDate date){

        ClotureJournaliereResponse response = clotureJournaliereService
                .getCloture(poissonnerieId, date);
        return ApiResponse.<ClotureJournaliereResponse>builder()
                .success(true)
                .data(response)
                .message("Clôture récupérée")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/historique")
    @Operation(summary = "Historique des clotures (Paginé)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<Page<ClotureJournaliereResponse>> getHistorique(
            @RequestParam Long poissonnerieId,
            @PageableDefault(size = 10) Pageable pageable) { // <-- 10 lignes par page par défaut

        Page<ClotureJournaliereResponse> response = clotureJournaliereService
                .getHistorique(poissonnerieId, pageable);

        return ApiResponse.<Page<ClotureJournaliereResponse>>builder()
                .success(true)
                .data(response)
                .message("Historique récupéré")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

}
