package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.EmpruntRequest;
import com.fishcam.adapter.web.dto.request.ModifierLimiteCreditRequest;
import com.fishcam.adapter.web.dto.request.RemboursementCCRequest;
import com.fishcam.adapter.web.dto.request.TransfertEpargneVersCCRequest;
import com.fishcam.adapter.web.dto.response.*;
import com.fishcam.application.comptecourant.CompteCourantService;
import com.fishcam.application.comptecourant.TransfertEpargneVersCCUseCase;
import com.fishcam.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/comptes-courants")
@RequiredArgsConstructor
@Tag(name = "Comptes Courants", description = "Gestion des comptes courants clients")
public class CompteCourantController {

    private final CompteCourantService compteCourantService;
    private final TransfertEpargneVersCCUseCase transfertEpargneUseCase;

    @PostMapping("/client/{clientId}")
    @Operation(summary = "Créer un compte courant pour un client")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE')")
    public ResponseEntity<ApiResponse<CompteCourantResponse>> createCompteCourant(
            @PathVariable Long clientId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        log.info("Création compte courant pour client {} par user {}", clientId, currentUser.getId());

        CompteCourantResponse data = compteCourantService.createCompteCourant(clientId, currentUser.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<CompteCourantResponse>builder()
                        .success(true)
                        .data(data)
                        .message("Compte courant créé avec succès")
                        .code(201)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @PostMapping("/emprunts")
    @Operation(summary = "Enregistrer un emprunt")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ResponseEntity<ApiResponse<CompteCourantResponse>> enregistrerEmprunt(
            @Valid @RequestBody EmpruntRequest request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        log.info("Emprunt {} FCFA sur compte {} par user {}",
                request.getMontant(), request.getCompteCourantId(), currentUser.getId());

        CompteCourantResponse data = compteCourantService.enregistrerEmprunt(request, currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.<CompteCourantResponse>builder()
                        .success(true)
                        .data(data)
                        .message("Emprunt enregistré avec succès")
                        .code(200)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @PostMapping("/remboursements")
    @Operation(summary = "Enregistrer un remboursement")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE')")
    public ResponseEntity<ApiResponse<CompteCourantResponse>> enregistrerRemboursement(
            @Valid @RequestBody RemboursementCCRequest request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        log.info("Remboursement {} FCFA sur compte {} par user {}",
                request.getMontant(), request.getCompteCourantId(), currentUser.getId());

        CompteCourantResponse data = compteCourantService.enregistrerRemboursement(request, currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.<CompteCourantResponse>builder()
                        .success(true)
                        .data(data)
                        .message("Remboursement enregistré avec succès")
                        .code(200)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @PutMapping("/{compteId}/limite-credit")
    @Operation(summary = "Modifier la limite de crédit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ResponseEntity<ApiResponse<CompteCourantResponse>> modifierLimiteCredit(
            @PathVariable Long compteId,
            @Valid @RequestBody ModifierLimiteCreditRequest request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        log.info("Modification limite crédit compte {} par user {} ({})",
                compteId, currentUser.getId(), currentUser.getRole());

        CompteCourantResponse data = compteCourantService.modifierLimiteCredit(compteId, request, currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.<CompteCourantResponse>builder()
                        .success(true)
                        .data(data)
                        .message("Limite de crédit modifiée avec succès")
                        .code(200)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @PostMapping("/transfert-epargne")
    @Operation(summary = "Transférer épargne vers compte courant")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE')")
    public ResponseEntity<ApiResponse<TransfertEpargneVersCCResponse>> transfererEpargne(
            @Valid @RequestBody TransfertEpargneVersCCRequest request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        log.info("Transfert épargne {} FCFA par user {}", request.getMontant(), currentUser.getId());

        TransfertEpargneVersCCResponse data = transfertEpargneUseCase.execute(request, currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.<TransfertEpargneVersCCResponse>builder()
                        .success(true)
                        .data(data)
                        .message("Transfert effectué avec succès")
                        .code(200)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Obtenir le compte courant d'un client")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ResponseEntity<ApiResponse<CompteCourantResponse>> getCompteCourantByClient(
            @PathVariable Long clientId) {

        log.debug("Récupération compte courant client {}", clientId);
        CompteCourantResponse data = compteCourantService.getCompteCourantByClient(clientId);

        return ResponseEntity.ok(
                ApiResponse.<CompteCourantResponse>builder()
                        .success(true)
                        .data(data)
                        .message("Compte courant récupéré")
                        .code(200)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping("/{compteId}")
    @Operation(summary = "Obtenir les détails complets d'un compte courant")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ResponseEntity<ApiResponse<CompteCourantDetailResponse>> getCompteCourantDetail(
            @PathVariable Long compteId) {

        log.debug("Récupération détail compte {}", compteId);
        CompteCourantDetailResponse data = compteCourantService.getCompteCourantDetail(compteId);

        return ResponseEntity.ok(
                ApiResponse.<CompteCourantDetailResponse>builder()
                        .success(true)
                        .data(data)
                        .message("Détails du compte récupérés")
                        .code(200)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping("/poissonnerie/{poissonnerieId}/en-dette")
    @Operation(summary = "Lister tous les comptes en dette")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ResponseEntity<ApiResponse<List<CompteCourantResponse>>> getComptesEnDette(
            @PathVariable Long poissonnerieId) {

        log.debug("Récupération comptes en dette poissonnerie {}", poissonnerieId);
        List<CompteCourantResponse> data = compteCourantService.getComptesEnDette(poissonnerieId);

        return ResponseEntity.ok(
                ApiResponse.<List<CompteCourantResponse>>builder()
                        .success(true)
                        .data(data)
                        .message("Comptes en dette récupérés")
                        .code(200)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping("/poissonnerie/{poissonnerieId}/transactions")
    @Operation(summary = "Lister toutes les transactions (CC + Epargne) d'une poissonnerie")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE')")
    public ResponseEntity<ApiResponse<Page<TransactionGlobalResponse>>> getAllTransactions(
            @PathVariable Long poissonnerieId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable) {

        log.debug("Récupération du journal des transactions pour la poissonnerie {}", poissonnerieId);
        Page<TransactionGlobalResponse> data = compteCourantService.getAllTransactions(poissonnerieId, type, searchTerm, date, pageable);

        return ResponseEntity.ok(
                ApiResponse.<Page<TransactionGlobalResponse>>builder()
                        .success(true)
                        .data(data)
                        .message("Journal des transactions récupéré")
                        .code(200)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}