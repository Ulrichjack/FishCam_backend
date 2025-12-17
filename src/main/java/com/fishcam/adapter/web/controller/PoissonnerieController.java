package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.CreatePoissonnerieRequest;
import com.fishcam.adapter.web.dto.request.UpdatePoissonnerieRequest;
import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.PoissonnerieResponse;
import com.fishcam.application.poissonnerie.PoissonnerieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/poissonneries")
@RequiredArgsConstructor
@Tag(name = "Poissonneries", description = "Gestion des poissonneries")
public class PoissonnerieController {

    private final PoissonnerieService poissonnerieService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une nouvelle poissonnerie")
    public ApiResponse<PoissonnerieResponse> createPoissonnerie(@Valid @RequestBody CreatePoissonnerieRequest request) {
        PoissonnerieResponse response = poissonnerieService.createPoissonnerie(request);
        return ApiResponse.<PoissonnerieResponse>builder()
                .success(true)
                .data(response)
                .message("Poissonnerie créée avec succès")
                .code(201)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping
    @Operation(summary = "Lister les poissonneries actives (paginated)")
    public ApiResponse<Page<PoissonnerieResponse>> getAllPoissonneries(Pageable pageable) {
        Page<PoissonnerieResponse> page = poissonnerieService.getAllPoissonneries(pageable);
        return ApiResponse.<Page<PoissonnerieResponse>>builder()
                .success(true)
                .data(page)
                .message("Liste des poissonneries récupérée")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une poissonnerie par ID")
    public ApiResponse<PoissonnerieResponse> getPoissonnerieById(@PathVariable Long id) {
        PoissonnerieResponse response = poissonnerieService.getPoissonnerieById(id);
        return ApiResponse.<PoissonnerieResponse>builder()
                .success(true)
                .data(response)
                .message("Poissonnerie trouvée")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une poissonnerie")
    public ApiResponse<PoissonnerieResponse> updatePoissonnerie(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePoissonnerieRequest request) {
        PoissonnerieResponse response = poissonnerieService.updatePoissonnerie(id, request);
        return ApiResponse.<PoissonnerieResponse>builder()
                .success(true)
                .data(response)
                .message("Poissonnerie modifiée avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Désactiver une poissonnerie")
    public ApiResponse<Void> deletePoissonnerie(@PathVariable Long id) {
        poissonnerieService.deletePoissonnerie(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Poissonnerie désactivée avec succès")
                .code(204)
                .timestamp(LocalDateTime.now())
                .build();
    }
}