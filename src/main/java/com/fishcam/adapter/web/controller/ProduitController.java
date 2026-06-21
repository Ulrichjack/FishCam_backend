package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.CreateProduitRequest;
import com.fishcam.adapter.web.dto.request.UpdateProduitRequest;
import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.ProduitAvecPrixResponse;
import com.fishcam.adapter.web.dto.response.ProduitResponse;
import com.fishcam.application.produit.ProduitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/produits")
@RequiredArgsConstructor
@Tag(name = "Produit", description = "Gestion des produits")
public class ProduitController {

    private final ProduitService produitService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un nouveau Produit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON','CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<ProduitResponse> createProduit(@Valid @RequestBody CreateProduitRequest request){
        ProduitResponse response = produitService.createProduit(request);
        return ApiResponse.<ProduitResponse>builder()
                .success(true)
                .data(response)
                .message("Produit créée avec succès")
                .code(201)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping
    @Operation(summary = "Lister des produits (paginated)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON','CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<Page<ProduitResponse>> getAllProduits (
            @PageableDefault(sort = "nom", direction = Sort.Direction.ASC) Pageable pageable){
        Page<ProduitResponse> page = produitService.getAllProduits(pageable);
        return ApiResponse.<Page<ProduitResponse>>builder()
                .success(true)
                .data(page)
                .message("Liste des produits récupérée")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();

    }

    @GetMapping("/avec-prix")
    @Operation(summary = "Lister des produits avec leur dernier prix (paginated)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON','CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<Page<ProduitAvecPrixResponse>> getAllProduitsAvecPrix (
            @RequestParam Long poissonnerieId,
            @PageableDefault(sort = "nom", direction = Sort.Direction.ASC) Pageable pageable){

        Page<ProduitAvecPrixResponse> page = produitService.getAllProduitsAvecPrix(poissonnerieId, pageable);
        return ApiResponse.<Page<ProduitAvecPrixResponse>>builder()
                .success(true)
                .data(page)
                .message("Liste des produits avec prix récupérée")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des produits")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<List<ProduitResponse>> searchProduits(
            @RequestParam(required = false) String q){

        List<ProduitResponse> response  = produitService.searchProduits(q);
        return ApiResponse.<List<ProduitResponse>>builder()
                .success(true)
                .data(response)
                .message("")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une produit  par ID")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<ProduitResponse> getProduitById(@PathVariable Long id){
        ProduitResponse response = produitService.getProduitById(id);
        return ApiResponse.<ProduitResponse>builder()
                .success(true)
                .data(response)
                .message("Produit trouvée")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un produit ")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<ProduitResponse> updateProduit(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProduitRequest request){
        ProduitResponse response = produitService.updateProduit(id, request);
        return ApiResponse.<ProduitResponse>builder()
                .success(true)
                .data(response)
                .message("Produit modifié avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Produit désactiver")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<Void> deleteProduit(@PathVariable Long id) {
        produitService.deleteProduit(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Produit désactiver avec success")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PatchMapping("/{id}/reactivate")
    @Operation(summary = "Réactiver un produit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<ProduitResponse> reactivateProduit(@PathVariable Long id) {
        // Assure-toi d'ajouter cette méthode dans ProduitService !
        ProduitResponse response = produitService.reactivateProduit(id);
        return ApiResponse.<ProduitResponse>builder()
                .success(true).data(response).message("Produit réactivé").code(200).timestamp(LocalDateTime.now()).build();
    }

}
