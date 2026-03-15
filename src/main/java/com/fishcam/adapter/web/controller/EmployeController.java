package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.CreateEmployeRequest;
import com.fishcam.adapter.web.dto.request.UpdateEmployeRequest;
import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.EmployeResponse;
import com.fishcam.application.employe.EmployeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employes")
@RequiredArgsConstructor
@Tag(name = "Employé", description = "Gestion des employés")
public class EmployeController {

    private final EmployeService employeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un nouvel employé")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<EmployeResponse> createEmploye(
            @Valid @RequestBody CreateEmployeRequest request) {
        EmployeResponse response = employeService.createEmploye(request);
        return ApiResponse.<EmployeResponse>builder()
                .success(true)
                .data(response)
                .message("Employé créé avec succès")
                .code(201)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping
    @Operation(summary = "Lister les employés d'une poissonnerie")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<List<EmployeResponse>> getEmployesByPoissonnerie(
            @RequestParam Long poissonnerieId) {
        List<EmployeResponse> response = employeService.getEmployesByPoissonnerie(poissonnerieId);
        return ApiResponse.<List<EmployeResponse>>builder()
                .success(true)
                .data(response)
                .message("Liste des employés récupérée")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un employé par ID")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<EmployeResponse> getEmployeById(@PathVariable Long id) {
        EmployeResponse response = employeService.getEmployeById(id);
        return ApiResponse.<EmployeResponse>builder()
                .success(true)
                .data(response)
                .message("Employé trouvé")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un employé")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<EmployeResponse> updateEmploye(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeRequest request) {
        EmployeResponse response = employeService.updateEmploye(id, request);
        return ApiResponse.<EmployeResponse>builder()
                .success(true)
                .data(response)
                .message("Employé modifié avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Désactiver un employé")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<Void> deleteEmploye(@PathVariable Long id) {
        employeService.deleteEmploye(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Employé désactivé avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }
}