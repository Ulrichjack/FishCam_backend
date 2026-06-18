package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.CreateLivreurRequest;
import com.fishcam.adapter.web.dto.request.UpdateLivreurRequest;
import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.LivreurResponse;
import com.fishcam.application.livreur.LivreurService;
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
@RequestMapping("/api/v1/livreurs")
@RequiredArgsConstructor
@Tag(name = "Livreur", description = "Gestion des livreurs")
public class LivreurController {

    private final LivreurService livreurService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    @Operation(summary = "Créer un nouveau livreur")
    public ApiResponse<LivreurResponse> createLivreur(
            @RequestBody @Valid CreateLivreurRequest request){
        LivreurResponse response = livreurService.createLivreur(request);
        return ApiResponse.<LivreurResponse>builder()
                .success(true)
                .data(response)
                .message("Livreur créé avec succès")
                .code(201)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<List<LivreurResponse>>getAllLivreurs(){
        List<LivreurResponse> response = livreurService.getAllLivreurs();
        return ApiResponse.<List<LivreurResponse>>builder()
                .success(true)
                .data(response)
                .message("Liste récupère avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PatchMapping("/{id}/toggle-statut")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    @Operation(summary = "Modifie status livreur")
    public ApiResponse<LivreurResponse> toggleLivreur(
            @PathVariable Long id){
        LivreurResponse response = livreurService.toggleStatut(id);
        return ApiResponse.<LivreurResponse>builder()
                .success(true)
                .data(response)
                .message(" status Livreur change avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/dernier-utilise")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    @Operation(summary = "Récupérer le dernier livreur utilisé (UX)")
    public ApiResponse<LivreurResponse> getLastLivreur() {
        LivreurResponse response = livreurService.getLastLivreurUtilise();
        return ApiResponse.<LivreurResponse>builder()
                .success(true)
                .data(response)
                .message("Dernier livreur récupéré")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    @Operation(summary = "Modifier un livreur")
    public ApiResponse<LivreurResponse> updateLivreur(
            @PathVariable Long id,
            @RequestBody @Valid UpdateLivreurRequest request) {
        LivreurResponse response = livreurService.updateLivreur(id, request);
        return ApiResponse.<LivreurResponse>builder()
                .success(true)
                .data(response)
                .message("Livreur modifié avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
