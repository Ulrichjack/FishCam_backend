package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.CreateChargeGestionRequest;
import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.ChargeGestionResponse;
import com.fishcam.application.gestion.ChargeGestionService;
import com.fishcam.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/charges-gestion")
@RequiredArgsConstructor
@Tag(name = "Charges de gestion", description = "Charges mensuelles récurrentes ou ponctuelles")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
public class ChargeGestionController {

    private final ChargeGestionService chargeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une charge de gestion")
    public ApiResponse<ChargeGestionResponse> creer(
            @RequestBody @Valid CreateChargeGestionRequest request,
            @AuthenticationPrincipal User currentUser) {
        ChargeGestionResponse response = chargeService.creer(request, currentUser.getId());
        return reponse(response, "Charge enregistrée", HttpStatus.CREATED.value());
    }

    @GetMapping
    @Operation(summary = "Lister les charges applicables à un mois")
    public ApiResponse<List<ChargeGestionResponse>> listerApplicables(
            @RequestParam Integer mois,
            @RequestParam Integer annee,
            @RequestParam(required = false) Long poissonnerieId) {
        List<ChargeGestionResponse> response = chargeService
                .listerApplicables(mois, annee, poissonnerieId);
        return reponse(response, "Charges récupérées", HttpStatus.OK.value());
    }

    @PatchMapping("/{id}/terminer")
    @Operation(summary = "Fixer la date de fin d'une charge sans modifier son historique")
    public ApiResponse<ChargeGestionResponse> terminer(
            @PathVariable Long id,
            @RequestParam LocalDate dateFin) {
        ChargeGestionResponse response = chargeService.terminer(id, dateFin);
        return reponse(response, "Charge terminée", HttpStatus.OK.value());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une charge de gestion")
    public ApiResponse<ChargeGestionResponse> modifier(
            @PathVariable Long id,
            @RequestBody @Valid CreateChargeGestionRequest request,
            @AuthenticationPrincipal User currentUser) {
        ChargeGestionResponse response = chargeService.modifier(id, request, currentUser.getId());
        return reponse(response, "Charge modifiée", HttpStatus.OK.value());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer logiquement une charge saisie par erreur")
    public ApiResponse<ChargeGestionResponse> supprimer(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        ChargeGestionResponse response = chargeService.supprimer(id, currentUser.getId());
        return reponse(response, "Charge supprimée", HttpStatus.OK.value());
    }

    private <T> ApiResponse<T> reponse(T data, String message, int code) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .code(code)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
