package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.CreateFactureRequest;
import com.fishcam.adapter.web.dto.request.CreateLigneRequest;
import com.fishcam.adapter.web.dto.request.UpdateLigneRequest;
import com.fishcam.adapter.web.dto.response.*;
import com.fishcam.application.achat.AchatJournalierService;
import com.fishcam.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/factures")
@RequiredArgsConstructor
@Tag(name = "Achat", description = "Gestion des achats journaliers")
public class AchatJournalierController {

    private final AchatJournalierService achatJournalierService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une nouvelle facture")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE','ENREGISTREUR')")
    public ApiResponse<FactureResponse> createFacture(
            @Valid @RequestBody CreateFactureRequest request,
            @AuthenticationPrincipal User currentUser) {
        FactureResponse response = achatJournalierService.createFacture(request, currentUser.getId());
        return ApiResponse.<FactureResponse>builder()
                .success(true)
                .data(response)
                .message("Facture créée avec succès")
                .code(201)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping
    @Operation(summary = "Lister les factures par poissonnerie et date")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<List<FactureResponse>> getFacturesByPoissonnerieAndDate(
            @RequestParam Long poissonnerieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<FactureResponse> response = achatJournalierService
                .getFacturesByPoissonnerieAndDate(poissonnerieId, date);
        return ApiResponse.<List<FactureResponse>>builder()
                .success(true)
                .data(response)
                .message("Factures récupérées")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une facture avec ses lignes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<FactureDetailResponse> getFactureDetail(@PathVariable Long id) {
        FactureDetailResponse response = achatJournalierService.getFactureDetail(id);
        return ApiResponse.<FactureDetailResponse>builder()
                .success(true)
                .data(response)
                .message("Détail facture récupéré")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PutMapping("/{id}/cloturer")
    @Operation(summary = "Clôturer une facture")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<FactureResponse> cloturerFacture(@PathVariable Long id) {
        FactureResponse response = achatJournalierService.cloturerFacture(id);
        return ApiResponse.<FactureResponse>builder()
                .success(true)
                .data(response)
                .message("Facture clôturée avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PostMapping("/{id}/lignes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ajouter une ligne à une facture")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<LigneAchatResponse> addLigne(
            @PathVariable Long id,
            @Valid @RequestBody CreateLigneRequest request) {
        LigneAchatResponse response = achatJournalierService.addLigne(id, request);
        return ApiResponse.<LigneAchatResponse>builder()
                .success(true)
                .data(response)
                .message("Ligne ajoutée avec succès")
                .code(201)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PutMapping("/{factureId}/lignes/{ligneId}")
    @Operation(summary = "Modifier une ligne")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<LigneAchatResponse> updateLigne(
            @PathVariable Long factureId,
            @PathVariable Long ligneId,
            @Valid @RequestBody UpdateLigneRequest request) {
        LigneAchatResponse response = achatJournalierService.updateLigne(factureId, ligneId, request);
        return ApiResponse.<LigneAchatResponse>builder()
                .success(true)
                .data(response)
                .message("Ligne modifiée avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @DeleteMapping("/{factureId}/lignes/{ligneId}")
    @Operation(summary = "Supprimer une ligne")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<Void> deleteLigne(
            @PathVariable Long factureId,
            @PathVariable Long ligneId) {
        achatJournalierService.deleteLigne(factureId, ligneId);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Ligne supprimée avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }


}
