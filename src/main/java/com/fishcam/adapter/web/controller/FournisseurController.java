package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.CreateFournisseurRequest;
import com.fishcam.adapter.web.dto.request.UpdateFournisseurRequest;
import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.FournisseurResponse;
import com.fishcam.application.fournisseur.FournisseurService;
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
@RequestMapping("/api/v1/fournisseurs")
@RequiredArgsConstructor
@Tag(name = "Fournisseur", description = "Gestion des fournisseur")
public class FournisseurController {

    private final FournisseurService fournisseurService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un nouveau fournisseur")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<FournisseurResponse> createFournisseur(
            @Valid @RequestBody CreateFournisseurRequest request){
        FournisseurResponse response = fournisseurService.createFournisseur(request);
        return ApiResponse.<FournisseurResponse>builder()
                .success(true)
                .data(response)
                .message("Fournisseur créé avec succès")
                .code(201)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping
    @Operation(summary = "Lister des fournisseurs ")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<List<FournisseurResponse>> getAllFournisseur( ){
        List<FournisseurResponse> response =  fournisseurService.getAllFournisseurs();
        return ApiResponse.<List<FournisseurResponse>>builder()
                .success(true)
                .data(response)
                .message("Liste des fournisseurs récupérée")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un fournisseur par ID")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<FournisseurResponse> getFournisseur (@PathVariable Long id) {
        FournisseurResponse response = fournisseurService.getFournisseurById(id);
        return ApiResponse.<FournisseurResponse>builder()
                .success(true)
                .data(response)
                .message("Fournisseur  trouvé")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un fournisseur")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<FournisseurResponse> updateFournisseur(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFournisseurRequest request) {
        FournisseurResponse response = fournisseurService.updateFournisseur(id, request);
        return ApiResponse.<FournisseurResponse>builder()
                .success(true)
                .data(response)
                .message("Fournisseur modifié avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Désactiver un fournisseur")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<Void> deleteFournisseur(@PathVariable Long id) {
        fournisseurService.deleteFournisseur(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Fournisseur désactivé avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Ajoute l'endpoint de recherche
    @GetMapping("/search")
    @Operation(summary = "Rechercher des fournisseurs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<List<FournisseurResponse>> searchFournisseurs(@RequestParam(required = false) String term) {
        List<FournisseurResponse> response = fournisseurService.searchFournisseurs(term);
        return ApiResponse.<List<FournisseurResponse>>builder()
                .success(true)
                .data(response)
                .message("Recherche effectuée")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Ajoute l'endpoint de réactivation
    @PatchMapping("/{id}/reactivate")
    @Operation(summary = "Réactiver un fournisseur")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<FournisseurResponse> reactivateFournisseur(@PathVariable Long id) {
        FournisseurResponse response = fournisseurService.reactivateFournisseur(id);
        return ApiResponse.<FournisseurResponse>builder()
                .success(true)
                .data(response)
                .message("Fournisseur réactivé avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }


}
