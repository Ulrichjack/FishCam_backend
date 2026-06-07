package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.CreateFournisseurRequest;
import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.FournisseurResponse;
import com.fishcam.adapter.web.dto.response.StatistiquesGlobalesResponse;
import com.fishcam.adapter.web.dto.response.StatistiquesPoissonnerieResponse;
import com.fishcam.application.statistique.StatistiquesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/statistiques")
@RequiredArgsConstructor
@Tag(name = "Statistique", description = "Gestion des stats")
public class StatistiquesController {

    private final StatistiquesService statistiquesService;

    @GetMapping("/poissonneries/{poissonnerieId}/dashboard")
    @Operation(summary = "Dashboard spécifiques")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE')")
    public ApiResponse<StatistiquesPoissonnerieResponse> getDashboardStats(
            @PathVariable Long poissonnerieId){
        StatistiquesPoissonnerieResponse response = statistiquesService.getDashboardStats(poissonnerieId);
        return ApiResponse.<StatistiquesPoissonnerieResponse>builder()
                .success(true)
                .data(response)
                .message("Dashboard spécifique générée avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/global")
    @Operation(summary = "Dashboard Global")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<StatistiquesGlobalesResponse> getGlobalDashboardStats(){
        StatistiquesGlobalesResponse response = statistiquesService.getGlobalDashboardStats();
        return ApiResponse.<StatistiquesGlobalesResponse>builder()
                .success(true)
                .data(response)
                .message("Dashboard Global générée avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }


}
