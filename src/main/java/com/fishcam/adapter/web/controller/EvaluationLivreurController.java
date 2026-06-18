package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.CreateEvaluationRequest;
import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.EvaluationLivreurResponse;
import com.fishcam.application.livreur.EvaluationLivreurService;
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
@RequestMapping("/api/v1/evaluations")
@RequiredArgsConstructor
@Tag(name = "Evaluation", description = "Evaluation des livreurs")
public class EvaluationLivreurController {

    private final EvaluationLivreurService evaluationLivreurService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    @Operation(summary = "Créer une Evalaution")
    public ApiResponse<EvaluationLivreurResponse> createEvalaution(
            @RequestBody @Valid CreateEvaluationRequest request,
            @AuthenticationPrincipal User currentUser){
        EvaluationLivreurResponse response = evaluationLivreurService.createEvaluation(request, currentUser.getId());
        return ApiResponse.<EvaluationLivreurResponse>builder()
                .success(true)
                .data(response)
                .message("Evaluation créé avec succès")
                .code(201)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/livreur/{livreurId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<List<EvaluationLivreurResponse>>getEvaluation(
            @PathVariable Long livreurId){
        List<EvaluationLivreurResponse> response = evaluationLivreurService.getEvaluationsByLivreur(livreurId);
        return ApiResponse.<List<EvaluationLivreurResponse>>builder()
                .success(true)
                .data(response)
                .message("Liste récupère avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/facture/{factureId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<EvaluationLivreurResponse> getEvaluationByFacture(@PathVariable Long factureId) {
        EvaluationLivreurResponse response = evaluationLivreurService.getEvaluationByFacture(factureId);
        return ApiResponse.<EvaluationLivreurResponse>builder()
                .success(true)
                .data(response)
                .message(response != null ? "Évaluation trouvée" : "Aucune évaluation")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

}
