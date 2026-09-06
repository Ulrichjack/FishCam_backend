package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.SaveReleveSituationRequest;
import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.ReleveSituationResponse;
import com.fishcam.application.gestion.ReleveSituationMensuelleService;
import com.fishcam.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/releves-mensuels")
@RequiredArgsConstructor
@Tag(name = "Relevés mensuels", description = "Stock et créances constatés à une date")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
public class ReleveSituationMensuelleController {

    private final ReleveSituationMensuelleService releveService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enregistrer ou corriger un relevé mensuel")
    public ApiResponse<ReleveSituationResponse> enregistrer(
            @RequestBody @Valid SaveReleveSituationRequest request,
            @AuthenticationPrincipal User currentUser) {
        ReleveSituationResponse response = releveService.enregistrer(request, currentUser.getId());
        return reponse(response, "Relevé enregistré", HttpStatus.CREATED.value());
    }

    @GetMapping
    @Operation(summary = "Lister les relevés d'une poissonnerie")
    public ApiResponse<List<ReleveSituationResponse>> lister(@RequestParam Long poissonnerieId) {
        List<ReleveSituationResponse> response = releveService.lister(poissonnerieId);
        return reponse(response, "Relevés récupérés", HttpStatus.OK.value());
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
